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

import android.graphics.drawable.Drawable;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.room.Room;

import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Adapter.CartAdapter;
import com.example.openbook.Adapter.MenuAdapter;
import com.example.openbook.Adapter.SideListViewAdapter;
import com.example.openbook.AppDatabase;
import com.example.openbook.BuildConfig;
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
import com.example.openbook.KakaoPay;
import com.example.openbook.MenuDao;
import com.example.openbook.MenuData;
import com.example.openbook.MenuListDTO;
import com.example.openbook.RetrofitManager;
import com.example.openbook.RetrofitService;
import com.example.openbook.SaveOrderDeleteData;
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

    JSONArray menujArray;

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
        setContentView(R.layout.menu_activity);


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
//        Intent fcm = new Intent(getApplicationContext(), FCM.class);
//        fcm.putExtra("userId", myData.getId());
//        startService(fcm);

        sendNotification = new SendNotification();

        if (myData.getPaymentStyle().equals("before") && myData.isUsedTable() == false) {
            sendNotification.usingTable(myData.getId(), "사용", myData.getIdentifier());
            Log.d(TAG, "usingTable identifier: " + myData.getIdentifier());
            myData.setUsedTable(true);
        }


        /**
         * AppBar: 로그인하면 table number 바로 나오는거
         */
        TextView tableNumber = findViewById(R.id.appbar_menu_table_number);
        tableNumber.setText(myData.getId());

        menuClose = findViewById(R.id.menu_close);

        if (myData.getPaymentStyle().equals("after")) {
            menuClose.setVisibility(View.GONE);
        }
        appbarMenuTable = findViewById(R.id.appbar_menu_table);
        appbarOrderList = findViewById(R.id.appbar_menu_orderList);


        /**
         * 장바구니
         */
        RecyclerView cartRecyclerview = findViewById(R.id.menu_cart_recyclerview);
        cartRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        cartAdapter = new CartAdapter();

        cartLists = new ArrayList<>();
        cartRecyclerview.setAdapter(cartAdapter);

        /**
         * 액티비티 전환 시 sp에 저장된 장바구니 데이터 가져와서 뿌려주기
         */
        pref = getSharedPreferences("cart_list", MODE_PRIVATE);
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

        dialogManager = new DialogManager();
        progressbar = dialogManager.progressDialog(Menu.this);
        progressbar.show();


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
            while (res.moveToNext()) {
                menuLists.add(new MenuList(res.getString(3),//img
                        res.getString(1), //name
                        res.getInt(2), //price
                        res.getInt(4), //menuType
                        1));
            }
            menuAdapter.setAdapterItem(menuLists);
            progressbar.dismiss();
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


    } // onCreate()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        //액티비티가 사용자에게 보여질 때, 사용자와 상호작용 X

        String returnOrderList = getIntent().getStringExtra("orderList");
        Log.d(TAG, "onStart_returnOrderList : " + returnOrderList);
        Log.d(TAG, "onStart_PaymentStyle :" + myData.getPaymentStyle());

        if (returnOrderList != null) {
            SaveOrderDeleteData orderSave = new SaveOrderDeleteData();
            try {
                boolean success = orderSave.orderSave(returnOrderList);

                if (success == true) {
//                    successOrder();
                    Log.d(TAG, "successOrder 성공: ");
                } else {
                    myData.setOrder(false);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

//        String savedJson = pref.getString("cart_list", null);
//
//        //저장된 cart_list가 존재하면 띄운다..!
//        if (savedJson != null) {
//            cartLists = getSharedPreference(savedJson);
//
//            for (CartList menu : cartLists) {
//                totalPrice += menu.getMenu_price() * menu.getMenu_quantity();
//                Log.d(TAG, "shared price: " + totalPrice);
//                int price = menu.getMenu_price() * menu.getMenu_quantity();
//                menu.setMenu_price(price);
//            }
//
//            String commaTotalPrice = addCommasToNumber(totalPrice);
//
//            cartOrderTotalPrice.setText("합계 : " + commaTotalPrice);
//            cartAdapter.setAdapterItem(cartLists);
//        }

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
            Log.d(TAG, "menuClose Click: ");
            sendNotification.usingTable(myData.getId(), "종료", 0);
            Log.d(TAG, "종료: ");

            editor.remove("cart_list");
            editor.remove("order_list");
            editor.commit();

            Intent intent = new Intent(Menu.this, PaymentSelect.class);
            myData.setPaymentStyle(null);
            myData.setOrder(false);
            myData.setUsedTable(false);
            myData.setIdentifier(0);
            intent.putExtra("myData", myData);
            startActivity(intent);

        });


        /**
         * Appbar: table 클래스로 이동
         */
        appbarMenuTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Table.class);
                intent.putExtra("myData", myData);
                intent.putExtra("chattingData", chattingDataHashMap);
                intent.putExtra("ticketData", ticketDataHashMap);
                intent.putExtra("tableList", tableList);
                startActivity(intent);
            }
        });

        appbarOrderList.setOnClickListener(v -> showReceiptDialog());


        /**
         * 장바구니 클릭 이벤트
         */
        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onPlusClick(View view, int position) {

                int beforePrice;

                if (cartLists.get(position).getMenu_quantity() == 1) {
                    beforePrice = cartLists.get(position).getMenu_price();
                    Log.d(TAG, "beforePrice 1: " + beforePrice);
                } else {
                    beforePrice = cartLists.get(position).getMenu_price() / cartLists.get(position).getMenu_quantity();
                    Log.d(TAG, "beforePrice 2: " + beforePrice);
                }

                int add = cartLists.get(position).getMenu_quantity() + 1;
                cartLists.get(position).setMenu_quantity(add);

                int addPrice = add * beforePrice;
                cartLists.get(position).setMenu_price(addPrice);

                totalPrice = totalPrice + beforePrice;

                String commaTotalPrice = addCommasToNumber(totalPrice);

                cartOrderTotalPrice.setText("합계 : " + commaTotalPrice);
                cartAdapter.setAdapterItem(cartLists);

                cartSharedPreference("cart_list");
            }

            @Override
            public void onMinusClick(View view, int position) {
                int beforePrice;

                if (cartLists.get(position).getMenu_quantity() == 1) {

                    beforePrice = cartLists.get(position).getMenu_price();
                    totalPrice = totalPrice - beforePrice;

                    cartLists.remove(position);

                } else {
                    beforePrice = cartLists.get(position).getMenu_price() / cartLists.get(position).getMenu_quantity();
                    Log.d(TAG, "minus beforePrice: " + beforePrice);

                    int minus = cartLists.get(position).getMenu_quantity() - 1;
                    cartLists.get(position).setMenu_quantity(minus);

                    int minusPrice = minus * beforePrice;
                    cartLists.get(position).setMenu_price(minusPrice);

                    totalPrice = totalPrice - beforePrice;
                }

                if (totalPrice == 0) {
                    cartOrderTotalPrice.setText("");
                } else {
                    String commaTotalPrice = addCommasToNumber(totalPrice);
                    cartOrderTotalPrice.setText("합계 : " + commaTotalPrice);
                }

                cartAdapter.setAdapterItem(cartLists);
                cartSharedPreference("cart_list");
            }


            @Override
            public void onDeleteClick(View view, int position) {
                int delete_price = cartLists.get(position).getMenu_price();

                totalPrice = totalPrice - delete_price;

                if (totalPrice == 0) {
                    cartOrderTotalPrice.setText("");
                } else {
                    String commaTotalPrice = addCommasToNumber(totalPrice);
                    cartOrderTotalPrice.setText("합계: " + commaTotalPrice);
                }


                cartLists.remove(position);

                cartAdapter.setAdapterItem(cartLists);
                cartSharedPreference("cart_list");
            }
        });


        /**
         * 클릭하면 장바구니에 담기게
         */
        menuAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {

            int pos = 1000;
            int menuQuantity;

            @Override
            public void onItemClick(View view, String name, int price, int position) {
                //중복되는 메뉴의 포지션 값 get
                for (int i = 0; i < cartLists.size(); i++) {
                    if (cartLists.get(i).getMenu_name().equals(name)) {
                        pos = i;
                        Log.d(TAG, "pos : " + pos);
                    }
                }

                //포지션 값이 초기값이면 새롭게 추가하고, 아니면 개수만 올려서 다시 적용
                if (pos == 1000) {
                    cartLists.add(new CartList(name, price, 1, 1));
                } else {
                    menuQuantity = cartLists.get(pos).getMenu_quantity() + 1;
                    cartLists.get(pos).setMenu_quantity(menuQuantity);

                    int addPrice = price * menuQuantity;
                    cartLists.get(pos).setMenu_price(addPrice);
                }

                cartAdapter.setAdapterItem(cartLists);

                pos = 1000;

                totalPrice = totalPrice + price;
                Log.d(TAG, "totalPrice: " + totalPrice);

                String commaTotalPrice = addCommasToNumber(totalPrice);

                cartOrderTotalPrice.setText("합계 : " + commaTotalPrice);

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

//        cartOrderButton.setOnClickListener(view -> {
//
//            if (cartLists.size() == 0) {
//                dialogManager.noButtonDialog(Menu.this, "장바구니가 비어있습니다.");
//
//
//            } else {
//                switch (myData.getPaymentStyle()){
//                    case "after" :
//                        // fcm으로 날리고
//                        sendNotification.sendMenu(adminOrderMenuList(cartLists));
//                        orderSharedPreference();
//                        successOrder();
//
//                        if (clientSocket == null) {
//                            clientSocket = new ClientSocket(myData.getId(), Menu.this);
//                            clientSocket.start();
//                        }
//                        break;
//
//                    case "before" :
//                        Intent intent = new Intent(Menu.this, KakaoPay.class);
//                        intent.putExtra("menuName", getOrderMenuName(cartLists));
//                        intent.putExtra("menuPrice", totalPrice);
//                        intent.putExtra("jsonOrderList", getJson(myData.getId(), cartLists, myData.getIdentifier()));
//                        intent.putExtra("myData", myData);
//                        startActivity(intent);
//
//                        orderSharedPreference();
//                        break;
//
//                    default:
//                        Log.d(TAG, "order click : paymentStyle이 없어..");
//                        break;
//                }
//
//                myData.setOrder(true);
//
//
//            } // cartList에 데이터 if-else
//        }); //주문하기 click event


        if (!myData.getId().equals("구글로그인")) {
            myTable = Integer.parseInt(myData.getId().replace("table", ""));
        }


        if (tableList == null) {
            Log.d(TAG, "onResume tableList null: ");

            tableList = new ArrayList<>();

            for (int i = 1; i < myData.getTableFromDB() + 1; i++) {
                if (i == myTable) {
                    tableList.add(new TableList(myData.getId(), (Drawable) null, 0));
                } else {
                    tableList.add(new TableList(i, (Drawable) null, 1));
                }
            }

            Log.d(TAG, "tableList :" + tableList.size());


        } else {
            Log.d(TAG, "onResume tableList not null: ");
        }

    } // onResume

//    public String getOrderMenuName(ArrayList<CartList> cartLists) {
//
//        int menuQuantity = cartLists.size();
//
//        String menuName;
//
//        if (menuQuantity == 1) {
//            menuName = cartLists.get(0).getMenu_name();
//        } else {
//            menuName = cartLists.get(0).getMenu_name() + " 외" + Integer.toString(menuQuantity - 1);
//        }
//        return menuName;
//    }

//    public String adminOrderMenuList(ArrayList<CartList> cartLists) {
//        JSONObject jsonObject = new JSONObject();
//        try {
//
//            menujArray = new JSONArray();//배열이 필요할때
//
//            for (int i = 0; i < cartLists.size(); i++)//배열
//            {
//                //배열 내에 들어갈 json
//                JSONObject sObject = new JSONObject();
//                sObject.put("menu", cartLists.get(i).getMenu_name());
//                sObject.put("price", cartLists.get(i).getMenu_price());
//                sObject.put("quantity", cartLists.get(i).getMenu_quantity());
//                menujArray.put(sObject);
//            }
//
//            jsonObject.put("item", menujArray);
//            jsonObject.put("menuName", getOrderMenuName(cartLists));
//            jsonObject.put("tableName", myData.getId());
//            jsonObject.put("identifier", 0);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        return jsonObject.toString();
//    }

    public void cartSharedPreference(String keyName) {

        JSONArray jsonArray = new JSONArray();

        for (CartList menu : cartLists) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("menu", menu.getMenu_name());
                jsonObject.put("quantity", menu.getMenu_quantity());
                jsonObject.put("price", menu.getMenu_price() / menu.getMenu_quantity());
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
//
//
//    }

//    public ArrayList getSharedPreference(String savedJson) {
//        try {
//            JSONArray jsonArray = new JSONArray(savedJson);
//            for (int i = 0; i < jsonArray.length(); i++) {
//                JSONObject jsonObject = jsonArray.getJSONObject(i);
//                String menuName = jsonObject.getString("menu");
//                int menuQuantity = jsonObject.getInt("quantity");
//                int menuPrice = jsonObject.getInt("price");
//                cartLists.add(new CartList(menuName, menuPrice, menuQuantity, 1));
//            }
//        } catch (JSONException e) {
//
//        }
//        return cartLists;
//    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public String getJson(String get_id, ArrayList<CartList> list, int identifier) {
//        JSONObject obj = new JSONObject();
//        try {
//            menujArray = new JSONArray();//배열이 필요할때
//            for (int i = 0; i < list.size(); i++)//배열
//            {
//                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
//                sObject.put("menu", list.get(i).getMenu_name());
//                sObject.put("price", list.get(i).getMenu_price());
//                sObject.put("quantity", list.get(i).getMenu_quantity());
//                menujArray.put(sObject);
//            }
//            obj.put("table", get_id);
//            obj.put("item", menujArray);//배열을 넣음
//            obj.put("identifier", identifier);
//
//            Log.d(TAG, "getJson: " + obj);
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return obj.toString();
//    }


    public void setMenuList(List<MenuListDTO.MenuItem> menuList) {

        for (MenuListDTO.MenuItem menuItem : menuList) {

            String url = BuildConfig.SERVER_IP + "MenuImages/" + menuItem.getImageURL();
            String menuName = menuItem.getMenuName();
            int price = menuItem.getMenuPrice();
            int category = menuItem.getMenuCategory();

            menuLists.add(new MenuList(url, menuName, price, category, 1));
//            dbHelper.insertMenuData(menuName, price, url, category);

        }

        menuAdapter.notifyDataSetChanged();
        progressbar.dismiss();
    }


    public void successOrder() {

        cartLists = new ArrayList<>();
        cartAdapter.setAdapterItem(cartLists);
        totalPrice = 0;
        cartOrderTotalPrice.setText("");

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

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dlg.dismiss();
            }
        }, 1000);
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
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

        String totalPrice = addCommasToNumber(price);

        menuReceiptTotalPrice.setText(totalPrice);
        dialog.show();

        menuReceiptCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });


    }

    public String addCommasToNumber(int number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number) + "원";
    }

}
