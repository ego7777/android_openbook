package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminTableAdapter;
import com.example.openbook.Category.CartCategory;
import com.example.openbook.Data.AdminData;
import com.example.openbook.BuildConfig;
import com.example.openbook.Data.CartList;
import com.example.openbook.Category.PaymentCategory;
import com.example.openbook.kakaopay.KakaoPay;
import com.example.openbook.retrofit.AdminTableDTO;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.DialogManager;
import com.example.openbook.FCM.FCM;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;

import com.example.openbook.R;
import com.example.openbook.retrofit.TableInformationDTO;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class Admin extends AppCompatActivity {

    String TAG = "AdminTAG";
    AdminTableAdapter adapter;

    TextView appbarAdminSales, appbarAdminAddMenu, appbarAdminModifyTable;
    TextView adminSidebarMenu, adminSidebarInfo, adminSidebarPay;

    String menuName;
    int totalPrice, tableIdentifier;
    boolean isPayment;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    AdminData adminData;
    RetrofitService service;
    ArrayList<AdminTableList> adminTableLists;
    ArrayList<OrderList> orderLists;
    Gson gson;
    String tableRequest;
    DialogManager dialogManager;
    Dialog popUpDialog;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case "tableRequest":
                    String fcmData = intent.getStringExtra("fcmData");
                    Log.d(TAG, "onReceive tableRequest: " + fcmData);
                    if (fcmData != null) {

                        JsonObject requestJson = gson.fromJson(fcmData, JsonObject.class);
                        Log.d(TAG, "requestJson: " + requestJson);
                        String request = requestJson.get("request").getAsString();
                        String tableName = requestJson.get("tableName").getAsString();
                        int tableNumber = Integer.parseInt(tableName.replace("table", "")) - 1;

                        switch (request) {
                            case "PayNow":
                            case "End":
                            case "PayLater":
                                updateTable(request, tableName, tableNumber);
                                break;
                            case "Order":
                                String items = requestJson.get("items").getAsString();
                                JsonArray jsonArray = gson.fromJson(items, JsonArray.class);
                                String orderItemName = requestJson.get("orderItemName").getAsString();
                                int totalPrice = requestJson.get("totalPrice").getAsInt();
                                orderMenu(tableName, tableNumber, orderItemName, jsonArray, totalPrice);
                                break;

                            case "GiftMenuOrder":
                                items = requestJson.get("items").getAsString();
                                jsonArray = gson.fromJson(items, JsonArray.class);
                                String from = requestJson.get("fromTable").getAsString();
                                orderItemName = requestJson.get("orderItemName").getAsString();
                                orderMenu(tableName, tableNumber, orderItemName , jsonArray, 0);


                        }
                    }

                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        overridePendingTransition(0, 0);

        adminData = getIntent().getParcelableExtra("adminData");
        Log.d(TAG, "adminData: " + adminData);
        isPayment = getIntent().getBooleanExtra("isPayment", false);

        adminTableLists = new ArrayList<>();
        orderLists = new ArrayList<>();

        sharedPreference = getSharedPreferences("AdminInfo", MODE_PRIVATE);
        editor = sharedPreference.edit();
        gson = new Gson();

        if (adminData == null || adminData.getId() == null) {
            String id = sharedPreference.getString("id", null);
            adminData = new AdminData(id, null, false);
            Log.d(TAG, "id null admin: " + adminData.getId());
        }

        RecyclerView tableGrid = findViewById(R.id.admin_grid);
        adapter = new AdminTableAdapter();
        tableGrid.setLayoutManager(new GridLayoutManager(this, 3));
        tableGrid.setAdapter(adapter);


        if (adminData.getAdminTableLists() != null) {
            Log.d(TAG, "adminTableList size : " + adminData.getAdminTableLists().size());
            adminTableLists = adminData.getAdminTableLists();
            adapter.setAdapterItem(adminTableLists);

        } else {
            adminTableLists = loadAdminTableList();
            adminData.setAdminTableLists(adminTableLists);
            adapter.setAdapterItem(adminTableLists);
            Log.d(TAG, "loadAdminTableList Adapter");
        }

        orderLists = loadOrderList();

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

        dialogManager = new DialogManager();


//            int table = tableQuantity.getTableQuantity();
//            Log.d(TAG, "tableQuantity : " + table);

//            for (int i = 1; i < table + 1; i++) {
//
//                String summary = sharedPreference.getString("table" + i + "menu", null);
//                Log.d(TAG, "summary: " + summary);
//
//                int price = sharedPreference.getInt("table" + i + "price", 0);
//                Log.d(TAG, "price: " + price);
//
//                String gender = sharedPreference.getString("table" + i + "gender", null);
//                Log.d(TAG, "gender: " + gender);
//
//                String guestNumber = sharedPreference.getString("table" + i + "guestNumber", null);
//                Log.d(TAG, "guestNumber: " + guestNumber);
//
//                int viewType = sharedPreference.getInt("table" + i + "viewType", 0);
//                Log.d(TAG, "viewType: " + viewType);
//
//                int identifier = sharedPreference.getInt("table" + i + "tableIdentifier", 0);
//                Log.d(TAG, "identifier: " + identifier);
//
//
//                if (summary != null) {
//                    adminTableList.add(new AdminTableList("table" + i,
//                            summary, String.valueOf(price), gender, guestNumber, viewType, identifier));
//
//                } else {
//                    adminTableList.add(new AdminTableList("table" + i,
//                            null,
//                            null,
//                            null,
//                            null,0, 0));
//                }
//
//            }// for문 끝


        /**
         * 로그인을 성공하면 id, token을 firebase realtime db에 저장
         */
        if (!adminData.isFcmExist()) {
            boolean fcmExist = sharedPreference.getBoolean("isFcmExist", false);

            if (!fcmExist) {
                FCM fcm = new FCM();
                fcm.getToken(adminData.getId().hashCode());
                adminData.setFcmExist(true);

                editor.putBoolean("isFcmExist", true);
                editor.commit();
            }
        }


        ImageView appbarAdminGear = findViewById(R.id.appbar_admin_gear);
        appbarAdminGear.setVisibility(View.INVISIBLE);

        appbarAdminSales = findViewById(R.id.appbar_admin_sales);
        appbarAdminAddMenu = findViewById(R.id.appbar_admin_addMenu);
        appbarAdminModifyTable = findViewById(R.id.appbar_admin_modifyTable);

        adminSidebarMenu = findViewById(R.id.admin_sidebar_menu);
        adminSidebarInfo = findViewById(R.id.admin_sidebar_info);
        adminSidebarPay = findViewById(R.id.admin_sidebar_pay);

    }


    @Override
    protected void onStart() {
        super.onStart();

        if (menuName != null) {
            int pastCount = Integer.parseInt(String.valueOf(menuName.indexOf(menuName.length())));
            Log.d(TAG, "pastCount: " + pastCount);

        }
        menuName = getIntent().getStringExtra("menuName");

//        if (afterPaymentList != null) {
//            SaveOrderDeleteData orderSaveDeleteData = new SaveOrderDeleteData();
//            //저장하고,
//
//            try {
//                boolean success = orderSaveDeleteData.orderSave(afterPaymentList);
//
//                Log.d(TAG, "success: " + success);
//
//                if (success) {
////                    deleteLocalData();
//                    Log.d(TAG, "deleteData: ");
//
//                    orderSaveDeleteData.deleteServerData(tableName); // 서버 데이터
//                    Log.d(TAG, "deleteServerData: ");
//                }
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tableRequest");
        LocalBroadcastManager.getInstance(Admin.this).registerReceiver(broadcastReceiver, intentFilter);

        appbarAdminSales.setOnClickListener(v -> {
            //매출 액티비티가 나온다
            startActivityClass(AdminSales.class);
        });


        appbarAdminAddMenu.setOnClickListener(view -> dialogManager.addMenu(Admin.this).show());
        appbarAdminModifyTable.setOnClickListener(view -> startActivityClass(AdminModifyTableQuantity.class));

        adapter.setOnItemClickListener((view, position) -> {

            adminSidebarMenu.setOnClickListener(v -> {

                if (adminData.getAdminTableLists().get(position).getAdminTablePrice() != null ||
                        adminData.getAdminTableLists().get(position).getAdminTableStatement() != null) {

                    Pair<ArrayList<OrderList>, String> pair = getReceiptData(position);
                    Log.d(TAG, "receipt: " + gson.toJson(pair.first));
                    dialogManager.showReceiptDialog(this, pair.first, pair.second).show();
                } else {
                    dialogManager.noButtonDialog(Admin.this, getResources().getString(R.string.unusableTable));

                }
            });

            adminSidebarInfo.setOnClickListener(v -> requestTableInfo(position + 1));

            adminSidebarPay.setOnClickListener(v -> {

                AdminTableList table = adminData.getAdminTableLists().get(position);

                if (table.getAdminTablePrice() != null) {
                    String tableName = table.getAdminTableNumber();
                    String tablePrice = table.getAdminTablePrice();
                    totalPrice = removeCommas(tablePrice);

                    Intent intent = new Intent(Admin.this, KakaoPay.class);
                    intent.putExtra("orderItemName", getOrderItemName(tableName));
                    intent.putExtra("totalPrice", totalPrice);
                    intent.putExtra("tableName", tableName);
                    intent.putExtra("orderItems", getOrderList(position));
//                    intent.putExtra("paymentStyle", PaymentCategory.LATER);
                    startActivity(intent);
                } else {
                    dialogManager.noButtonDialog(Admin.this, getResources().getString(R.string.unusableTable));

                }
            });
        });

    }

    public String getOrderList(int position) {
        String clickTable = adminTableLists.get(position).getAdminTableNumber();
        String sharedOrderList = sharedPreference.getString(clickTable, null);

        if (sharedOrderList != null) {

            JsonArray menuItems = gson.fromJson(sharedOrderList, JsonArray.class);
            return gson.toJson(menuItems);

        } else {
            return null;
        }

    }


    public Pair<ArrayList<OrderList>, String> getReceiptData(int position) {

        ArrayList<OrderList> orderLists = new ArrayList<>();
        String clickTable = adminTableLists.get(position).getAdminTableNumber();
        String sharedOrderList = sharedPreference.getString(clickTable, null);

        int price = 0;

        if (sharedOrderList != null) {

            JsonArray jsonArray = gson.fromJson(sharedOrderList, JsonArray.class);

            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                orderLists.add(new OrderList(adminTableLists.get(position).getPaymentType(),
                        clickTable,
                        jsonObject.get("menuName").getAsString(),
                        jsonObject.get("menuQuantity").getAsInt(),
                        jsonObject.get("menuPrice").getAsInt()));

                price = price + jsonObject.get("menuPrice").getAsInt();

            }

        } else {
            orderLists.add(new OrderList(adminTableLists.get(position).getPaymentType(),
                    clickTable,
                    "주문 내역이 없습니다."));
        }

        String totalPrice = addCommasToNumber(price);
//        Log.d(TAG, "getReceiptData: " + gson.toJson(orderLists));

        return Pair.create(orderLists, totalPrice);
    }


    public void startActivityClass(Class activity) {
        Intent intent = new Intent(Admin.this, activity);
        intent.putExtra("adminData", adminData);
        startActivity(intent);
    }

    public String addCommasToNumber(int number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number) + "원";
    }

    public int removeCommas(String price) {
        String remove = price.replace(",", "");
        return Integer.parseInt(remove.replace("원", ""));
    }

    public String getOrderItemName(String tableName) {
        String orderItems = sharedPreference.getString(tableName, null);
        Type type = new TypeToken<ArrayList<CartList>>() {
        }.getType();
        ArrayList<CartList> orderLists = gson.fromJson(orderItems, type);

        String orderItemName = orderLists.stream()
                .map(cartItem -> cartItem.getMenuName() + " " + cartItem.getMenuQuantity() + "개")
                .collect(Collectors.joining(", "));

        Log.d(TAG, "getOrderItemName: " + orderItemName);

        return orderItemName;
    }


//    public void deleteLocalData() {
//
//        runOnUiThread(() -> {
//
//            //서버에서도 데이터를 이동하는 것이 좋겠다...! 채팅 데이터를 서버로 보내야함 여기서 + 얘기만(gender)
//
//            Log.d(TAG, "run: " + tableName);
//            dbHelper.deleteTableData(tableName, "adminTableList", "tableName"); //sqlite에서 지우고
//            editor.remove(tableName + "price"); //s.p에서도 지움
//            editor.remove(tableName + "menu");
//            editor.remove(tableName + "gender");
//            editor.remove(tableName + "guestName");
//            editor.commit();
//
//            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
//            Log.d(TAG, "tableNameInt: " + tableNameInt);
//            adminTableList.get(tableNameInt).setAdminTableMenu(null);
//            adminTableList.get(tableNameInt).setAdminTablePrice(null);
//            adminTableList.get(tableNameInt).setAdminTableGender(null);
//            adminTableList.get(tableNameInt).setAdminTableGuestNumber(null);
//            adapter.notifyItemChanged(tableNameInt);
//
//        });
//    }

    private void requestTableInfo(int clickTable) {

        Call<TableInformationDTO> call = service.getTableImage("table" + clickTable);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<TableInformationDTO> call, Response<TableInformationDTO> response) {
                Log.d(TAG, "onResponse tableInfoCheck: " + response.body().getResult());
                if (response.isSuccessful()) {
                    switch (response.body().getResult()) {
                        case "success":
                            dialogManager.otherTableDialog(Admin.this, "admin", "table" + clickTable, response.body(), true).show();
                            break;

                        case "failed":
                            dialogManager.positiveBtnDialog(Admin.this, "정보를 입력하지 않은 테이블입니다.");
                            break;

                        default:
                            Log.d(TAG, "onResponse tableInfoCheck default");
                    }
                } else {
                    Log.d(TAG, "onResponse tableInfoCheck is not successful");
                }
            }

            @Override
            public void onFailure(Call<TableInformationDTO> call, Throwable t) {
                Log.d(TAG, "onFailure tableInfoCheck :" + t.getMessage());
            }
        });

    }

    public ArrayList<AdminTableList> loadAdminTableList() {
        String table = sharedPreference.getString("adminTableList", null);
        Type type = new TypeToken<ArrayList<AdminTableList>>() {
        }.getType();
        return gson.fromJson(table, type);

    }

    public ArrayList<OrderList> loadOrderList() {
        String order = sharedPreference.getString("orderList", null);
        Type type = new TypeToken<ArrayList<OrderList>>() {
        }.getType();
        return gson.fromJson(order, type);
    }

    public void updateTable(String request, String tableName, int tableNumber) {
        ArrayList<OrderList> orderLists = new ArrayList<>();
        switch (request) {
            case "PayNow":
                orderLists.add(new OrderList(PaymentCategory.NOW.getValue(), tableName, tableName + " 선불 좌석 이용 시작하였습니다."));
                popUpDialog = dialogManager.popUpAdmin(this, orderLists);
                popUpDialog.show();

                adminData.getAdminTableLists().get(tableNumber).setPaymentType(PaymentCategory.NOW.getValue());
                adminData.getAdminTableLists().get(tableNumber).setAdminTableStatement("선불 좌석 이용");

                adminTableLists = new ArrayList<>();
                adminTableLists = adminData.getAdminTableLists();

                adapter.notifyItemChanged(tableNumber);

                editor.putString("adminTableList", gson.toJson(adminTableLists));
                editor.commit();
                break;

            case "End":
                orderLists.add(new OrderList(PaymentCategory.NOW.getValue(), tableName, tableName + " 선불 좌석 이용 종료하였습니다."));
                popUpDialog = dialogManager.popUpAdmin(this, orderLists);
                popUpDialog.show();

                adminData.getAdminTableLists().get(tableNumber).init();
                adapter.notifyItemChanged(tableNumber);

                editor.putString("adminTableList", gson.toJson(adminData.getAdminTableLists()));
                editor.commit();
                break;

            case "PayLater":
                orderLists.add(new OrderList(PaymentCategory.NOW.getValue(), tableName, tableName + " 후불 좌석 이용 시작하였습니다."));
                popUpDialog = dialogManager.popUpAdmin(this, orderLists);
                popUpDialog.show();

                adminData.getAdminTableLists().get(tableNumber).setPaymentType(PaymentCategory.LATER.getValue());

                adminTableLists = new ArrayList<>();
                adminTableLists = adminData.getAdminTableLists();

                adapter.notifyItemChanged(tableNumber);

                editor.putString("adminTableList", gson.toJson(adminTableLists));
                editor.commit();
                break;

        }
    }

    public void orderMenu(String tableName,
                          int tableNumber,
                          String orderItemName,
                          JsonArray items,
                          int totalPrice) {

        ArrayList<OrderList> orderLists = new ArrayList<>();
        Log.d(TAG, "orderMenu orderItemName: " + orderItemName);

        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();

            String menuName = item.get("menuName").getAsString();
            int menuPrice = item.get("menuPrice").getAsInt();
            int menuQuantity = item.get("menuQuantity").getAsInt();

            orderLists.add(new OrderList(PaymentCategory.LATER.getValue(),
                    tableName, menuName, menuPrice, menuQuantity));
        }

        popUpDialog = dialogManager.popUpAdmin(this, orderLists);
        popUpDialog.show();

        String oldPrice = adminData.getAdminTableLists().get(tableNumber).getAdminTablePrice();

        if (oldPrice != null) {
            int newPrice = removeCommas(oldPrice) + totalPrice;
            adminData.getAdminTableLists().get(tableNumber).setAdminTablePrice(addCommasToNumber(newPrice));
        } else {
            adminData.getAdminTableLists().get(tableNumber).setAdminTablePrice(addCommasToNumber(totalPrice));
        }

        adminTableLists = new ArrayList<>();
        adminTableLists = adminData.getAdminTableLists();

        adapter.notifyItemChanged(tableNumber);
        editor.putString("adminTableList", gson.toJson(adminTableLists));
        editor.commit();

        updateOrderList(tableName, items);

    }

    public void updateOrderList(String tableName, JsonArray newOrder) {
        String orderItems = sharedPreference.getString(tableName, null);

        ArrayList<CartList> previousOrderList = new ArrayList<>();

        if (orderItems != null && !orderItems.isEmpty()) {
            Type type = new TypeToken<ArrayList<CartList>>() {
            }.getType();
            previousOrderList = gson.fromJson(orderItems, type);

            boolean found = false;
            for (int i = 0; i < newOrder.size(); i++) {
                JsonObject item = newOrder.get(i).getAsJsonObject();
                String menuName = item.get("menuName").getAsString();
                int menuQuantity = item.get("menuQuantity").getAsInt();
                int menuPrice = item.get("menuPrice").getAsInt();

                for (int j = 0; j < previousOrderList.size(); j++) {
                    CartList orderItem = previousOrderList.get(j);

                    if (orderItem.getMenuName().equals(menuName)) {

                        int oldQuantity = orderItem.getMenuQuantity();
                        int newQuantity = oldQuantity + menuQuantity;
                        orderItem.setMenuQuantity(newQuantity);

                        int oldPrice = orderItem.getMenuPrice();
                        int newPrice = oldPrice + menuPrice;
                        orderItem.setMenuPrice(newPrice);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    previousOrderList.add(new CartList(menuName,
                            menuPrice, menuQuantity, 0, CartCategory.MENU));
                }
                found = false;
            }

        } else {

            for (int i = 0; i < newOrder.size(); i++) {
                JsonObject item = newOrder.get(i).getAsJsonObject();

                String menuName = item.get("menuName").getAsString();
                int menuPrice = item.get("menuPrice").getAsInt();
                int menuQuantity = item.get("menuQuantity").getAsInt();

                previousOrderList.add(new CartList(menuName, menuPrice, menuQuantity, 0, CartCategory.MENU));
            }

        }
        editor.putString(tableName, gson.toJson(previousOrderList));
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (popUpDialog != null && popUpDialog.isShowing()) {
            popUpDialog.dismiss();
        }
    }
}
