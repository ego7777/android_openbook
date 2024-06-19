package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.MenuAdapter;
import com.example.openbook.Adapter.TableAdapter;
import com.example.openbook.BuildConfig;
import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.ChattingData;
import com.example.openbook.Data.MenuList;
import com.example.openbook.Data.MyData;
import com.example.openbook.Deco.menu_recyclerview_deco;
import com.example.openbook.DialogManager;
import com.example.openbook.PaymentCategory;
import com.example.openbook.R;
import com.example.openbook.Data.TableList;
import com.example.openbook.TableCategory;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.example.openbook.retrofit.TableInformationDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

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
    HashMap<String, ChattingData> chattingDataHashMap;
    ArrayList<TableList> tableList;

    int clickTable, myTable, sendGiftQuantity;
    TableAdapter adapter;

    TextView appbarMenu, appbarOrderList, requestChatting, checkInformation, sendGift;
    LinearLayout tableSidebar;

    RetrofitService service;

    DialogManager dialogManager;

    SharedPreferences customerDataSp;
    SharedPreferences.Editor editor;
    Gson gson;


    SendToPopUp sendToPopUp = new SendToPopUp();

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
                    }else{
                        Log.d(TAG, "updateNewTable nothing");
                    }
                    break;

                case "chatRequest":
                    String fcmData = intent.getStringExtra("fcmData");

//                sendToPopUp.sendToPopUpChatting(Table.this, myData,
//                        chattingDataHashMap, ticketDataHashMap, tableList, fcmData);
                    break;
                case "giftArrived":
                    String from = intent.getStringExtra("tableName");
                    String menuName = intent.getStringExtra("menuName");

//                sendToPopUp.sendToPopUpGift(Table.this, myData,
//                        chattingDataHashMap, ticketDataHashMap, tableList, from, menuName);
                    break;
            }
        }
    };


    int tablePosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_activity);

        overridePendingTransition(0, 0);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        Log.d(TAG, "myData IsOrder: " + myData.isOrder());
        myTable = Integer.parseInt(myData.getId().replace("table", ""));
        Log.d(TAG, "myTable: " + myTable);

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

        customerDataSp = getSharedPreferences("CustomerData", MODE_PRIVATE);
        String activeTable = customerDataSp.getString("activeTableList", null);

        Log.d(TAG, "activeTable: " + activeTable);

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

        RecyclerView tableGrid = findViewById(R.id.tableGrid);
        adapter = new TableAdapter(tableList, myTable);

        //그리드 레이아웃 설정
        tableGrid.setLayoutManager(new GridLayoutManager(this, 5));
        tableGrid.setAdapter(adapter);

        if(activeTable != null) {
            activeTableUpdate(activeTable);
        }


        //오른쪽 사이드 메뉴
        tableSidebar = findViewById(R.id.table_sidebar);
        tableSidebar.setVisibility(View.INVISIBLE);

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
        intentFilter.addAction("chatRequest");
        intentFilter.addAction("sendChattingData");
        intentFilter.addAction("giftArrived");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);



//
//        if (tableList != null) {
//            updateNewTable(activeTable);
//        }

        appbarMenu.setOnClickListener(this::moveToMenu);

        appbarOrderList.setOnClickListener(v -> {

        });


        /**
         * table 누르면 옆에 사이드 메뉴 popup
         */
        adapter.setOnItemClickListener((view, position) -> {
            tableSidebar.setVisibility(View.VISIBLE);
            clickTable = position + 1;
            Log.d(TAG, "clickTable: " + clickTable);

            if (myData.getPaymentCategory() == PaymentCategory.NOW) {
                requestChatting.setVisibility(View.GONE);
            }

        });

        dialogManager = new DialogManager();


        /**
         * 채팅하기 누르면 팝업 뜨고 채팅할 수 있도록 :
         * 1. 내가 주문을 안했으면 주문하라고 팝업이 뜨고
         * 2. 상대방이 주문을 안했으면 알려주기
         */

        requestChatting.setOnClickListener(view -> {

            if (!myData.isOrder()) {
                dialogManager.positiveBtnDialog(Table.this, "주문 후 채팅이 가능합니다.").show();

            } else if (clickTable == myTable) {
                dialogManager.positiveBtnDialog(Table.this,
                        getResources().getString(R.string.myTable)).show();

            } else if (tableList.get(clickTable - 1).getCategory() != TableCategory.ACTIVE) {
                dialogManager.positiveBtnDialog(Table.this,
                        getResources().getString(R.string.unusableTable)).show();
            }
//            else if (tableList.get(clickTable - 1).getViewType() == 2) {
//
//                if (chattingDataHashMap == null ||
//                        !chattingDataHashMap.get("table" + clickTable).isChattingAgree()) {
//
//                    dialogManager.chattingRequest(Table.this,
//                            String.valueOf(clickTable) + R.string.chattingAlarm,
//                            "table" + clickTable, myData.getId());
//
//                    Log.d(TAG, "채팅 신청");
//
//                } else if (chattingDataHashMap.get("table" + clickTable).isChattingAgree()) {
//
//                    Intent intent = new Intent(Table.this, ChattingUI.class);
//                    intent.putExtra("tableNumber", clickTable);
//                    intent.putExtra("myData", myData);
//                    intent.putExtra("chattingData", chattingDataHashMap);
//                    intent.putExtra("ticketData", ticketDataHashMap);
//                    intent.putExtra("tableList", tableList);
//                    //여기서 이미 채팅 agree면 isRead를 보내는거야
//
//                    //상대방한테 알려줘야지
//                    Intent isRead = new Intent("SendChattingData");
//                    /**
//                     * 읽음_from_to
//                     */
//                    isRead.putExtra("sendToServer", "isRead_" + myData.getId() + "_table" + clickTable);
//                    LocalBroadcastManager.getInstance(Table.this).sendBroadcast(isRead);
//
//                    startActivity(intent);
//
//                }

            else {
                Intent intent = new Intent(Table.this, ChattingUI.class);
                intent.putExtra("tableNumber", clickTable);
                intent.putExtra("myData", myData);
                intent.putExtra("chattingData", chattingDataHashMap);
                intent.putExtra("tableList", tableList);
                startActivity(intent);
            }
        }); //setOnClickListener


        /**
         * info 누르면 해당 테이블 정보 볼 수 있게
         */
        checkInformation.setOnClickListener(view -> requestTableInfo(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TableInformationDTO> call, @NonNull Response<TableInformationDTO> response) {
                if (response.isSuccessful()) {

                    if (clickTable == myTable) {
                        dialogManager.myTableDialog(Table.this, response.body(), myData.getId()).show();
                    } else {
                        dialogManager.otherTableDialog(Table.this, response.body(), false).show();
                    }
                } else {
                    Log.d(TAG, "onResponse tableInformation isNotSuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<TableInformationDTO> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure tableInformation: " + t.getMessage());
            }
        }));

        sendGift.setOnClickListener(v -> {

            if (tableList.get(clickTable - 1).getCategory() != TableCategory.ACTIVE) {
                Log.d(TAG, "비어있는 테이블");
                dialogManager.positiveBtnDialog(Table.this,
                        String.valueOf(R.string.unusableTable));
            } else {

                Dialog dlg = new Dialog(Table.this);
                dlg.setContentView(R.layout.send_gift_select_dialog);
                dlg.show();

                RecyclerView sendGiftRecyclerview = dlg.findViewById(R.id.send_gift_select_recyclerview);
                TextView sendGiftCancel = dlg.findViewById(R.id.send_gift_select_cancel);

                sendGiftCancel.setOnClickListener(view -> dlg.dismiss());

                sendGiftRecyclerview.setLayoutManager(new LinearLayoutManager
                        (Table.this, RecyclerView.HORIZONTAL, false));

                MenuAdapter menuAdapter = new MenuAdapter();
                ArrayList<MenuList> menuLists = new ArrayList<>();

                sendGiftRecyclerview.setAdapter(menuAdapter);
                sendGiftRecyclerview.addItemDecoration(new menu_recyclerview_deco(Table.this));
                menuAdapter.setAdapterItem(menuLists);


                DBHelper dbHelper = new DBHelper(Table.this);
                menuLists = dbHelper.getTableData(menuLists);
                Log.d(TAG, "menuLists size: " + menuLists.size());

                menuAdapter.setAdapterItem(menuLists);

                menuAdapter.setOnItemClickListener((view, name, price, position) -> {
                    dlg.dismiss();

                    Dialog dialog = new Dialog(Table.this);
                    dialog.setContentView(R.layout.send_gift_quantity_dialog);
                    dialog.show();

                    TextView menuName = dialog.findViewById(R.id.send_gift_quantity_menuName);
                    TextView menuQuantity = dialog.findViewById(R.id.send_gift_quantity_menuQuantity);
                    TextView menuPrice = dialog.findViewById(R.id.send_gift_quantity_price);
                    Button sendGiftButton = dialog.findViewById(R.id.send_gift_button);
                    Button plus = dialog.findViewById(R.id.send_gift_quantity_plus);
                    Button minus = dialog.findViewById(R.id.send_gift_quantity_minus);
                    Button cancel = dialog.findViewById(R.id.send_gift_quantity_cancel);

                    cancel.setOnClickListener(v1 -> dialog.dismiss());


                    sendGiftQuantity = 1;
                    menuName.setText(name);
                    menuPrice.setText(String.valueOf(price));
                    menuQuantity.setText(String.valueOf(sendGiftQuantity));


                    plus.setOnClickListener(v12 -> {
                        sendGiftQuantity = sendGiftQuantity + 1;
                        menuQuantity.setText(String.valueOf(sendGiftQuantity));

                        int totalPrice = sendGiftQuantity * price;
                        menuPrice.setText(String.valueOf(totalPrice));

                    });

                    minus.setOnClickListener(v13 -> {
                        if (sendGiftQuantity > 0) {
                            sendGiftQuantity = sendGiftQuantity - 1;
                            menuQuantity.setText(String.valueOf(sendGiftQuantity));

                            int totalPrice = sendGiftQuantity * price;
                            menuPrice.setText(String.valueOf(totalPrice));
                        }

                    });

                    sendGiftButton.setOnClickListener(v14 -> {
                        Log.d(TAG, "onClick sendGift: ");
                        int menuPrice1 = sendGiftQuantity * price;
                        sendGiftOtherTable(name, sendGiftQuantity, menuPrice1);
                        dialog.dismiss();
                    });
                });
            }

        });


    } //onResume

    private void requestTableInfo(Callback<TableInformationDTO> callback) {
        Call<TableInformationDTO> call = service.getTableImage("table" + clickTable);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<TableInformationDTO> call, @NonNull Response<TableInformationDTO> response) {
                Log.d(TAG, "onResponse: " + response.body().getResult());
                callback.onResponse(call, response);
            }

            @Override
            public void onFailure(@NonNull Call<TableInformationDTO> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(Table.this).unregisterReceiver(broadcastReceiver);
    }


    public void moveToMenu(View view) {
        Intent intent = new Intent(Table.this, Menu.class);
        intent.putExtra("myData", myData);
        intent.putExtra("chattingData", chattingDataHashMap);
        intent.putExtra("tableList", tableList);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void updateNewTable(int newTableNumber) {
        Log.d(TAG, "tableUpdate: " + newTableNumber);
        editor = customerDataSp.edit();
        String activeTable = customerDataSp.getString("activeTableList", null);
        int[] table = gson.fromJson(activeTable, int[].class);
        table[table.length+1] = newTableNumber;

        Log.d(TAG, "add updateNewTable: " + Arrays.toString(table));
        editor.putString("activeTableList", Arrays.toString(table));
        editor.commit();

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
//            table[i] = Integer.parseInt(table[i]);

            if (table[i] != myTable) {
                Log.d(TAG, "activeTableUpdate table: " + table[i]);
                tablePosition = table[i] - 1;
                tableList.get(tablePosition).setCategory(TableCategory.ACTIVE);
                adapter.notifyItemChanged(tablePosition);
            }

        }
    }


        @Override
        protected void onActivityResult ( int requestCode, int resultCode, @Nullable Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            myData = (MyData) data.getSerializableExtra("myData");
            Log.d(TAG, "onActivityResult myData Id: " + myData.getId());
            Log.d(TAG, "onActivityResult myData IsOrder: " + myData.isOrder());

            chattingDataHashMap = (HashMap<String, ChattingData>) data.getSerializableExtra("chattingData");
            Log.d(TAG, "onActivityResult chattingData: " + chattingDataHashMap);


        }

        public String sendTicketToAdmin (String whoBuy){
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

        public void sendGiftOtherTable (String menuName,int menuQuantity, int menuPrice){

            Call<SuccessOrNot> call = service.sendGiftOtherTable("table" + clickTable,
                    myData.getId(), menuName, menuQuantity, menuPrice);

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {

                }

                @Override
                public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {

                }
            });


        }

        @Override
        public void onBackPressed () {
            //안드로이드 백버튼 막기
        }

    }



