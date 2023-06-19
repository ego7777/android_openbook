package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DecimalFormat;
import android.icu.text.UnicodeSetSpanner;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Adapter.AdminTableAdapter;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.FCM.FCM;
import com.example.openbook.R;
import com.example.openbook.TableQuantity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;

public class Admin extends AppCompatActivity {

    String TAG = "AdminTAG";

    ArrayList<AdminTableList> adminTableList;

    AdminTableAdapter adapter;

    TextView appbar_admin_sales, appbar_admin_addMenu, appbar_admin_modifyTable;

    String get_id;

    String gender, guestNumber, tableName, tableStatement,  menuName;
    int totalPrice;

    DBHelper dbHelper;
    Cursor res;
    SQLiteDatabase sqLiteDatabase;
    int version=1;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        overridePendingTransition(0, 0);

        adminTableList = (ArrayList<AdminTableList>) getIntent().getSerializableExtra("adminTableList");


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

                String summary = sharedPreference.getString("table" +i + "menu", null);
                Log.d(TAG, "summary: " + summary);

                int price = sharedPreference.getInt("table" + i + "price", 0);
                Log.d(TAG, "price: " + price);

                if (summary != null) {
                    adminTableList.add(new AdminTableList("table" + i,
                            summary, String.valueOf(price), null, null));

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

        RecyclerView tableGrid = findViewById(R.id.admin_grid);
        adapter = new AdminTableAdapter();

        //그리드 레이아웃 설정
        tableGrid.setLayoutManager(new GridLayoutManager(this, 3));

        //어댑터 연결
        tableGrid.setAdapter(adapter);

        adapter.setAdapterItem(adminTableList);


        OkHttpClient okHttpClient = new OkHttpClient();

        dbHelper = new DBHelper(Admin.this, version);

        sqLiteDatabase = dbHelper.getWritableDatabase();
        res = dbHelper.getTableData("adminTableList");
        version ++;



    }


    @Override
    protected void onStart() {
        super.onStart();
        gender = null;
        gender = getIntent().getStringExtra("gender");

        guestNumber = null;
        guestNumber = getIntent().getStringExtra("guestNumber");

        tableName = null;
        tableName = getIntent().getStringExtra("tableName");

        tableStatement = getIntent().getStringExtra("tableStatement");


        if (menuName != null) {
            int pastCount = Integer.parseInt(String.valueOf(menuName.indexOf(menuName.length())));
            Log.d(TAG, "pastCount: " + pastCount);

        }
        menuName = getIntent().getStringExtra("menuName");
//        totalPrice = getIntent().getIntExtra("totalPrice", 0);
//        Log.d(TAG, "totalPrice: " + totalPrice);

    }

    @Override
    protected void onResume() {
        super.onResume();


        if (gender != null) {
            Log.d(TAG, "gender not null: " + gender);
            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
            adminTableList.get(tableNameInt).setAdminTableGender(gender);
            adminTableList.get(tableNameInt).setAdminTableGuestNumber(guestNumber);

        } else if (tableStatement != null) {
            Log.d(TAG, "tableStatement not null: " + tableStatement);

            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
            Log.d(TAG, "tableNameInt: " + tableNameInt);
            adminTableList.get(tableNameInt).setAdminTableMenu(tableStatement);
            adminTableList.get(tableNameInt).setAdminTablePrice("");

        } else if (menuName != null) {
            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
            adminTableList.get(tableNameInt).setAdminTableMenu(menuName);
            adminTableList.get(tableNameInt).setAdminTablePrice(String.valueOf(totalPrice));

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


        adapter.setOnItemClickListener(new AdminTableAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (!adminTableList.get(position).getAdminTableMenu().contains("선불")) {
                    showReceiptDialog(position, Admin.this);

                } else {
                    Log.d(TAG, "onItemClick: 빈 좌석 or 선불 이용 좌석");

                }


//                totalMenuList 여기 있는 데이터를 dialog에 띄울거야 (저장도 당연히 해야겠지)
            }
        });

    }

    public void showReceiptDialog(int position, Context context) {
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

    public String addCommasToNumber(int number){
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number) + "원";
    }
}
