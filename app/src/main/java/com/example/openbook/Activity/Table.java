package com.example.openbook.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.openbook.Adapter.TableAdapter;
import com.example.openbook.BuildConfig;
import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.OrderList;
import com.example.openbook.DialogManager;
import com.example.openbook.Category.PaymentCategory;
import com.example.openbook.ManageOrderItems;
import com.example.openbook.R;
import com.example.openbook.Data.TableList;
import com.example.openbook.Category.TableCategory;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.TableInformationDTO;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class Table extends AppCompatActivity {

    String TAG = "TableTAG";
    MyData myData;
    HashMap<String, Boolean> profileTicketMap;
    ArrayList<TableList> tableList;

    int clickTable, myTable;
    TableAdapter adapter;

    TextView appbarMenu, appbarOrderList, requestChatting, checkInformation, sendGift;
    LinearLayout tableSidebar;

    ImageView tableDocument;

    RetrofitService service;

    DialogManager dialogManager;

    SharedPreferences customerDataSp;
    Gson gson;
    int previousClickTable;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "updateNewTable":
                    int newTableNumber = intent.getIntExtra("newTable", 1000);
                    String activeTableList = intent.getStringExtra("activeTableList");

                    if (newTableNumber != 1000) {
                        Log.d(TAG, "newTableNumber: " + newTableNumber);
                        updateNewTable(newTableNumber);

                    } else if (activeTableList != null) {
                        Log.d(TAG, "activeTableList: " + activeTableList);
                        activeTableUpdate(activeTableList);
                    } else {
                        Log.d(TAG, "updateNewTable nothing");
                    }
                    break;

                case "giftArrived":
                    String from = intent.getStringExtra("from");
                    String menuItem = intent.getStringExtra("menuItem");
                    String count = intent.getStringExtra("count");

                    dialogManager.giftReceiveDialog(Table.this, myData.getId(), from, menuItem, count).show();
                    break;

                case "isGiftAccept":
                    from = intent.getStringExtra("from");
                    boolean isAccept = intent.getBooleanExtra("isAccept", false);
                    String message;
                    if(isAccept){
                        message = from + "에서 선물을 수락하였습니다.";
                        //여기서 메뉴 주문 메뉴 저장하기
                    }else{
                        message = from + "에서 선물을 거절하였습니다.";
                    }
                    dialogManager.positiveBtnDialog(Table.this, message).show();
                    break;
            }
        }
    };


    int tablePosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        overridePendingTransition(0, 0);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        Log.d(TAG, "myData IsOrder: " + myData.isOrder());
        myTable = Integer.parseInt(myData.getId().replace("table", ""));
        Log.d(TAG, "myTable: " + myTable);

        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

        customerDataSp = getSharedPreferences("CustomerData", MODE_PRIVATE);
        String activeTable = customerDataSp.getString("activeTableList", null);
        if (tableList != null) {

            Log.d(TAG, "tableList size: " + tableList.size());

        } else {
            Log.d(TAG, "table is null");

            tableList = new ArrayList<>();

            for (int i = 1; i < myData.getTableFromDB() + 1; i++) {
                if (i == myTable) {
                    tableList.add(new TableList(myData.getId(), TableCategory.MY));
                } else {
                    tableList.add(new TableList(i, TableCategory.OTHER));
                }
            }
        }


        if (!myData.getId().equals("구글로그인")) {
            myTable = Integer.parseInt(myData.getId().replace("table", ""));
            TextView table_num = findViewById(R.id.appbar_menu_table_number);
            table_num.setText(myData.getId());
        }


        /**
         * Appbar
         */
        appbarMenu = findViewById(R.id.appbar_menu_menu);

        appbarOrderList = findViewById(R.id.appbar_menu_orderList);

        RecyclerView tableRecyclerview = findViewById(R.id.tableGrid);
        adapter = new TableAdapter(tableList, myTable);

        RecyclerView.ItemAnimator animator = tableRecyclerview.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        //그리드 레이아웃 설정
        tableRecyclerview.setLayoutManager(new GridLayoutManager(this, 5));
        tableRecyclerview.setAdapter(adapter);

        if (activeTable != null && tableList.size() > 0) {
            activeTableUpdate(activeTable);
        }


        //오른쪽 사이드 메뉴
        tableSidebar = findViewById(R.id.table_sidebar);
        tableDocument = findViewById(R.id.table_document);

        requestChatting = findViewById(R.id.chatting);
        checkInformation = findViewById(R.id.take_info);
        sendGift = findViewById(R.id.send_gift);

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);

    } //onCreate


    @Override
    protected void onResume() {
        super.onResume();
        //로컬 브로드 캐스트 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("updateNewTable");
        intentFilter.addAction("sendChattingData");
        intentFilter.addAction("giftArrived");
        intentFilter.addAction("isGiftAccept");
        intentFilter.addAction("CompletePayment");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        appbarMenu.setOnClickListener(this::moveToMenu);

        appbarOrderList.setOnClickListener(v -> {
            ManageOrderItems manageOrderItems = new ManageOrderItems();
            Pair<ArrayList<OrderList>, String> pair = manageOrderItems.getReceiptData(this, myData);
            dialogManager.showReceiptDialog(this, pair.first, pair.second).show();
        });


        /**
         * table 누르면 옆에 사이드 메뉴 popup
         */
        adapter.setOnItemClickListener((view, position) -> {

            clickTable = position + 1;
            if(previousClickTable == clickTable){
                tableSidebar.setVisibility(View.INVISIBLE);
                tableDocument.setVisibility(View.VISIBLE);
                previousClickTable = -1;
                adapter.setLastClickedPosition(position, false);
            }else{
                tableSidebar.setVisibility(View.VISIBLE);
                tableDocument.setVisibility(View.INVISIBLE);
                previousClickTable = clickTable;
                adapter.setLastClickedPosition(position, true);
            }

            Log.d(TAG, "clickTable: " + clickTable);

            if (myData.getPaymentCategory() == PaymentCategory.NOW) {
                requestChatting.setVisibility(View.GONE);
            }

        });

        dialogManager = new DialogManager();

        requestChatting.setOnClickListener(view -> {

            if (!myData.isOrder()) {
                dialogManager.positiveBtnDialog
                        (Table.this,
                                getResources().getString(R.string.notOrder)).show();

            } else if (clickTable == myTable) {
                dialogManager.positiveBtnDialog(Table.this,
                        getResources().getString(R.string.myTable)).show();

            } else if (tableList.get(clickTable - 1).getCategory() != TableCategory.ACTIVE) {
                dialogManager.positiveBtnDialog(Table.this,
                        getResources().getString(R.string.unusableTable)).show();

            } else {
                Intent intent = new Intent(Table.this, ChattingUI.class);
                intent.putExtra("tableNumber", clickTable);
                intent.putExtra("myData", myData);
                intent.putExtra("tableList", tableList);
                startActivity(intent);
            }
        });


        checkInformation.setOnClickListener(view -> {
            if(!myData.isOrder()){
                dialogManager.positiveBtnDialog
                        (Table.this,
                        getResources().getString(R.string.notOrder)).show();

            }else if(tableList.get(clickTable - 1).getCategory() == TableCategory.OTHER){
                dialogManager.positiveBtnDialog(Table.this,
                        getResources().getString(R.string.unusableTable)).show();
            }else{
                requestTableInfo();
            }

        });

        sendGift.setOnClickListener(v -> {
            if(!myData.isOrder()){
                dialogManager.positiveBtnDialog
                        (Table.this,
                                getResources().getString(R.string.notOrder)).show();

            }else if(tableList.get(clickTable - 1).getCategory() == TableCategory.OTHER){
                dialogManager.positiveBtnDialog(Table.this,
                        getResources().getString(R.string.unusableTable)).show();
            }else{
                dialogManager.giftSelectDialog(Table.this, myData.getId(), "table" + clickTable).show();
            }
        });


    } //onResume

    private void requestTableInfo() {
        Call<TableInformationDTO> call = service.requestTableInfo("table" + clickTable);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TableInformationDTO> call, @NonNull Response<TableInformationDTO> response) {
                Log.d(TAG, "onResponse: " + response.body().getResult());
                if (response.isSuccessful()) {

                    if (clickTable == myTable) {
                        dialogManager.myTableDialog(Table.this, response.body(), myData.getId()).show();
                    } else {
                        String profileTicket = customerDataSp.getString("profileTicket", null);
                        Log.d(TAG, "profileTicket: " + profileTicket);
                        Boolean ticket = false;

                        if(profileTicket != null){
                            gson = new Gson();
                            profileTicketMap = gson.fromJson(profileTicket, HashMap.class);

                            ticket = profileTicketMap.get("table" + clickTable);
                        }

                        dialogManager.otherTableDialog
                                (Table.this,
                                        myData.getId(),
                                        "table" + clickTable,
                                        response.body(), ticket).show();
                    }
                } else {
                    Log.d(TAG, "onResponse tableInformation isNotSuccessful");
                    Toast.makeText(Table.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TableInformationDTO> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure tableInformation: " + t.getMessage());
                Toast.makeText(Table.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
            }
        });
    }




    public void moveToMenu(View view) {
        Intent intent = new Intent(Table.this, Menu.class);
        intent.putExtra("myData", myData);
        intent.putExtra("tableList", tableList);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void updateNewTable(int newTableNumber) {
        Log.d(TAG, "tableUpdate: " + newTableNumber);

        if (newTableNumber == 1000) {
            Log.d(TAG, "tableUpdate line null: ");
            return;
        }
        tableList.get(newTableNumber - 1).setCategory(TableCategory.ACTIVE);
        adapter.notifyItemChanged(newTableNumber - 1);
    }

    public void activeTableUpdate(String activeTableList) {
        Log.d(TAG, "activeTableUpdate: " + activeTableList);

        gson = new Gson();
        int[] table = gson.fromJson(activeTableList, int[].class);
        Arrays.sort(table);
        Log.d(TAG, "activeTableUpdate after sort: " + Arrays.toString(table));

        for (int i = 0; i < table.length; i++) {

            if (table[i] != myTable) {
                Log.d(TAG, "activeTableUpdate table: " + table[i]);
                tablePosition = table[i] - 1;
                tableList.get(tablePosition).setCategory(TableCategory.ACTIVE);
                adapter.notifyItemChanged(tablePosition);
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        myData = (MyData) data.getSerializableExtra("myData");
        Log.d(TAG, "onActivityResult myData Id: " + myData.getId());
        Log.d(TAG, "onActivityResult myData IsOrder: " + myData.isOrder());

    }

    public String sendTicketToAdmin(String whoBuy) {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray menujArray = new JSONArray();//배열이 필요할때
            JSONObject object = new JSONObject();

            object.put("menu", "profileTicket");
            object.put("price", 2000);
            object.put("quantity", 1);
            menujArray.put(object);

            jsonObject.put("item", menujArray);
            jsonObject.put("menuName", "profileTicket");
            jsonObject.put("tableName", whoBuy);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }


    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
    }

}



