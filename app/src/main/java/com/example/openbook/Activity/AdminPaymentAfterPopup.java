package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.R;
import com.example.openbook.SharedPreference;
import com.example.openbook.TableQuantity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AdminPaymentAfterPopup extends Activity {

    String TAG = "AdminPopUp_TAG";

    String tableName;

    String newMenuSummary, oldMenuSummary, totalMenuList;

    int oldTotalPrice, newTotalPrice, tableNumber;

    ArrayList<OrderList> orderLists;
    AdminPopUpAdapter adminPopUpAdapter;

    TextView popup_title;
    RecyclerView popup_body_recyclerView;
    Button popup_button;

    Handler handler = new Handler();

    ArrayList<AdminTableList> adminTableList;
    DBHelper dbHelper;

    int version = 2;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;



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


        dbHelper = new DBHelper(AdminPaymentAfterPopup.this, version);
        version++;

        sharedPreference = getSharedPreferences("oldMenuSummary", MODE_PRIVATE);
        editor = sharedPreference.edit();


    }


    @Override
    protected void onStart() {
        super.onStart();


        tableName = getIntent().getStringExtra("tableName");
        Log.d(TAG, "tableName: " + tableName);

        tableNumber = Integer.parseInt(tableName.replace("table", ""));
//        Log.d(TAG, "tableNumber: " + tableNumber);

        oldMenuSummary = sharedPreference.getString(tableName + "menu", null);
        Log.d(TAG, "oldMenuSummary getSP: " + oldMenuSummary);

        oldTotalPrice = sharedPreference.getInt(tableName + "price", 0);
        Log.d(TAG, "oldTotalPrice getSP: " + oldTotalPrice);

        if (oldMenuSummary != null) {
            if (oldMenuSummary.contains("외")) {

                int oldTest = Integer.parseInt(oldMenuSummary.substring(oldMenuSummary.length() - 1));
                Log.d(TAG, "oldTestCount: " + oldTest);


            } else {

                int oldTest = 1;

            }
        }

        newMenuSummary = getIntent().getStringExtra("menuSummary");
        Log.d(TAG, "newMenuSummary Intent: " + newMenuSummary);
        //menuName : ex) 목살스테이크 외 2
        newTotalPrice = getIntent().getIntExtra("totalPrice", 0);
        Log.d(TAG, "newTotalPrice Intent: " + newTotalPrice);

        if (newMenuSummary != null) {

            int additionalCount = getAdditionalCount(newMenuSummary);
            Log.d(TAG, "additionalCount: " + additionalCount);

            if(oldMenuSummary != null){
                newMenuSummary = summarizeMenu(oldMenuSummary, additionalCount);
                newTotalPrice = newTotalPrice + oldTotalPrice;
                Log.d(TAG, "newMenuSummary 처리: " + newMenuSummary);
                Log.d(TAG, "newTotalPrice 처리: " + newTotalPrice);
            }

            editor.putString(tableName + "menu", newMenuSummary);
            editor.putInt(tableName + "price", newTotalPrice);
            editor.commit();

        }


        totalMenuList = getIntent().getStringExtra("totalMenuList");
        Log.d(TAG, "totalMenuList: " + totalMenuList);


        popup_title.setText(tableName);
    }

    @Override
    protected void onResume() {
        super.onResume();


//        viewType: 0->in/out, 1->menu


        if (totalMenuList != null) {
            try {
                JSONArray jsonArray = new JSONArray(totalMenuList);
                JSONObject jsonObject;


                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);

                    String menuName = jsonObject.getString("menu");
                    int menuQuantity = jsonObject.getInt("quantity");
                    int menuPrice = jsonObject.getInt("price");

                    orderLists.add(new OrderList(1, tableName,
                            menuName, menuQuantity, menuPrice));

                    //기존에 있으면 개수만 증가, 아니면 추가
                    dbHelper.addMenu(tableName, menuName, menuQuantity, menuPrice);

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


        popup_button.setOnClickListener(view -> {
            Intent intent = new Intent(this, Admin.class);

            intent.putExtra("tableName", tableName);
            intent.putExtra("adminTableList", adminTableList);

            startActivity(intent);
            handler.removeCallbacksAndMessages(null);
        });


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(AdminPaymentAfterPopup.this, Admin.class);

                intent.putExtra("tableName", tableName);
                intent.putExtra("adminTableList", adminTableList);

                startActivity(intent);
            }
        }, 5000);


        //intent로 넘겨줄라고 하는거임
        if (adminTableList == null) {

            adminTableList = new ArrayList<>();


            TableQuantity tableQuantity = new TableQuantity();

            int table = tableQuantity.getTableQuantity();
//            Log.d(TAG, "onResume tableQuantity : " + table);


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

        }

//        Log.d(TAG, "onResume tableList size: "  + adminTableList.size());


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

    public int getAdditionalCount(String newMenu) {
        if(newMenu.contains("외")){
            String getCountString = newMenu.substring(newMenu.length()-1);
            Log.d(TAG, "getCount: " + getCountString);

            int getCountInt = Integer.parseInt(getCountString) + 1;
            Log.d(TAG, "getCountInt: " + getCountInt);

            return getCountInt;

        }else{
            int getCountInt = 1;

            return getCountInt;
        }
    }

    public String summarizeMenu(String oldMenu, int additionalCount) {

        if (oldMenu.contains("외")) {
            // '외' 이후의 숫자 추출
            int countStartIndex = oldMenu.lastIndexOf("외") + 1;
            Log.d(TAG, "countStartIndex: " + countStartIndex);

            String countString = oldMenu.substring(countStartIndex).trim();
            Log.d(TAG, "countString: " + countString);

            //숫자를 정수로 변환
            int existingCount = Integer.parseInt(countString);
            Log.d(TAG, "existingCount: " + existingCount);

            //총 개수 계산
            int totalCount = existingCount + additionalCount;
            Log.d(TAG, "totalCount: " + totalCount);

            String summarizedMenu = oldMenu.substring(0, countStartIndex) + " "+totalCount;
            Log.d(TAG, "summarizeMenu: " + summarizedMenu);

            return summarizedMenu;

        } else {
            //'외'로 끝나지 않는 경우, '외 1' 추가
            String summarizedMenu = oldMenu + " 외 " + additionalCount;
            Log.d(TAG, "summarizeMenu not endWith 외: " + summarizedMenu);

            return summarizedMenu;
        }
    }


}
