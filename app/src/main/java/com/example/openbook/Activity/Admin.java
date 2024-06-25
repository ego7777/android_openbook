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
import android.widget.Toast;

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
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.kakaopay.KakaoPay;
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
    int totalPrice;
    String tid;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;
    AdminData adminData;
    RetrofitService service;
    ArrayList<AdminTableList> adminTableLists;
    ArrayList<OrderList> orderLists;
    Gson gson;
    DialogManager dialogManager;
    Dialog popUpDialog;
    String paidTable;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("tableRequest")) {
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
                            Log.d(TAG, "onReceive: ");
                            String fromMenuItem = requestJson.get("fromMenuItem").getAsString();
                            JsonArray fromJsonArray = gson.fromJson(fromMenuItem, JsonArray.class);
                            String from = requestJson.get("fromTable").getAsString();

                            String toMenuItem = requestJson.get("toMenuItem").getAsString();
                            JsonArray toJsonArray = gson.fromJson(toMenuItem, JsonArray.class);

                            orderGiftMenu(tableName, tableNumber,toJsonArray,from, fromJsonArray);
                            break;
                    }
                }
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
        tid = getIntent().getStringExtra("tid");
        paidTable = getIntent().getStringExtra("paidTable");

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


        if (adminData.getAdminTableLists() != null && adminData.getAdminTableLists().size() != 0) {
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
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tableRequest");
        LocalBroadcastManager.getInstance(Admin.this).registerReceiver(broadcastReceiver, intentFilter);

        appbarAdminSales.setOnClickListener(v -> {
            //매출 액티비티가 나온다
            startActivityClass(AdminSales.class);
        });

        if(paidTable != null && tid != null){
            int paidTableNumber = Integer.parseInt(paidTable.replace("table", ""));
            adminData.getAdminTableLists().get(paidTableNumber).init();
            adapter.notifyItemChanged(paidTableNumber);

            editor.putString("adminTableList", gson.toJson(adminData.getAdminTableLists()));
            editor.commit();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("request", "CompletePayment");
            jsonObject.addProperty("tid", tid);

            SendNotification sendNotification = new SendNotification();
            sendNotification.CompletePayment(paidTable, jsonObject.toString());
        }


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

                if(table.getPaymentType() == PaymentCategory.NOW.getValue()){
                    Toast.makeText(this, "선불 이용 좌석입니다.", Toast.LENGTH_SHORT).show();

                } else if (table.getAdminTablePrice() != null &&
                        table.getPaymentType() == PaymentCategory.LATER.getValue()) {

                    String tableName = table.getAdminTableNumber();
                    String tablePrice = table.getAdminTablePrice();
                    totalPrice = removeCommas(tablePrice);

                    Intent intent = new Intent(Admin.this, KakaoPay.class);
                    intent.putExtra("orderItemName", getOrderItemName(tableName));
                    intent.putExtra("totalPrice", totalPrice);
                    intent.putExtra("tableName", tableName);
                    intent.putExtra("orderItems", getOrderList(position));
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

    private void requestTableInfo(int clickTable) {

        Call<TableInformationDTO> call = service.requestTableInfo("table" + clickTable);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TableInformationDTO> call, @NonNull Response<TableInformationDTO> response) {
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
            public void onFailure(@NonNull Call<TableInformationDTO> call, @NonNull Throwable t) {
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

    public void orderGiftMenu(String to, int tableNumber,
                              JsonArray toMenuArray,
                              String from, JsonArray fromMenuArray){

        ArrayList<OrderList> orderLists = new ArrayList<>();
        JsonObject toItem = toMenuArray.get(0).getAsJsonObject();

        String toMenuName = toItem.get("menuName").getAsString();
        int menuQuantity = toItem.get("menuQuantity").getAsInt();

        JsonObject fromItem = fromMenuArray.get(0).getAsJsonObject();
        int menuPrice = fromItem.get("menuPrice").getAsInt();

        orderLists.add(new OrderList
                (PaymentCategory.LATER.getValue(),
                to, toMenuName, menuQuantity, 0));

        popUpDialog = dialogManager.popUpAdmin(this, orderLists);
        popUpDialog.show();

        int fromTableNumber = Integer.parseInt(from.replace("table", "")) - 1;

        String oldPrice = adminData.getAdminTableLists().get(fromTableNumber).getAdminTablePrice();
        Log.d(TAG, "orderGiftMenu oldPrice: " + oldPrice);

        if (oldPrice != null) {
            int newPrice = removeCommas(oldPrice) + menuPrice;
            Log.d(TAG, "orderGiftMenu newPrice: " + newPrice);
            adminData.getAdminTableLists().get(fromTableNumber).setAdminTablePrice(addCommasToNumber(newPrice));
        } else {
            adminData.getAdminTableLists().get(fromTableNumber).setAdminTablePrice(addCommasToNumber(menuPrice));
        }

        adminTableLists = new ArrayList<>();
        adminTableLists = adminData.getAdminTableLists();

        adapter.notifyItemChanged(fromTableNumber);
        editor.putString("adminTableList", gson.toJson(adminTableLists));
        editor.commit();

        updateOrderList(from, fromMenuArray);
        updateOrderList(to, toMenuArray);

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
