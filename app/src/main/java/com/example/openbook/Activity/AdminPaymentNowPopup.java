package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Data.OrderList;
import com.example.openbook.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminPaymentNowPopup extends Activity {

    String TAG = "AdminPaymentBeforePopupTAG";

    String request, tableName;
    int tableIdentifier;

    ArrayList<OrderList> orderLists;
    AdminPopUpAdapter adminPopUpAdapter;
    RecyclerView popup_body_recyclerView;

    TextView popup_title;
    Button popup_button;

    Handler handler = new Handler();
    Gson gson;
    Map<String, String> tableInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.admin_popup);

        popup_title = findViewById(R.id.admin_popup_title);
        popup_body_recyclerView = findViewById(R.id.admin_popup_body_recyclerView);
        popup_button = findViewById(R.id.admin_popup_button);

        orderLists = new ArrayList<>();
        adminPopUpAdapter = new AdminPopUpAdapter();

        popup_body_recyclerView.setLayoutManager(new LinearLayoutManager(this));
        popup_body_recyclerView.setAdapter(adminPopUpAdapter);

        adminPopUpAdapter.setAdapterItem(orderLists);
        gson = new Gson();
        tableInfo = new HashMap<>();
    }

    @Override
    protected void onStart() {
        super.onStart();

        tableName = getIntent().getStringExtra("tableName");
        Log.d(TAG, "tableName: " + tableName);

        request = getIntent().getStringExtra("request");
        Log.d(TAG, "request: " + request);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (request != null) {
            tableInfo.put("request", request);
            tableInfo.put("tableName", tableName);

            switch (request){
                case "PayNow" :
                    orderLists.add(new OrderList(0, tableName, tableName + " 선불 좌석 이용 시작하였습니다."));
                    break;
                case "End" :
                    orderLists.add(new OrderList(0, tableName, tableName + " 선불 좌석 이용 종료하였습니다."));
                    break;

            }

        }


        popup_button.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPaymentNowPopup.this, Admin.class);
            intent.putExtra("tableRequest", gson.toJson(tableInfo));
//            intent.putExtra("request", request);
//            intent.putExtra("tableName", tableName);
//            intent.putExtra("tableIdentifier", tableIdentifier);

            startActivity(intent);
            overridePendingTransition(0, 0);
            handler.removeCallbacksAndMessages(null);
        });

        handler.postDelayed(() -> {
            Intent intent = new Intent(AdminPaymentNowPopup.this, Admin.class);
            intent.putExtra("tableRequest", gson.toJson(tableInfo));
//            intent.putExtra("tableIdentifier", tableIdentifier);

            startActivity(intent);
        }, 5000);

    }
}
