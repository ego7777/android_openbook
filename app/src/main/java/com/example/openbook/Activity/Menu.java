package com.example.openbook.Activity;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Adapter.CartAdapter;
import com.example.openbook.Adapter.MenuAdapter;
import com.example.openbook.Adapter.SideListViewAdapter;
import com.example.openbook.BuildConfig;
import com.example.openbook.CartCategory;
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.ChattingData;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.OrderList;
import com.example.openbook.Data.SideList;
import com.example.openbook.Deco.menu_recyclerview_deco;
import com.example.openbook.DialogManager;
import com.example.openbook.FCM.FCM;
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.PaymentCategory;
import com.example.openbook.kakaopay.KakaoPay;
import com.example.openbook.retrofit.MenuListDTO;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.R;
import com.example.openbook.Data.TicketData;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.MenuList;
import com.example.openbook.Data.TableList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class Menu extends AppCompatActivity {

    String TAG = "MenuTAG";

    int totalPrice, myTable;

    ArrayList<MenuList> menuLists;
    ArrayList<CartList> cartLists;
    ArrayList<TableList> tableList;
    ListView menuNavigation;


    SharedPreferences pref;
    SharedPreferences.Editor editor;

    TextView appbarMenuTable, appbarOrderList, cartOrderTotalPrice;

    CartAdapter cartAdapter;
    MenuAdapter menuAdapter;
    SideListViewAdapter sideAdapter;

    Button cartOrderButton, menuClose;

    RecyclerView menuRecyclerview;

    ClientSocket clientSocket;


    DBHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;

    SendNotification sendNotification;

    MyData myData;
    HashMap<String, ChattingData> chattingDataHashMap;
    HashMap<String, TicketData> ticketDataHashMap;
    SendToPopUp sendToPopUp = new SendToPopUp();
    RetrofitService service;
    DialogManager dialogManager;
    Dialog progressbar;

    //액티비티가 onCreate 되면 자동으로 받는거고
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("chattingRequestArrived")) {
                String fcmData = intent.getStringExtra("fcmData");

                sendToPopUp.sendToPopUpChatting(Menu.this, myData,
                        chattingDataHashMap, ticketDataHashMap, tableList, fcmData);
            } else if (intent.getAction().equals("giftArrived")) {
                String from = intent.getStringExtra("tableName");
                String menuName = intent.getStringExtra("menuName");

                sendToPopUp.sendToPopUpGift(Menu.this, myData,
                        chattingDataHashMap, ticketDataHashMap, tableList, from, menuName);

            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        dialogManager = new DialogManager();
        progressbar = dialogManager.progressDialog(Menu.this);
        progressbar.show();
        Log.d(TAG, "progressbar start");


        // 로컬 브로드캐스트 리시버 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("chattingRequestArrived");
        intentFilter.addAction("giftArrived");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        Log.d(TAG, "myData paymentStyle: " + myData.getPaymentStyle());
        Log.d(TAG, "myData isOrder: " + myData.isOrder());
        Log.d(TAG, "myData identifier: " + myData.getIdentifier());

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
        Log.d(TAG, "chattingData size: " + chattingDataHashMap);

        ticketDataHashMap = (HashMap<String, TicketData>) getIntent().getSerializableExtra("ticketData");


        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");


        /**
         * 로그인을 성공하면 id, token을 firebase realtime db에 저장
         */

        if (!myData.isFcmExist()) {
            Intent fcm = new Intent(getApplicationContext(), FCM.class);
            fcm.putExtra("identifier", myData.getIdentifier());
            startService(fcm);
        }

        sendNotification = new SendNotification();
        Log.d(TAG, "paymentStyle: " + myData.getPaymentStyle());
        if (myData.getPaymentStyle() == PaymentCategory.NOW.getValue() && !myData.isUsedTable()) {

            sendNotification.usingTableUpdate("admin",
                    myData.getId(),
                    "PayNow");
            myData.setUsedTable(true);
        }


        /**
         * AppBar: 로그인하면 table number 바로 나오는거
         */
        TextView tableName = findViewById(R.id.appbar_menu_table_number);
        tableName.setText(myData.getId());

        menuClose = findViewById(R.id.menu_close);

        if (myData.getPaymentStyle() == PaymentCategory.LATER.getValue()) {
            menuClose.setVisibility(View.GONE);
        }
        appbarMenuTable = findViewById(R.id.appbar_menu_table);
        appbarOrderList = findViewById(R.id.appbar_menu_orderList);


        /**
         * 장바구니
         */
        RecyclerView cartRecyclerview = findViewById(R.id.menu_cart_recyclerview);
        cartRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        RecyclerView.ItemAnimator animator = cartRecyclerview.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        cartAdapter = new CartAdapter();
        cartLists = new ArrayList<>();
        cartRecyclerview.setAdapter(cartAdapter);

        pref = getSharedPreferences("menu_activity", MODE_PRIVATE);
        editor = pref.edit();

        cartOrderTotalPrice = findViewById(R.id.cart_order_total_price);
        cartOrderButton = findViewById(R.id.cart_order_button);

        /**
         * 안주
         */
        menuRecyclerview = findViewById(R.id.menu_recyclerview);
        menuRecyclerview.setLayoutManager(new GridLayoutManager(this, 3));
        menuRecyclerview.addItemDecoration(new menu_recyclerview_deco(Menu.this));

        menuAdapter = new MenuAdapter();

        menuLists = new ArrayList<>();
        menuRecyclerview.setAdapter(menuAdapter);

        menuAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    runOnUiThread(() -> {
                        progressbar.dismiss();
                        Log.d(TAG, "progress bar dismiss: ");
                    });
                }, 1000);

            }
        });

        int version = 1;
        version++;

        dbHelper = new DBHelper(Menu.this, version);
        sqLiteDatabase = dbHelper.getWritableDatabase();
        Cursor res = dbHelper.getTableData("menuListTable");


        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

        if (res.getCount() == 0) {
            Log.d(TAG, "메뉴 db 새로 받아오기");

            Call<MenuListDTO> call = service.getMenuList();
            call.enqueue(new Callback<MenuListDTO>() {
                @Override
                public void onResponse(Call<MenuListDTO> call, Response<MenuListDTO> response) {
                    Log.d(TAG, "onResponse: " + response);
                    if (response.isSuccessful()) {
                        switch (response.body().getResult()) {
                            case "success":
                                setMenuList(response.body().getItems().getItemList());
                                break;
                            case "failed":
                                Toast.makeText(Menu.this, "가져올 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else {
                        Toast.makeText(Menu.this, R.string.networkError, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<MenuListDTO> call, Throwable t) {
                    Log.d(TAG, "onFailure menu: " + t.getMessage());
                }
            });


        } else {
            Log.d(TAG, "메뉴db 있는거 사용");

            new Thread(() -> {
                while (res.moveToNext()) {
                    menuLists.add(new MenuList(res.getString(3),//img
                            res.getString(1), //name
                            res.getInt(2), //price
                            res.getInt(4), //menuType
                            1));
                }

                runOnUiThread(() -> {
                    menuAdapter.setAdapterItem(menuLists);
//                    progressbar.dismiss();

                });


            }).start();

        }


        /**
         * 사이드 네비게이션
         */
        menuNavigation = findViewById(R.id.menu_navigation);
        sideAdapter = new SideListViewAdapter();

        String[] sideList = getResources().getStringArray(R.array.sideMenu);

        for (String side : sideList) {
            sideAdapter.addItem(new SideList(side));
        }
        menuNavigation.setAdapter(sideAdapter);

        String succeedOrderList = getIntent().getStringExtra("succeedOrderList");
        Log.d(TAG, "succeedOrderList : " + succeedOrderList);
        Log.d(TAG, "onStart_PaymentStyle :" + myData.getPaymentStyle());
        if (succeedOrderList != null) {
            successOrder();
            Log.d(TAG, "successOrder 성공: ");
        }


    } // onCreate()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        //액티비티가 사용자에게 보여질 때, 사용자와 상호작용 X

//        String succeedOrderList = getIntent().getStringExtra("succeedOrderList");
//        Log.d(TAG, "succeedOrderList : " + succeedOrderList);
//        Log.d(TAG, "onStart_PaymentStyle :" + myData.getPaymentStyle());

//        if (succeedOrderList != null) {
//            SaveOrderDeleteData orderSave = new SaveOrderDeleteData();
//            try {
//                boolean success = orderSave.orderSave(succeedOrderList);
//
//                if (success == true) {
//                    successOrder();
//                    Log.d(TAG, "successOrder 성공: ");
//                } else {
//                    myData.setOrder(false);
//                }
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }

        /**
         * 액티비티 전환 시 sp에 저장된 장바구니 데이터 가져와서 뿌려주기
         */

        String cartItems = pref.getString("cart_list", null);

        if (cartItems != null) {
            cartLists = getCartItems(cartItems);

            for (CartList menu : cartLists) {
                int price = menu.getMenuPrice() * menu.getMenuQuantity();
                totalPrice += price;
                Log.d(TAG, "shared price: " + totalPrice);
                menu.setMenuPrice(price);
            }
            addCommasToNumber(totalPrice, 0);
            cartAdapter.setAdapterItem(cartLists);
        }


    } //onStart()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        //사용자와 상호작용 하는 단계, 어플 기능은 여기서

        IntentFilter intentFilter = new IntentFilter("chattingRequestArrived");
        LocalBroadcastManager.getInstance(Menu.this).registerReceiver(broadcastReceiver, intentFilter);

        menuClose.setOnClickListener(view -> {
            //admin에게 fcm 날리기
            sendNotification.usingTableUpdate("admin", myData.getId(), "End");

            editor.remove("cart_list");
            editor.remove("order_list");
            editor.commit();

            Intent intent = new Intent(Menu.this, PaymentSelect.class);
            myData.setPaymentStyle(PaymentCategory.UNSELECTED.getValue());
            myData.setOrder(false);
            myData.setUsedTable(false);
            myData.setIdentifier(0);
            intent.putExtra("myData", myData);
            startActivity(intent);

        });


        /**
         * Appbar: table 클래스로 이동
         */
        appbarMenuTable.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), Table.class);
            intent.putExtra("myData", myData);
            intent.putExtra("chattingData", chattingDataHashMap);
            intent.putExtra("ticketData", ticketDataHashMap);
            intent.putExtra("tableList", tableList);
            startActivity(intent);
        });

        appbarOrderList.setOnClickListener(v -> showReceiptDialog());


        /**
         * 장바구니 클릭 이벤트
         */
        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onPlusClick(View view, int position) {

                int originalPrice = cartLists.get(position).getOriginalPrice();
                int quantity = cartLists.get(position).getMenuQuantity() + 1;

                int newPrice = originalPrice * quantity;

                cartLists.get(position).setMenuQuantity(quantity);
                cartLists.get(position).setMenuPrice(newPrice);

                totalPrice = totalPrice + newPrice;
                addCommasToNumber(totalPrice, 0);

                cartAdapter.notifyItemChanged(position);
                cartSharedPreference("cart_list");
            }

            @Override
            public void onMinusClick(View view, int position) {

                int originalPrice = cartLists.get(position).getOriginalPrice();

                if (cartLists.get(position).getMenuQuantity() == 1) {

                    cartLists.remove(position);
                    cartAdapter.notifyItemRemoved(position);

                } else {
                    int newQuantity = cartLists.get(position).getMenuQuantity() - 1;
                    int newPrice = originalPrice * newQuantity;

                    cartLists.get(position).setMenuQuantity(newQuantity);
                    cartLists.get(position).setMenuPrice(newPrice);
                    cartAdapter.notifyItemChanged(position);
                }

                totalPrice = totalPrice - originalPrice;
                addCommasToNumber(totalPrice, 0);

                cartSharedPreference("cart_list");
            }


            @Override
            public void onDeleteClick(View view, int position) {
                int deletePrice = cartLists.get(position).getMenuPrice();

                totalPrice = totalPrice - deletePrice;

                addCommasToNumber(totalPrice, 0);

                cartLists.remove(position);
                cartAdapter.notifyItemRemoved(position);
                cartSharedPreference("cart_list");
            }
        });


        menuAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            boolean menuExist = false;

            @Override
            public void onItemClick(View view, String name, int price, int position) {
                //중복되는 메뉴의 포지션 값 get
                for (int i = 0; i < cartLists.size(); i++) {
                    if (cartLists.get(i).getMenuName().equals(name)) {
                        menuExist = true;
                        int newQuantity = cartLists.get(i).getMenuQuantity() + 1;
                        cartLists.get(i).setMenuQuantity(newQuantity);

                        int newPrice = newQuantity * price;
                        cartLists.get(i).setMenuPrice(newPrice);

                        cartAdapter.notifyItemChanged(i);
                        break;
                    }
                }

                if (!menuExist) {
                    cartLists.add(new CartList(name, price, 1, price, CartCategory.MENU));
                    cartAdapter.setAdapterItem(cartLists);
                }

                menuExist = false;

                totalPrice = totalPrice + price;
                addCommasToNumber(totalPrice, 0);
                cartSharedPreference("cart_list");

            }
        });


        sideAdapter.setOnItemClickListener((view, where) -> {
            switch (where) {
                case "메인안주":
                    menuRecyclerview.smoothScrollToPosition(0);
                    break;
                case "주류":
                    menuRecyclerview.smoothScrollToPosition(9);
                    break;
                case "사이드":
                    menuRecyclerview.smoothScrollToPosition(12);
                    break;
                case "직원호출":
                    Intent intent = new Intent(Menu.this, CallServer.class);
                    intent.putExtra("myData", myData);
                    intent.putExtra("chattingData", chattingDataHashMap);
                    intent.putExtra("ticketData", ticketDataHashMap);
                    intent.putExtra("tableList", tableList);
                    startActivity(intent);
                    break;
            }

        });


        /**
         * 1. 주문내역 관리자한테 넘어가기
         * 2. 채팅온거 표시 -> 카트 밑에 빈공간에
         * 3. 읽으면 <읽음> 표시만 하기…………
         * 4. 채팅 신청 (하트대신!!!)-> 궁금하면 사진 까봐 -> 채팅하기
         */


        /**
         * paymentStyle = "before" -> 바로 결제 붙여서 서버로 넘아가고 db에 저장
         * paymentStyle = "after" -> 이면 그냥 주문 fcm으로 날리고
         * -> admin에서 팝업 확인 누르면 -> admin s.p(or any db)에 메뉴 정보 담아서
         * -> admin에서 결제하면 -> db로 넘어가도록...!!!!
         */

        cartOrderButton.setOnClickListener(view -> {

            if (cartLists.size() == 0) {
                dialogManager.noButtonDialog(Menu.this, "장바구니가 비어있습니다.");

            } else {

                switch (myData.getPaymentStyle()) {
                    case 1:
                        // fcm으로 날리고
                        sendNotification.sendMenu(adminOrderMenuList(cartLists));
//                        orderSharedPreference();
//                        successOrder();

//                        if (clientSocket == null) {
//                            clientSocket = new ClientSocket(myData.getId(), Menu.this);
//                            clientSocket.start();
//                        }
                        break;

                    case 0:
                        Intent intent = new Intent(Menu.this, KakaoPay.class);
                        intent.putExtra("menuName", getOrderMenuName(cartLists));
                        intent.putExtra("totalPrice", totalPrice);
                        intent.putExtra("orderList", getOrderList(myData.getId(),
                                cartLists, myData.getIdentifier()));
                        intent.putExtra("myData", myData);
                        startActivity(intent);
//
//                        orderSharedPreference();
                        break;

                    default:
                        Log.d(TAG, "order click : paymentStyle이 없어..");
                        break;
                }

                myData.setOrder(true);

            } // cartList에 데이터 if-else
        }); //주문하기 click event


        if (!myData.getId().equals("구글로그인")) {
            myTable = Integer.parseInt(myData.getId().replace("table", ""));
        }


        if (tableList == null) {
            Log.d(TAG, "onResume tableList null: ");

            tableList = new ArrayList<>();

            for (int i = 1; i < myData.getTableFromDB() + 1; i++) {
                if (i == myTable) {
                    tableList.add(new TableList(myData.getId(), null, 0));
                } else {
                    tableList.add(new TableList(i, null, 1));
                }
            }
            Log.d(TAG, "tableList :" + tableList.size());

        } else {
            Log.d(TAG, "onResume tableList not null: ");
        }

    } // onResume

    public String getOrderMenuName(ArrayList<CartList> cartLists) {

        int menuQuantity = 0;
        for (CartList item : cartLists) {
            menuQuantity += item.getMenuQuantity();
        }

        String menuName;

        if (menuQuantity == 1) {
            menuName = cartLists.get(0).getMenuName();
        } else {
            menuName = cartLists.get(0).getMenuName() + " 외" + (menuQuantity - cartLists.get(0).getMenuQuantity());
        }
        return menuName;
    }

    public String adminOrderMenuList(ArrayList<CartList> cartLists) {
        JSONObject jsonObject = new JSONObject();
        try {

            JSONArray menujArray = new JSONArray();//배열이 필요할때

            for (int i = 0; i < cartLists.size(); i++)//배열
            {
                //배열 내에 들어갈 json
                JSONObject sObject = new JSONObject();
                sObject.put("menu", cartLists.get(i).getMenuName());
                sObject.put("price", cartLists.get(i).getMenuPrice());
                sObject.put("quantity", cartLists.get(i).getMenuQuantity());
                menujArray.put(sObject);
            }

            jsonObject.put("item", menujArray);
            jsonObject.put("menuName", getOrderMenuName(cartLists));
            jsonObject.put("tableName", myData.getId());
            jsonObject.put("identifier", 0);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    public void cartSharedPreference(String keyName) {

        JSONArray jsonArray = new JSONArray();

        for (CartList menu : cartLists) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("menu", menu.getMenuName());
                jsonObject.put("quantity", menu.getMenuQuantity());
                jsonObject.put("price", menu.getOriginalPrice());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }

        String json = jsonArray.toString();
        Log.d(TAG, "cartSharedPreference: " + json);
        editor.putString(keyName, json);
        editor.commit();

    }

//    private void orderSharedPreference() {
//        String orderListString = pref.getString("order_list", null);
//        Log.d(TAG, "orderSharedPreference: " + orderListString);
//
//        //shared에 저장된 내용이 있으면 기존값에 추가해서 저장
//        if (orderListString != null && !orderListString.isEmpty()) {
//            try {
//                JSONArray jsonArray = new JSONArray(orderListString);
//
//                //중복처리..!어카누..!!..!!!
//
//                for (CartList menu : cartLists) {
//                    JSONObject jsonObject = new JSONObject();
//
//                    try {
//                        jsonObject.put("menu", menu.getMenu_name());
//                        jsonObject.put("quantity", menu.getMenu_quantity());
//                        jsonObject.put("price", menu.getMenu_price());
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    jsonArray.put(jsonObject);
//                }
//
//                String json = jsonArray.toString();
//                Log.d(TAG, "orderSharedPreference json: " + json);
//                editor.putString("order_list", json);
//                editor.commit();
//
//
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            //아니면 새로 저장
//        } else {
//            cartSharedPreference("order_list");
//        }
//    }

    public ArrayList getCartItems(String cartItem) {
        try {
            JSONArray jsonArray = new JSONArray(cartItem);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String menuName = jsonObject.getString("menu");
                int menuQuantity = jsonObject.getInt("quantity");
                int menuPrice = jsonObject.getInt("price");

                cartLists.add(new CartList(menuName, menuPrice, menuQuantity, menuPrice, CartCategory.MENU));
            }
        } catch (JSONException e) {
            Log.d(TAG, "getCartItemsInShared e: " + e.getMessage());
        }
        return cartLists;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getOrderList(String id, ArrayList<CartList> cart, int identifier) {
        JSONObject orderList = new JSONObject();

        try {
            JSONArray items = new JSONArray();
            for (CartList item : cart) {
                JSONObject orderMenu = new JSONObject();//배열 내에 들어갈 json
                orderMenu.put("menu", item.getMenuName());
                orderMenu.put("price", item.getMenuPrice());
                orderMenu.put("quantity", item.getMenuQuantity());
                items.put(orderMenu);
            }
            orderList.put("tableId", id);
            orderList.put("items", items);//배열을 넣음
//            orderList.put("identifier", identifier);

            Log.d(TAG, "orderList: " + orderList);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return orderList.toString();
    }


    public void setMenuList(List<MenuListDTO.MenuItem> menuList) {

        new Thread(() -> {
            for (MenuListDTO.MenuItem menuItem : menuList) {

                String url = BuildConfig.SERVER_IP + "MenuImages/" + menuItem.getImageURL();
                String menuName = menuItem.getMenuName();
                int price = menuItem.getMenuPrice();
                int category = menuItem.getMenuCategory();

                menuLists.add(new MenuList(url, menuName, price, category, 1));
                dbHelper.insertMenuData(menuName, price, url, category);
//            myModel.insertMenuData(menuName, price, url, category);

            }
//            runOnUiThread(() -> {
            menuAdapter.setAdapterItem(menuLists);
            Log.d(TAG, "setMenuList setAdapter: ");
//                progressbar.dismiss();
//            });
        }).start();


    }


    public void successOrder() {

        cartLists = new ArrayList<>();
        cartAdapter.setAdapterItem(cartLists);
        totalPrice = 0;
        cartOrderTotalPrice.setText("총 금액 : ");

        editor.remove("cart_list");
        editor.commit();


        Dialog dlg = new Dialog(Menu.this);
        dlg.setContentView(R.layout.order_complete);
        dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dlg.show();

        ImageView img = dlg.findViewById(R.id.serve_img);
        TextView text = dlg.findViewById(R.id.serve_text);


        Animation animation = AnimationUtils.loadAnimation(Menu.this, R.anim.order_complete);
        img.startAnimation(animation);
        text.startAnimation(animation);

        Handler handler = new Handler();

        handler.postDelayed(() -> dlg.dismiss(), 1000);
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
    }

    private void showReceiptDialog() {
        Dialog dialog = new Dialog(Menu.this);
        dialog.setContentView(R.layout.admin_receipt_dialog);

        ArrayList<OrderList> orderLists = new ArrayList<>();

        String sharedOrderList = pref.getString("order_list", null);
        Log.d(TAG, "showReceiptDialog orderList: " + sharedOrderList);
        int price = 0;

        if (sharedOrderList != null) {
            try {
                JSONArray jsonArray = new JSONArray(sharedOrderList);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    orderLists.add(new OrderList(1, myData.getId(),
                            jsonObject.getString("menu"),
                            jsonObject.getInt("quantity"),
                            jsonObject.getInt("price")));

                    price = price + jsonObject.getInt("price");

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            orderLists.add(new OrderList(0, myData.getId(),
                    "주문 내역이 없습니다."));
        }


        TextView menuReceiptCancel = dialog.findViewById(R.id.admin_receipt_cancel);
        TextView menuReceiptTotalPrice = dialog.findViewById(R.id.admin_receipt_totalPrice);
        RecyclerView menuReceiptRecyclerView = dialog.findViewById(R.id.admin_receipt_recyclerView);


        AdminPopUpAdapter menuReceiptAdapter = new AdminPopUpAdapter();
        menuReceiptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        menuReceiptRecyclerView.setAdapter(menuReceiptAdapter);
        menuReceiptAdapter.setAdapterItem(orderLists);

        String totalPrice = addCommasToNumber(price, 1);

        menuReceiptTotalPrice.setText(totalPrice);
        dialog.show();

        menuReceiptCancel.setOnClickListener(view -> dialog.dismiss());


    }

    private int CART = 0;
    private int RECEIPT = 1;

    public String addCommasToNumber(int price, int type) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String totalPrice = decimalFormat.format(price) + "원";

        if (type == CART) {
            if (price == 0) {
                cartOrderTotalPrice.setText("총 금액 : ");
            } else {
                cartOrderTotalPrice.setText("총 금액 : " + totalPrice);
            }
            return null;
        }
        return totalPrice;
    }


}
