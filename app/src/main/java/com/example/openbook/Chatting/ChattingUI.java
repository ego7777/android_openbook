package com.example.openbook.Chatting;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Activity.SendToPopUp;
import com.example.openbook.Activity.Table;
import com.example.openbook.Adapter.ChattingAdapter;
import com.example.openbook.Activity.Menu;
import com.example.openbook.Category.ChattingCategory;
import com.example.openbook.Data.ChattingData;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.TableList;
import com.example.openbook.R;
import com.example.openbook.Data.ChattingList;
import com.google.gson.Gson;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

public class ChattingUI extends AppCompatActivity {

    String TAG = "chatUI";

    String time;
    int tableNumber;
    ArrayList<ChattingList> chatLists;
    ChattingAdapter chattingAdapter;
    RecyclerView chattingRecyclerView;
    EditText chattingEditText;

    DBHelper dbHelper;
    ArrayList<TableList> tableList;

    MyData myData;
    HashMap<String, ChattingData> chattingDataHashMap;
    Gson gson = new Gson();
    SendToPopUp sendToPopUp = new SendToPopUp();

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("newChatArrived")) {
                String message = intent.getStringExtra("chat");
                Log.d(TAG, "onReceive message: " + message);
                chattingUpdate(message);

            } else if (intent.getAction().equals("isReadArrived")) {
                String readTable = intent.getStringExtra("readTable");
                Log.d(TAG, "onReceive isRead: " + readTable);
                isReadUpdate(readTable);

            } else if (intent.getAction().equals("chatRequest")) {
                String fcmData = intent.getStringExtra("fcmData");
//                sendToPopUp.sendToPopUpChatting(ChattingUI.this, myData,
//                        chattingDataHashMap, ticketDataHashMap, tableList, fcmData);

            } else if (intent.getAction().equals("giftArrived")) {
                String from = intent.getStringExtra("tableName");
                String menuName = intent.getStringExtra("menuName");

//                sendToPopUp.sendToPopUpGift(ChattingUI.this, myData,
//                        chattingDataHashMap, ticketDataHashMap, tableList, from, menuName);
            }

        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("newChatArrived");
        intentFilter.addAction("isReadArrived");
        intentFilter.addAction("chatRequest");
        intentFilter.addAction("giftArrived");
        LocalBroadcastManager.getInstance(ChattingUI.this).registerReceiver(broadcastReceiver, intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(ChattingUI.this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        tableNumber = getIntent().getIntExtra("tableNumber", 0);
        myData = (MyData) getIntent().getSerializableExtra("myData");

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
        Log.d(TAG, "chattingData: " + chattingDataHashMap);

//        chattingDataHashMap.get("table"+table_num).setChattingAgree(true);


        TextView moveMenu = findViewById(R.id.appbar_menu_menu);
        moveMenu.setOnClickListener(view -> moveActivity(Menu.class));

        TextView moveTable = findViewById(R.id.appbar_menu_table);
        moveTable.setOnClickListener(view -> moveActivity(Table.class));

        TextView appbarTableNumber = findViewById(R.id.appbar_menu_table_number);
        appbarTableNumber.setText(myData.getId());

        TextView chatTableNum = findViewById(R.id.chatting_tableNum);
        chatTableNum.setText("Table" + tableNumber);

        chattingEditText = findViewById(R.id.chatting_edit);
        TextView chattingSendButton = findViewById(R.id.chatting_sendBtn);


        chattingRecyclerView = findViewById(R.id.chatting_recyclerview);
        chattingAdapter = new ChattingAdapter();
        chattingRecyclerView.setLayoutManager(new LinearLayoutManager(ChattingUI.this, RecyclerView.VERTICAL, false));
        chattingRecyclerView.setAdapter(chattingAdapter);


        dbHelper = new DBHelper(ChattingUI.this);
        Cursor res = dbHelper.getTableData("chattingTable");

        chatLists = initChattingList(res);

        chattingAdapter.setAdapterItem(chatLists);

        chattingRecyclerView.scrollToPosition(chattingAdapter.getItemCount() - 1);


        TextView chattingBack = findViewById(R.id.chatting_back);
        chattingBack.setOnClickListener(view -> moveActivity(Table.class));

        chattingSendButton.setOnClickListener(view -> {

            String chattingText = chattingEditText.getText().toString();

            if (!chattingText.isBlank()) {

                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

                MessageDTO message = new MessageDTO(
                        "table" + tableNumber,
                        myData.getId(),
                        chattingText,
                        time);

                Intent intent = new Intent("SendChattingData");
                intent.putExtra("sendToServer", gson.toJson(message));
                LocalBroadcastManager.getInstance(ChattingUI.this).sendBroadcast(intent);

                chatLists.add(new ChattingList(message.getMessage(), ChattingCategory.MINE, time, "1"));

                chattingAdapter.notifyItemChanged(chatLists.size());
                chattingRecyclerView.smoothScrollToPosition(chatLists.size());

                dbHelper.insertChattingData(message.getMessage(), time, myData.getId(), message.getTo(), "1");

                chattingEditText.setText(null);

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(chattingSendButton.getWindowToken(), 0);

            }
        });
    }

    public void notifyRead(){
        Log.d(TAG, "notifyRead 호출");
        Intent intent = new Intent("SendChattingData");
        MessageDTO message = new MessageDTO
                ("table" + tableNumber,
                        myData.getId(),
                        "isRead",
                        "");
        intent.putExtra("sendToServer", gson.toJson(message));
        LocalBroadcastManager.getInstance(ChattingUI.this).sendBroadcast(intent);
    }

    public ArrayList<ChattingList> initChattingList(Cursor res){
        chatLists = new ArrayList<>();

        if (res.getCount() > 0) {

            while (res.moveToNext()) {

                String message = res.getString(1);
                String time = res.getString(2);
                String sender = res.getString(3);
                String receiver = res.getString(4);
                String isRead = res.getString(5);

                if (sender.equals(myData.getId()) && receiver.equals("table" + tableNumber)) {
                    chatLists.add(new ChattingList(message, ChattingCategory.MINE, time, isRead));
                    Log.d(TAG, "내가 보낸 메세지: " + message);

                } else if (sender.equals("table" + tableNumber) && receiver.equals(myData.getId())) {
                    chatLists.add(new ChattingList(message, ChattingCategory.OTHERS, time, isRead));
                    Log.d(TAG, "받은 메세지: " + message);
                }
            }
            notifyRead();
        }

        return chatLists;
    }


    public void chattingUpdate(String receiveData) {
        Log.d(TAG, "chattingUpdate: 호출");

        MessageDTO message = gson.fromJson(receiveData, MessageDTO.class);

        String from = message.getFrom();
        String content = message.getMessage();
        int fromNumber = Integer.parseInt(from.replace("table", ""));
        String receivedTime = message.getTime();

        if (tableNumber == fromNumber) {

            chatLists.add(new ChattingList
                    (content,
                            ChattingCategory.OTHERS,
                            receivedTime,
                            ""));

            chattingAdapter.notifyDataSetChanged();
            chattingRecyclerView.smoothScrollToPosition(chatLists.size());
            notifyRead();
        }

    }

    private void isReadUpdate(String readTable) {

        int fromTable = Integer.parseInt(readTable.replace("table", ""));

        if (tableNumber == fromTable) {

            for (int i = 0; i < chatLists.size(); i++) {
                if(chatLists.get(i).getRead().equals("1")){
                    chatLists.get(i).setRead("");
                }
            }

            chattingAdapter.notifyDataSetChanged();
        }

    }


    public void moveActivity(Class activity) {
        Intent intent = new Intent(ChattingUI.this, activity);
        intent.putExtra("myData", myData);
        intent.putExtra("chattingData", chattingDataHashMap);
        intent.putExtra("tableList", tableList);
//        intent.putExtra("ticketData", ticketDataHashMap);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
    }
}
