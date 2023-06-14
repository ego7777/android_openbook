package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.R;
import com.example.openbook.TableQuantity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class AdminPopup extends Activity {

    String TAG = "AdminPopUp_TAG";

    String tableStatement, tableName;

    String menuName, totalMenuList, totalPrice;

    int tableNumber;

    ArrayList<OrderList> orderLists;
    AdminPopUpAdapter adminPopUpAdapter;

    TextView popup_title;
    RecyclerView popup_body_recyclerView;
    Button popup_button;

    Handler handler = new Handler();


    ArrayList<AdminTableList> adminTableList;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_popup);

        overridePendingTransition(0, 0);

        popup_title = findViewById(R.id.admin_popup_title);
        popup_body_recyclerView = findViewById(R.id.admin_popup_body_recyclerView);
        popup_button = findViewById(R.id.admin_popup_button);

        orderLists = new ArrayList<>();
        adminPopUpAdapter = new AdminPopUpAdapter();

        popup_body_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        popup_body_recyclerView.setAdapter(adminPopUpAdapter);

        adminPopUpAdapter.setAdapterItem(orderLists);


        if (adminTableList == null) {

            adminTableList = new ArrayList<>();


            TableQuantity tableQuantity = new TableQuantity();

            int table = tableQuantity.getTableQuantity();
            Log.d(TAG, "tableQuantity : " + table);


            for (int i = 1; i < table + 1; i++) {


                adminTableList.add(new AdminTableList("table" + i,
                        null,
                        null,
                        null,
                        null));


                adminTableList.add(new AdminTableList("table" + i,
                        menuName, totalPrice, null, null));

            }

        } // for문 끝

        Log.d(TAG, "adminTableSize: " + adminTableList.size());

    }


    @Override
    protected void onStart() {
        super.onStart();

        tableStatement = getIntent().getStringExtra("tableStatement");
        tableName = getIntent().getStringExtra("tableName");
        Log.d(TAG, "tableName: " + tableName);

        tableNumber = Integer.parseInt(tableName.replace("table", ""));
        Log.d(TAG, "tableNumber: " + tableNumber);


        menuName = getIntent().getStringExtra("menuName");
        totalMenuList = getIntent().getStringExtra("totalMenuList");
        totalPrice = getIntent().getStringExtra("totalPrice");
        Log.d(TAG, "totalMenuList: " + totalMenuList);


        popup_title.setText(tableName);
    }

    @Override
    protected void onResume() {
        super.onResume();


//        viewType: 0->in/out, 1->menu

        if (tableStatement != null) {
            if (tableStatement.contains("선불")) {
                orderLists.add(new OrderList(0, tableName, tableName + " 선불 좌석 이용 시작하였습니다."));


            } else if (tableStatement.contains("")) {
                orderLists.get(tableNumber - 1).setStatement(tableName + " 선불 좌석 이용 종료하였습니다.");

            }

        }


        if (totalMenuList != null) {
            try {
                JSONArray jsonArray = new JSONArray(totalMenuList);
                JSONObject jsonObject;


                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    orderLists.add(new OrderList(1, tableName,
                            jsonObject.getString("menu"), jsonObject.getInt("quantity"),
                            jsonObject.getInt("price")));

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


        popup_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, Admin.class);
            intent.putExtra("tableStatement", tableStatement);
            intent.putExtra("tableName", tableName);
            intent.putExtra("totalPrice", totalPrice);
            intent.putExtra("menuName", menuName);
            intent.putExtra("adminTableList", adminTableList);

            startActivity(intent);
            handler.removeCallbacksAndMessages(null);
        });


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AdminPopup.this, Admin.class);
                intent.putExtra("tableStatement", tableStatement);
                intent.putExtra("tableName", tableName);
                intent.putExtra("totalPrice", totalPrice);
                intent.putExtra("menuName", menuName);
                intent.putExtra("adminTableList", adminTableList);
                startActivity(intent);
            }
        }, 5000);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥 레이어 클릭해도 안닫히게 하기
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


}
