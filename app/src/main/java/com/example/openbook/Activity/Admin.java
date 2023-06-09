package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminTableAdapter;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.FCM.FCM;
import com.example.openbook.R;
import com.example.openbook.TableQuantity;

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

    String gender, guestNumber, tableName;

    SharedPreferences pref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        overridePendingTransition(0,0);


        adminTableList = (ArrayList<AdminTableList>) getIntent().getSerializableExtra("adminTableList");
        Log.d(TAG, "onCreate adminTableList size: " + adminTableList.size());
        get_id = getIntent().getStringExtra("get_id");

        if(get_id == null){
            get_id = "admin";
        }

        if(adminTableList == null){
            Log.d(TAG, "onCreate adminTableList null: ");
            adminTableList = new ArrayList<>();

            pref = getSharedPreferences("TableAdminInformation", MODE_PRIVATE);

            TableQuantity tableQuantity = new TableQuantity();

            int table = tableQuantity.getTableQuantity();
            String tableInformation;

            for(int i = 1; i<table+1; i++){
                tableInformation = pref.getString("table"+i, null);

                try {
                    JSONObject jsonObject =new JSONObject(tableInformation);

                    adminTableList.add(new AdminTableList("table"+i,
                            jsonObject.getString("adminTableMenu"),
                            jsonObject.getString("adminTablePrice"),
                            jsonObject.getString("adminTableGender"),
                            jsonObject.getString("adminTableGuestNumber")));


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } // for문 끝


        }else{
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
        Log.d(TAG, "onCreate setAdapter Table: ");

        OkHttpClient okHttpClient = new OkHttpClient();



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
    }

    @Override
    protected void onResume() {
        super.onResume();


        if(tableName != null){
            Log.d(TAG, "tableName not null: "  + tableName);
            int tableNameInt = Integer.parseInt(tableName.replace("table", ""))-1;
            adminTableList.get(tableNameInt).setAdminTableGender(gender);
            adminTableList.get(tableNameInt).setAdminTableGuestNumber(guestNumber);
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

        appbar_admin_modifyTable.setOnClickListener(view ->{
            startActivityClass(AdminModifyTableQuantity.class);


        });

        Dialog dialog = new Dialog(Admin.this);
        dialog.setContentView(R.layout.admin_receipt_dialog);


        adapter.setOnItemClickListener(new AdminTableAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                dialog.show();
            }
        });

    }

    public void startActivityClass(Class activity){
        Intent intent = new Intent(Admin.this, activity);
        intent.putExtra("get_id", get_id);
        intent.putExtra("adminTableList", adminTableList);
        startActivity(intent);
    }
}
