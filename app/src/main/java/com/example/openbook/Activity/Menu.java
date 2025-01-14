package com.example.openbook.Activity;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


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
import com.example.openbook.Category.CartCategory;
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.DBHelper;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.OrderList;
import com.example.openbook.Data.SideList;
import com.example.openbook.Deco.menu_recyclerview_deco;
import com.example.openbook.DialogManager;
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.Category.PaymentCategory;
import com.example.openbook.InactivityManager;
import com.example.openbook.ManageOrderItems;
import com.example.openbook.TableDataManager;
import com.example.openbook.kakaopay.KakaoPay;
import com.example.openbook.retrofit.MenuListDTO;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.R;
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

    TextView appbarMenuTable, appbarOrderList, cartOrderTotalPrice, menuNewChat;

    CartAdapter cartAdapter;
    MenuAdapter menuAdapter;
    SideListViewAdapter sideAdapter;

    Button cartOrderButton, useStop;

    RecyclerView menuRecyclerview;

    ClientSocket clientSocket;

    DBHelper dbHelper;

    SendNotification sendNotification;

    MyData myData;
    RetrofitService service;
    DialogManager dialogManager;
    Dialog progressbar;
    Gson gson;
    TableDataManager tableDataManager;
    String newChatTable;
    ManageOrderItems manageOrderItems;
    InactivityManager inactivityManager;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "giftArrived":
                    Log.d(TAG, "onReceive: " + intent.getAction());
                    String from = intent.getStringExtra("from");
                    String menuItem = intent.getStringExtra("menuItem");
//                    String count = intent.getStringExtra("count");

                    dialogManager.giftReceiveDialog(Menu.this, myData.getId(), from, menuItem).show();
                    break;

                case "newChatArrived":
                    newChatTable = intent.getStringExtra("newChatTable");
                    if (newChatTable != null) {
                        notifyNewChat(newChatTable);
                    }
                    break;

                case "isGiftAccept":
                    Log.d(TAG, "onReceive: " + intent.getAction());
                    from = intent.getStringExtra("from");

                    boolean isAccept = intent.getBooleanExtra("isAccept", false);

                    String message;
                    if (isAccept) {
                        message = from + "에서 선물을 수락하였습니다.";
                    } else {
                        message = from + "에서 선물을 거절하였습니다.";
                    }
                    dialogManager.positiveBtnDialog(Menu.this, message).show();
                    break;

                case "CompletePayment":
                    String tid = intent.getStringExtra("tid");

                    if (tid != null && !tid.isEmpty()) {

                        String chatMessages = dbHelper.getChatting(myData.getId());

                        if (chatMessages != null && !chatMessages.isBlank()) {
                            tableDataManager.saveChatMessages
                                    (myData.getId(),
                                            chatMessages,
                                            tid, service,
                                            chatResult -> {
                                                if (chatResult.equals("success")) {
                                                    dbHelper.deleteAllChatMessages(); //삭제
                                                    tableDataManager.setUseStop(Menu.this, myData);
                                                    tableDataManager.stopSocket(Menu.this, myData.getId());
                                                }
                                            });
                        } else {
                            tableDataManager.setUseStop(Menu.this, myData);
                            tableDataManager.stopSocket(Menu.this, myData.getId());
                        }
                    }
                    break;
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

        myData = (MyData) getIntent().getSerializableExtra("myData");

        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

        isPayment = getIntent().getBooleanExtra("isPayment", false);

        tableDataManager = new TableDataManager();
        tableDataManager.hideSystemUI(this);

        manageOrderItems = new ManageOrderItems();

        sharedPreference = getSharedPreferences("CustomerData", MODE_PRIVATE);
        editor = sharedPreference.edit();

        if (!myData.isFcmExist()) {
            tableDataManager.setFcmService(myData.getIdentifier());
            myData.setFcmExist(true);

            editor.putBoolean("isFcmExist", true);
            editor.commit();
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
        menuNewChat = findViewById(R.id.menu_new_chat);

        RecyclerView cartRecyclerview = findViewById(R.id.menu_cart_recyclerview);
        cartRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        RecyclerView.ItemAnimator animator = cartRecyclerview.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        cartOrderTotalPrice = findViewById(R.id.cart_order_total_price);
        cartOrderButton = findViewById(R.id.cart_order_button);

        cartAdapter = new CartAdapter();
        cartLists = new ArrayList<>();
        setCartItems();
        cartRecyclerview.setAdapter(cartAdapter);

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


        dbHelper = new DBHelper(Menu.this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        dbHelper.onCreate(db);
        Cursor res = dbHelper.getTableData("menuListTable");


        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

        if (res.getCount() == 0) {
            Log.d(TAG, "메뉴 db 새로 받아오기");

            Call<MenuListDTO> call = service.getMenuList();
            call.enqueue(new Callback<>() {
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
                    progressbar.dismiss();
                    Toast.makeText(Menu.this, R.string.networkError, Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Log.d(TAG, "메뉴db 있는거 사용");
            while (res.moveToNext()) {
                menuLists.add(new MenuList(res.getString(3),//img
                        res.getString(1), //name
                        res.getInt(2), //price
                        res.getInt(4) //menuType
                ));
            }
            menuAdapter.setAdapterItem(menuLists);
        }

        menuNavigation = findViewById(R.id.menu_navigation);
        sideAdapter = new SideListViewAdapter();

        String[] sideList = getResources().getStringArray(R.array.sideMenu);

        for (String side : sideList) {
            sideAdapter.addItem(new SideList(side));
        }
        menuNavigation.setAdapter(sideAdapter);

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isPayment) {
            Log.d(TAG, "isPayment: " + isPayment);
            sendNotification.sendMenu(sendOrderToAdmin(), result -> {
                progressbar = dialogManager.progressDialog(Menu.this);
                progressbar.show();
                if (result.equals("success")) {
                    manageOrderItems.saveOrderedItems(Menu.this, cartLists);
                    successOrder();
                    progressbar.dismiss();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.notOrder), Toast.LENGTH_SHORT).show();
                    progressbar.dismiss();
                }
            });
        } else {
            Log.d(TAG, "isPayment: " + isPayment);
        }

    }

    public void notifyNewChat(String tableName) {
        Animation shakeAndTranslate = AnimationUtils.loadAnimation(this, R.anim.up_and_shake);
        String notify = tableName + "\n채팅 알림";
        menuNewChat.setText(notify);
        menuNewChat.setVisibility(View.VISIBLE);
        menuNewChat.startAnimation(shakeAndTranslate);
    }

    @Override
    protected void onStop() {
        super.onStop();
        inactivityManager.stopTimer();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("giftArrived");
        intentFilter.addAction("isGiftAccept");
        intentFilter.addAction("CompletePayment");
        intentFilter.addAction("newChatArrived");
        LocalBroadcastManager.getInstance(Menu.this).registerReceiver(broadcastReceiver, intentFilter);

        inactivityManager = new InactivityManager(this, myData, tableList);
        inactivityManager.startInactivityTimer();

        useStop.setOnClickListener(view -> {
            sendNotification.usingTableUpdate("admin", myData.getId(), "End");
            tableDataManager.setUseStop(Menu.this, myData);
        });

        appbarMenuTable.setOnClickListener(view -> moveToOtherActivity(Table.class));

        appbarOrderList.setOnClickListener(view -> {
            if (myData.isOrder()) {
                Pair<ArrayList<OrderList>, String> pair = manageOrderItems.getReceiptData(this, myData);
                Log.d(TAG, "orderItem: " + pair.second);
                dialogManager.showReceiptDialog(this, pair.first, pair.second).show();
            } else {
                Toast.makeText(this, "주문 내역이 없습니다.", Toast.LENGTH_SHORT).show();
            }

        });


        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onPlusClick(View view, int position) {

                int originalPrice = cartLists.get(position).getOriginalPrice();
                int quantity = cartLists.get(position).getMenuQuantity() + 1;

                int newPrice = originalPrice * quantity;

                cartLists.get(position).setMenuQuantity(quantity);
                cartLists.get(position).setMenuPrice(newPrice);

                totalPrice = totalPrice + newPrice;
                cartOrderTotalPrice.setText(manageOrderItems.addCommasToNumber(totalPrice, 0));

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
                cartOrderTotalPrice.setText(manageOrderItems.addCommasToNumber(totalPrice, 0));

                editor.putString("cartItems", gson.toJson(cartLists));
                editor.commit();
            }


            @Override
            public void onDeleteClick(View view, int position) {
                int deletePrice = cartLists.get(position).getMenuPrice();

                totalPrice = totalPrice - deletePrice;

                cartOrderTotalPrice.setText(manageOrderItems.addCommasToNumber(totalPrice, 0));

                cartLists.remove(position);
                cartAdapter.notifyItemRemoved(position);

                editor.putString("cartItems", gson.toJson(cartLists));
                editor.commit();
            }
        });


        menuAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {
            boolean menuExist = false;

            @Override
            public void onItemClick(View view, String name, int price, int category, int position) {
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
                    cartLists.add(new CartList(name, price, 1,
                            price, category, CartCategory.MENU));
                    cartAdapter.setAdapterItem(cartLists);
                }

                menuExist = false;

                totalPrice = totalPrice + price;
                cartOrderTotalPrice.setText(manageOrderItems.addCommasToNumber(totalPrice, 0));

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
                    moveToOtherActivity(CallServer.class);
                    break;
            }

        });


        cartOrderButton.setOnClickListener(view -> {

            if (cartLists.size() == 0) {
                dialogManager.noButtonDialog(Menu.this, "장바구니가 비어있습니다.");

            } else {
                switch (myData.getPaymentCategory()) {
                    case LATER:
                        progressbar = dialogManager.progressDialog(this);
                        progressbar.show();

                        sendNotification.sendMenu(sendOrderToAdmin(), result -> {
                            if (result.equals("success")) {
                                manageOrderItems.saveOrderedItems(Menu.this, cartLists);
                                successOrder();
                                progressbar.dismiss();

                                if (clientSocket == null || !clientSocket.isAlive()) {
                                    clientSocket = new ClientSocket(myData.getId(), Menu.this);
                                    clientSocket.start();
                                    Log.d(TAG, "clientSocket Start: " + clientSocket);
                                }

                            } else {
                                progressbar.dismiss();
                                Toast.makeText(Menu.this, getResources().getString(R.string.menuOrderError), Toast.LENGTH_SHORT).show();
                            }
                        });

                        break;

                    case NOW:
                        Intent intent = new Intent(Menu.this, KakaoPay.class);
                        intent.putExtra("orderItemName", getOrderItemName());
                        intent.putExtra("totalPrice", totalPrice);
                        intent.putExtra("myData", myData);
                        intent.putExtra("orderItems", getCartList());
                        startActivity(intent);
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

        menuNewChat.setOnClickListener(view -> {
            menuNewChat.setVisibility(View.GONE);
            int newChatTableNumber = Integer.parseInt(newChatTable.replace("table", ""));
            newChatTable = null;

            Intent intent = new Intent(Menu.this, Chatting.class);
            intent.putExtra("myData", myData);
            intent.putExtra("tableList", tableList);
            intent.putExtra("tableNumber", newChatTableNumber);
            startActivity(intent);
        });
    }

    public String getOrderItemName() {

        String orderItemName = cartLists.stream()
                .map(cartItem -> cartItem.getMenuName() + " " + cartItem.getMenuQuantity() + "개")
                .collect(Collectors.joining(", "));

        Log.d(TAG, "getOrderItemName: " + orderItemName);

        return orderItemName;
    }

    public Map<String, String> sendOrderToAdmin() {

        Map<String, String> request = new HashMap<>();
        request.put("request", "Order");
        request.put("tableName", myData.getId());
        request.put("totalPrice", String.valueOf(totalPrice));
        request.put("items", getCartList());

        return request;
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
                cartOrderTotalPrice.setText(manageOrderItems.addCommasToNumber(totalPrice, 0));
                cartAdapter.setAdapterItem(cartLists);
            }
        }
    }


    public String getCartList() {

        JsonArray menuItems = new JsonArray();

        for (CartList item : cartLists) {
            JsonObject menuItem = new JsonObject();

            menuItem.addProperty("menuName", item.getMenuName());
            menuItem.addProperty("menuPrice", String.valueOf(item.getMenuPrice()));
            menuItem.addProperty("menuQuantity", String.valueOf(item.getMenuQuantity()));
            menuItem.addProperty("menuCategory", item.getMenuCategory());
            menuItems.add(menuItem);
        }

        Log.d(TAG, "get CartList: " + gson.toJson(menuItems));
        return gson.toJson(menuItems);
    }


    public void setMenuList(List<MenuListDTO.MenuItem> menuList) {

        for (MenuListDTO.MenuItem menuItem : menuList) {

            String url = BuildConfig.SERVER_IP + "menuImages/" + menuItem.getImageURL();
            String menuName = menuItem.getMenuName();
            int price = menuItem.getMenuPrice();
            int category = menuItem.getMenuCategory();

            menuLists.add(new MenuList(url, menuName, price, category));
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
    }


    public void moveToOtherActivity(Class<?> activity) {
        Intent intent = new Intent(Menu.this, activity);
        intent.putExtra("myData", myData);
        intent.putExtra("tableList", tableList);
        startActivity(intent);
        finish();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        inactivityManager.resetInactivityTimer();
        return super.onTouchEvent(event);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
