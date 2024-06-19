package com.example.openbook.Activity;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;

import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
    boolean isPayment;
    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;

    TextView appbarMenuTable, appbarOrderList, cartOrderTotalPrice;

    CartAdapter cartAdapter;
    MenuAdapter menuAdapter;
    SideListViewAdapter sideAdapter;

    Button cartOrderButton, useStop;

    RecyclerView menuRecyclerview;

    ClientSocket clientSocket;

    ActivityResultLauncher<Intent> paymentLauncher;

    DBHelper dbHelper;

    SendNotification sendNotification;

    MyData myData;
    HashMap<String, ChattingData> chattingDataHashMap;
    HashMap<String, TicketData> ticketDataHashMap;
    SendToPopUp sendToPopUp = new SendToPopUp();
    RetrofitService service;
    DialogManager dialogManager;
    Dialog progressbar;
    Gson gson;

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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        overridePendingTransition(0, 0);

        dialogManager = new DialogManager();
        progressbar = dialogManager.progressDialog(Menu.this);
        progressbar.show();
        gson = new Gson();


        // 로컬 브로드캐스트 리시버 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("chattingRequestArrived");
        intentFilter.addAction("giftArrived");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        myData = (MyData) getIntent().getSerializableExtra("myData");

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
        Log.d(TAG, "chattingData size: " + chattingDataHashMap);

        ticketDataHashMap = (HashMap<String, TicketData>) getIntent().getSerializableExtra("ticketData");

        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

        isPayment = getIntent().getBooleanExtra("isPayment", false);


        /**
         * 로그인을 성공하면 id, token을 firebase realtime db에 저장
         */

        if (!myData.isFcmExist()) {
            Intent fcm = new Intent(getApplicationContext(), FCM.class);
            fcm.putExtra("identifier", myData.getIdentifier());
            startService(fcm);
        }

        sendNotification = new SendNotification();

        if (myData.getPaymentCategory() == PaymentCategory.NOW && !myData.isUsedTable()) {
            sendNotification.usingTableUpdate("admin", myData.getId(), "PayNow");
            myData.setUsedTable(true);
        } else if (myData.getPaymentCategory() == PaymentCategory.LATER && !myData.isUsedTable()) {
            sendNotification.usingTableUpdate("admin", myData.getId(), "PayLater");
            myData.setUsedTable(true);
        }


        TextView tableName = findViewById(R.id.appbar_menu_table_number);
        tableName.setText(myData.getId());

        useStop = findViewById(R.id.menu_close);

        if (myData.getPaymentCategory() == PaymentCategory.LATER) {
            useStop.setVisibility(View.GONE);
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

        sharedPreference = getSharedPreferences("CustomerData", MODE_PRIVATE);
        editor = sharedPreference.edit();

        cartOrderTotalPrice = findViewById(R.id.cart_order_total_price);
        cartOrderButton = findViewById(R.id.cart_order_button);

        cartAdapter = new CartAdapter();
        cartLists = new ArrayList<>();
        setCartItems();
        cartRecyclerview.setAdapter(cartAdapter);

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
                new Handler(Looper.getMainLooper()).postDelayed(() -> runOnUiThread(() -> progressbar.dismiss()), 1000);

            }
        });

        int version = 1;
        version++;

        dbHelper = new DBHelper(Menu.this, version);
        Cursor res = dbHelper.getTableData("menuListTable");


        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

        if (res == null) {
            Log.d(TAG, "메뉴 db 새로 받아오기");

            Call<MenuListDTO> call = service.getMenuList();
            call.enqueue(new Callback<MenuListDTO>() {
                @Override
                public void onResponse(@NonNull Call<MenuListDTO> call, @NonNull Response<MenuListDTO> response) {
                    Log.d(TAG, "onResponse: " + response);
                    if (response.isSuccessful()) {
                        switch (response.body().getResult()) {
                            case "success":
                                Log.d(TAG, "onResponse itemList: " + response.body().getItemList());
                                setMenuList(response.body().getItemList());
                                break;
                            case "failed":
                                progressbar.dismiss();
                                Toast.makeText(Menu.this, "가져올 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    } else {
                        progressbar.dismiss();
                        Toast.makeText(Menu.this, R.string.networkError, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<MenuListDTO> call, @NonNull Throwable t) {
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

                runOnUiThread(() -> menuAdapter.setAdapterItem(menuLists));
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


    } // onCreate()


    @Override
    protected void onStart() {
        super.onStart();
        //액티비티가 사용자에게 보여질 때, 사용자와 상호작용 X
        if (isPayment) {
            Log.d(TAG, "isPayment: " + isPayment);
            sendNotification.sendMenu(submitOrder(), result -> {
                if (result.equals("success")) {
                    orderSharedPreference();
                    successOrder();
                }
            });
        } else {
            Log.d(TAG, "isPayment: " + isPayment);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        //사용자와 상호작용 하는 단계, 어플 기능은 여기서

        IntentFilter intentFilter = new IntentFilter("chattingRequestArrived");
        LocalBroadcastManager.getInstance(Menu.this).registerReceiver(broadcastReceiver, intentFilter);

        useStop.setOnClickListener(view -> {
            sendNotification.usingTableUpdate("admin", myData.getId(), "End");
            Intent intent = new Intent(Menu.this, PaymentSelect.class);
            myData.init();
            intent.putExtra("myData", myData);
            startActivity(intent);
        });


        /**
         * Appbar: table 클래스로 이동
         */
        appbarMenuTable.setOnClickListener(view -> {
            Intent intent = new Intent(Menu.this, Table.class);
            intent.putExtra("myData", myData);
            intent.putExtra("chattingData", chattingDataHashMap);
            intent.putExtra("ticketData", ticketDataHashMap);
            intent.putExtra("tableList", tableList);
            startActivity(intent);
        });


        appbarOrderList.setOnClickListener(view -> {
            Pair<ArrayList<OrderList>, String> pair = getReceiptData();
            dialogManager.showReceiptDialog(Menu.this, pair.first, pair.second).show();
        });


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

                editor.putString("cartItems", gson.toJson(cartLists));
                editor.commit();
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

                editor.putString("cartItems", gson.toJson(cartLists));
                editor.commit();
            }


            @Override
            public void onDeleteClick(View view, int position) {
                int deletePrice = cartLists.get(position).getMenuPrice();

                totalPrice = totalPrice - deletePrice;

                addCommasToNumber(totalPrice, 0);

                cartLists.remove(position);
                cartAdapter.notifyItemRemoved(position);

                editor.putString("cartItems", gson.toJson(cartLists));
                editor.commit();
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

                editor.putString("cartItems", gson.toJson(cartLists));
                editor.commit();

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
                switch (myData.getPaymentCategory()) {
                    case LATER:
                        sendNotification.sendMenu(submitOrder(), result -> {
                            if (result.equals("success")) {
                                orderSharedPreference();
                                successOrder();
                            } else {
                                Toast.makeText(Menu.this, getResources().getString(R.string.menuOrderError), Toast.LENGTH_SHORT).show();
                            }
                        });

                        if (clientSocket == null || !clientSocket.isAlive()) {
                            clientSocket = new ClientSocket(myData.getId(), Menu.this);
                            clientSocket.start();
                            Log.d(TAG, "clientSocket Start: " + clientSocket);
                        }
                        break;

                    case NOW:
                        Intent intent = new Intent(Menu.this, KakaoPay.class);
                        intent.putExtra("orderItemName", getOrderItemName());
                        intent.putExtra("totalPrice", totalPrice);
                        intent.putExtra("myData", myData);
                        intent.putExtra("orderItems", getOrderList());
                        startActivity(intent);
//                                paymentLauncher.launch(intent);


                        break;

                    default:
                        Log.d(TAG, "order click : paymentStyle이 없어..");
                        break;
                }
                myData.setOrder(true);
            }
        });


        if (!myData.getId().equals("구글로그인")) {
            myTable = Integer.parseInt(myData.getId().replace("table", ""));
        }
    } // onResume

    public String getOrderItemName() {

        String orderItemName = cartLists.stream()
                .map(cartItem -> cartItem.getMenuName() + " " + cartItem.getMenuQuantity() + "개")
                .collect(Collectors.joining(", "));

        Log.d(TAG, "getOrderItemName: " + orderItemName);

        return orderItemName;
    }

    public Map<String, String> submitOrder() {

        Map<String, String> request = new HashMap<>();
        request.put("request", "Order");
        request.put("tableName", myData.getId());
        request.put("orderItemName", getOrderItemName());
        request.put("totalPrice", String.valueOf(totalPrice));
        request.put("items", getOrderList());

        return request;
    }


    private void orderSharedPreference() {
        String orderItems = sharedPreference.getString("orderItems", null);
        Log.d(TAG, "orderSharedPreference: " + orderItems);

        //shared에 저장된 내용이 있으면 기존값에 추가해서 저장
        if (orderItems != null && !orderItems.isEmpty()) {
            Type type = new TypeToken<ArrayList<CartList>>() {
            }.getType();
            ArrayList<CartList> orderLists = gson.fromJson(orderItems, type);

            boolean found = false;
            for (int i = 0; i < cartLists.size(); i++) {
                CartList cartItem = cartLists.get(i);

                for (int j = 0; j < orderLists.size(); j++) {
                    if (orderLists.get(j).getMenuName().equals(cartItem.getMenuName())) {

                        int oldQuantity = orderLists.get(j).getMenuQuantity();
                        int newQuantity = oldQuantity + cartItem.getMenuQuantity();
                        orderLists.get(j).setMenuQuantity(newQuantity);

                        int oldPrice = orderLists.get(j).getMenuPrice();
                        int newPrice = oldPrice + cartItem.getMenuPrice();
                        orderLists.get(j).setMenuPrice(newPrice);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    orderLists.add(new CartList(cartItem.getMenuName(),
                            cartItem.getMenuPrice(),
                            cartItem.getMenuQuantity(),
                            cartItem.getOriginalPrice(),
                            cartItem.getCartCategory()));
                }
                found = false;
            }

            editor.putString("orderItems", gson.toJson(orderLists));
            editor.commit();
        } else {
            editor.putString("orderItems", gson.toJson(cartLists));
            editor.commit();
        }

    }

    public void setCartItems() {

        String cartItems = sharedPreference.getString("cartItems", null);

        if (cartItems != null) {
            Type type = new TypeToken<ArrayList<CartList>>() {
            }.getType();
            cartLists = gson.fromJson(cartItems, type);

            if (cartLists != null) {
                for (CartList menu : cartLists) {
                    int price = menu.getMenuPrice() * menu.getMenuQuantity();
                    totalPrice += price;
                    Log.d(TAG, "shared price: " + totalPrice);
                    menu.setMenuPrice(price);
                }
                addCommasToNumber(totalPrice, 0);
                cartAdapter.setAdapterItem(cartLists);
            }
        }
    }


    public String getOrderList() {

        JsonArray menuItems = new JsonArray();

        for (CartList item : cartLists) {
            JsonObject menuItem = new JsonObject();

            menuItem.addProperty("menuName", item.getMenuName());
            menuItem.addProperty("menuPrice", String.valueOf(item.getMenuPrice()));
            menuItem.addProperty("menuQuantity", String.valueOf(item.getMenuQuantity()));
            menuItem.addProperty("menuCategory", item.getCartCategory().getValue());
            menuItems.add(menuItem);
        }

        Log.d(TAG, "orderList: " + menuItems);
        return gson.toJson(menuItems);
    }


    public void setMenuList(List<MenuListDTO.MenuItem> menuList) {

        for (MenuListDTO.MenuItem menuItem : menuList) {

            String url = BuildConfig.SERVER_IP + "menuImages/" + menuItem.getImageURL();
            String menuName = menuItem.getMenuName();
            int price = menuItem.getMenuPrice();
            int category = menuItem.getMenuCategory();

            menuLists.add(new MenuList(url, menuName, price, category, 1));
            dbHelper.insertMenuData(menuName, price, url, category);

        }
        menuAdapter.setAdapterItem(menuLists);
        Log.d(TAG, "setMenuList setAdapter: ");

    }


    public void successOrder() {

        cartLists = new ArrayList<>();
        cartAdapter.setAdapterItem(cartLists);
        totalPrice = 0;
        cartOrderTotalPrice.setText("");

        editor.remove("cartItems");
        editor.commit();

        dialogManager.successOrder(Menu.this).show();
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
    }

    private Pair<ArrayList<OrderList>, String> getReceiptData() {
        Dialog dialog = new Dialog(Menu.this);
        dialog.setContentView(R.layout.receipt_dialog);

        ArrayList<OrderList> orderLists = new ArrayList<>();

        String sharedOrderList = sharedPreference.getString("orderItems", null);
        Log.d(TAG, "showReceiptDialog orderList: " + sharedOrderList);
        int price = 0;

        if (sharedOrderList != null) {

            JsonArray jsonArray = gson.fromJson(sharedOrderList, JsonArray.class);

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                orderLists.add(new OrderList(myData.getPaymentCategory().getValue(),
                        myData.getId(),
                        jsonObject.get("menuName").getAsString(),
                        jsonObject.get("menuQuantity").getAsInt(),
                        jsonObject.get("menuPrice").getAsInt()));

                price = price + jsonObject.get("menuPrice").getAsInt();

            }

        } else {
            orderLists.add(new OrderList(myData.getPaymentCategory().getValue(),
                    myData.getId(),
                    "주문 내역이 없습니다."));
        }


        String totalPrice = addCommasToNumber(price, 1);

        return Pair.create(orderLists, totalPrice);

    }


    public String addCommasToNumber(int price, int type) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        String totalPrice = decimalFormat.format(price) + "원";

        int CART = 0;
        if (type == CART) {
            if (price == 0) {
                cartOrderTotalPrice.setText("");
            } else {
                cartOrderTotalPrice.setText("총 금액 : " + totalPrice);
            }
            return null;
        }
        return totalPrice;
    }


}
