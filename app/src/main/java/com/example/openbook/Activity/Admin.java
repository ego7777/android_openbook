package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.text.DecimalFormat;
import android.icu.text.UnicodeSetSpanner;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Adapter.AdminTableAdapter;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.DialogCustom;
import com.example.openbook.FCM.FCM;
import com.example.openbook.KakaoPay;
import com.example.openbook.OrderSave;
import com.example.openbook.R;
import com.example.openbook.TableQuantity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Admin extends AppCompatActivity {

    String TAG = "AdminTAG";

    ArrayList<AdminTableList> adminTableList;

    AdminTableAdapter adapter;

    TextView appbar_admin_sales, appbar_admin_addMenu, appbar_admin_modifyTable;
    TextView admin_sidebar_menu, admin_sidebar_info, admin_sidebar_pay;

    String get_id;

    String gender, guestNumber, tableName, tableStatement, menuName;
    int totalPrice;

    DBHelper dbHelper;
    int version = 1;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;

    String afterPaymentList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        overridePendingTransition(0, 0);

        adminTableList = (ArrayList<AdminTableList>) getIntent().getSerializableExtra("adminTableList");
        afterPaymentList = getIntent().getStringExtra("orderList");
        Log.d(TAG, "orderList: " + afterPaymentList);

        get_id = getIntent().getStringExtra("get_id");

        if (get_id == null) {
            get_id = "admin";
        }


        sharedPreference = getSharedPreferences("oldMenuSummary", MODE_PRIVATE);
        editor = sharedPreference.edit();

        if (adminTableList != null) {
            Log.d(TAG, "onCreate adminTableList size : " + adminTableList.size());

        } else if (adminTableList == null) {

            Log.d(TAG, "onCreate adminTableList null: ");
            adminTableList = new ArrayList<>();
            TableQuantity tableQuantity = new TableQuantity();

            int table = tableQuantity.getTableQuantity();
            Log.d(TAG, "tableQuantity : " + table);


            for (int i = 1; i < table + 1; i++) {

                String summary = sharedPreference.getString("table" + i + "menu", null);
                Log.d(TAG, "summary: " + summary);

                int price = sharedPreference.getInt("table" + i + "price", 0);
                Log.d(TAG, "price: " + price);

                String gender = sharedPreference.getString("table" + i + "gender", null);
                Log.d(TAG, "gender: " + gender);

                String guestNumber = sharedPreference.getString("table" + i + "guestNumber", null);
                Log.d(TAG, "guestNumber: " + guestNumber);

                if (summary != null) {
                    adminTableList.add(new AdminTableList("table" + i,
                            summary, String.valueOf(price), gender, guestNumber));

                } else {
                    adminTableList.add(new AdminTableList("table" + i,
                            null,
                            null,
                            null,
                            null));
                }

            }// for문 끝


        } else {
            Log.d(TAG, "onCreate adminTableList not null: ");
        }

        /**
         * 로그인을 성공하면 id, token을 firebase realtime db에 저장
         */
        Intent fcm = new Intent(Admin.this, FCM.class);
        fcm.putExtra("get_id", get_id);
        startService(fcm);

        TextView appbar_admin_id = findViewById(R.id.appbar_admin_id);
        appbar_admin_id.setText(get_id);

        appbar_admin_sales = findViewById(R.id.appbar_admin_sales);
        appbar_admin_addMenu = findViewById(R.id.appbar_admin_addMenu);
        appbar_admin_modifyTable = findViewById(R.id.appbar_admin_modifyTable);

        admin_sidebar_menu = findViewById(R.id.admin_sidebar_menu);
        admin_sidebar_info = findViewById(R.id.admin_sidebar_info);
        admin_sidebar_pay = findViewById(R.id.admin_sidebar_pay);

        RecyclerView tableGrid = findViewById(R.id.admin_grid);
        adapter = new AdminTableAdapter();

        //그리드 레이아웃 설정
        tableGrid.setLayoutManager(new GridLayoutManager(this, 3));

        //어댑터 연결
        tableGrid.setAdapter(adapter);

        adapter.setAdapterItem(adminTableList);


        dbHelper = new DBHelper(Admin.this, version);
        version++;


    }


    @Override
    protected void onStart() {
        super.onStart();
        gender = null;
        gender = getIntent().getStringExtra("gender");
        editor.putString(tableName + "gender", gender);

        guestNumber = null;
        guestNumber = getIntent().getStringExtra("guestNumber");
        editor.putString(tableName + "guestNumber", guestNumber);
        editor.commit();

        tableName = null;
        tableName = getIntent().getStringExtra("tableName");

        tableStatement = getIntent().getStringExtra("tableStatement");


        if (menuName != null) {
            int pastCount = Integer.parseInt(String.valueOf(menuName.indexOf(menuName.length())));
            Log.d(TAG, "pastCount: " + pastCount);

        }
        menuName = getIntent().getStringExtra("menuName");

        if (afterPaymentList != null) {
            OrderSave orderSave = new OrderSave();


            try {
                boolean success = orderSave.orderSave(afterPaymentList);

                Log.d(TAG, "success: " + success);

                if (success == true) {
                    deleteData();
                    Log.d(TAG, "deleteData: ");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();


        if (gender != null) {
            Log.d(TAG, "gender not null: " + gender);
            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
            adminTableList.get(tableNameInt).setAdminTableGender(gender);
            adminTableList.get(tableNameInt).setAdminTableGuestNumber(guestNumber);
            // 이것도 쉐어드에 저장을 하고, 삭제하는 것으로..!

        } else if (tableStatement != null) {
            Log.d(TAG, "tableStatement not null: " + tableStatement);

            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
            Log.d(TAG, "tableNameInt: " + tableNameInt);
            adminTableList.get(tableNameInt).setAdminTableMenu(tableStatement);
            adminTableList.get(tableNameInt).setAdminTablePrice("");

        } else if (menuName != null) {
            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
            adminTableList.get(tableNameInt).setAdminTableMenu(menuName);
            Log.d(TAG, "setAdminTableMenu: " + menuName);
            adminTableList.get(tableNameInt).setAdminTablePrice(String.valueOf(totalPrice));
            Log.d(TAG, "setAdminTablePrice: " + totalPrice);

        }


        appbar_admin_sales.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //매출 액티비티가 나온다
                startActivityClass(AdminSales.class);
            }
        });

        appbar_admin_addMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 메뉴 이름, 가격, 이미지를 등록하면 서버로 들어가서 menuList Db에 등록된다
                startActivityClass(AdminModifyMenu.class);

            }
        });

        appbar_admin_modifyTable.setOnClickListener(view -> {
            startActivityClass(AdminModifyTableQuantity.class);


        });

        DialogCustom dialogCustom = new DialogCustom();


        adapter.setOnItemClickListener(new AdminTableAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Log.d(TAG, "onItemClick: " + position);

                admin_sidebar_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adminTableList.get(position).getAdminTableMenu() == null) {
                            dialogCustom.HandlerAlertDialog(Admin.this, "빈 좌석이거나 아직 주문하지 않은 좌석입니다.");

                        } else if (adminTableList.get(position).getAdminTableMenu().contains("선불")) {
                            dialogCustom.HandlerAlertDialog(Admin.this, "빈 좌석이거나 아직 주문하지 않은 좌석입니다.");

                        } else {
                            showReceiptDialog(position);

                        }
                    }
                });

                admin_sidebar_info.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                admin_sidebar_pay.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(View v) {

                        // sqlite에서 정보 날리고, 서버로 데이터 보낼겨
                        ArrayList<OrderList> orderLists = new ArrayList<>();
                        orderLists = dbHelper.fetchMenuDetails(tableName, orderLists);

                        if (adminTableList.get(position).getAdminTablePrice() != null) {
                            totalPrice = Integer.parseInt(adminTableList.get(position).getAdminTablePrice());
                            // 여기에 카카오 페이를 붙이겠읍니다.....
                            Intent intent = new Intent(Admin.this, KakaoPay.class);
                            intent.putExtra("menuName", getOrderMenuName(orderLists));
                            intent.putExtra("menuPrice", totalPrice);
                            intent.putExtra("tableName", tableName);
                            intent.putExtra("jsonOrderList", getJson(orderLists));
                            intent.putExtra("paymentStyle", "after");
                            intent.putExtra("get_id", get_id);
                            startActivity(intent);
                        } else {
                            dialogCustom.HandlerAlertDialog(Admin.this, "빈 좌석이거나 아직 주문하지 않은 좌석입니다.");
                        }


                    }
                });

            }
        });

    }

    public void showReceiptDialog(int position) {
        Dialog dialog = new Dialog(Admin.this);
        dialog.setContentView(R.layout.admin_receipt_dialog);

        AdminPopUpAdapter adminReceiptAdapter = new AdminPopUpAdapter();

        ArrayList<OrderList> adminOrderList = new ArrayList<>();

        adminOrderList = dbHelper.fetchMenuDetails(tableName, adminOrderList);

        TextView adminReceiptCancel = dialog.findViewById(R.id.admin_receipt_cancel);
        TextView adminReceiptTotalPrice = dialog.findViewById(R.id.admin_receipt_totalPrice);
        RecyclerView adminReceiptRecyclerView = dialog.findViewById(R.id.admin_receipt_recyclerView);

        adminReceiptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adminReceiptRecyclerView.setAdapter(adminReceiptAdapter);
        adminReceiptAdapter.setAdapterItem(adminOrderList);

        int getPrice = Integer.parseInt(adminTableList.get(position).getAdminTablePrice());

        String totalPrice = addCommasToNumber(getPrice);

        adminReceiptTotalPrice.setText(totalPrice);

        dialog.show();

        adminReceiptCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    public void startActivityClass(Class activity) {
        Intent intent = new Intent(Admin.this, activity);
        intent.putExtra("get_id", get_id);
        intent.putExtra("adminTableList", adminTableList);
        startActivity(intent);
    }

    public String addCommasToNumber(int number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number) + "원";
    }

    public String getOrderMenuName(ArrayList<OrderList> orderLists) {

        int menuQuantity = orderLists.size();

        String menuName;

        if (menuQuantity == 1) {
            menuName = orderLists.get(0).getMenu();
        } else {
            menuName = orderLists.get(0).getMenu() + " 외" + Integer.toString(menuQuantity - 1);
        }
        Log.d(TAG, "menuName :" + menuName);

        return menuName;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getJson(ArrayList<OrderList> list) {
        JSONObject jsonObject = new JSONObject();
        JSONArray menujArray = new JSONArray();//배열이 필요할때

        try {
            for (int i = 0; i < list.size(); i++)//배열
            {
                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
                sObject.put("menu", list.get(i).getMenu());
                sObject.put("quantity", list.get(i).getQuantity());
                sObject.put("price", list.get(i).getPrice());
                menujArray.put(sObject);
            }

            jsonObject.put("table", tableName);
            jsonObject.put("item", menujArray);

            Log.d(TAG, "getJson: " + jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    public void deleteData() {

        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                Log.d(TAG, "run: " + tableName);
                dbHelper.deleteTableData(tableName);
                editor.remove(tableName + "price");
                editor.remove(tableName + "menu");
                editor.commit();

                int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
                Log.d(TAG, "tableNameInt: " + tableNameInt);
                adminTableList.get(tableNameInt).setAdminTableMenu(null);
                adminTableList.get(tableNameInt).setAdminTablePrice(null);
                adapter.notifyItemChanged(tableNameInt);

            }
        });
    }


}
