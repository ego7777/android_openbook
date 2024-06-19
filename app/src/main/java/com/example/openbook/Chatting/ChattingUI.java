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
import com.example.openbook.TableCategory;
import com.example.openbook.Data.ChattingData;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.TableList;
import com.example.openbook.MessageDTO;
import com.example.openbook.R;
import com.example.openbook.Data.ChattingList;
import com.google.gson.Gson;

import java.time.LocalDateTime;
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
    int version = 1;
    LocalDateTime localTime = LocalDateTime.now();
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
                String message = intent.getStringExtra("isRead");
                Log.d(TAG, "onReceive isRead: " + message);
                isReadUpdate(message);

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
        setContentView(R.layout.chatting);

        tableNumber = getIntent().getIntExtra("tableNumber", 0);
        myData = (MyData) getIntent().getSerializableExtra("myData");

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
        Log.d(TAG, "chattingData: " + chattingDataHashMap);

//        chattingDataHashMap.get("table"+table_num).setChattingAgree(true);
        version++;

        dbHelper = new DBHelper(ChattingUI.this, version);

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


        /**
         * 채팅방 화면: 리사이클러뷰로 내가 보낸 것은 ViewType 1으로, 상대방이 보낸 것은 ViewType 0로 설정해서 화면에 띄워준다
         */
        chattingRecyclerView = findViewById(R.id.chatting_recyclerview);
        chattingAdapter = new ChattingAdapter();
        chattingRecyclerView.setLayoutManager(new LinearLayoutManager(ChattingUI.this, RecyclerView.VERTICAL, false));
        chattingRecyclerView.setAdapter(chattingAdapter);

        chatLists = new ArrayList<>();

        /**
         * 기존에 채팅한 내역이 있으면 채팅 내역을 가져온다.
         */
        Cursor res = dbHelper.getTableData("chattingTable");

        if (res.getCount() == 0) {
            Log.d(TAG, "SQlite에서 데이터를 찾을 수 없습니다.");
        }


        while (res.moveToNext()) {
            //sender 가 get_id인 것은 viewType 1로
            if (res.getString(3).equals(myData.getId())
                    && res.getString(4).equals("table" + tableNumber)) {
                chatLists.add(new ChattingList(res.getString(1), 1, res.getString(2), res.getString(5)));
                Log.d(TAG, "viewType: 오른쪽: " + res.getString(1));
                //receiver 가 table_num 인 것은 viewType 0으로
            } else if (res.getString(3).equals("table" + tableNumber) && res.getString(4).equals(myData.getId())) {
                chatLists.add(new ChattingList(res.getString(1), 0, res.getString(2), res.getString(5)));
                Log.d(TAG, "viewType 왼쪽: " + res.getString(1));
            }
        }


        chattingAdapter.setAdapterItem(chatLists);

        chattingRecyclerView.scrollToPosition(chattingAdapter.getItemCount() - 1);


        TextView chattingBack = findViewById(R.id.chatting_back);
        chattingBack.setOnClickListener(view -> moveActivity(Table.class));


        chattingSendButton.setOnClickListener(view -> {

            String chattingText = chattingEditText.getText().toString();

            if (!chattingText.isBlank()) {

                MessageDTO message = new MessageDTO(
                        "table" + tableNumber,
                        myData.getId(),
                        chattingText);

                Intent intent = new Intent("SendChattingData");
                intent.putExtra("sendToServer", gson.toJson(message));
                LocalBroadcastManager.getInstance(ChattingUI.this).sendBroadcast(intent);

                time = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                chatLists.add(new ChattingList(message.getMessage(), 1, time, "1"));

                chattingAdapter.setAdapterItem(chatLists);
                chattingRecyclerView.smoothScrollToPosition(chatLists.size());

                dbHelper.insertChattingData(message.getMessage(), time, myData.getId(), message.getTo(), "1");

                chattingEditText.setText(null);

                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(chattingSendButton.getWindowToken(), 0);

            }
        });

    }


    public void chattingUpdate(String receiveData) {
        Log.d(TAG, "chattingUpdate: 호출");

        MessageDTO message = gson.fromJson(receiveData, MessageDTO.class);

        String from = message.getFrom();
        String content = message.getMessage();
        int fromNumber = Integer.parseInt(from.replace("table", ""));

        if (tableNumber == fromNumber) {
            time = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

            chatLists.add(new ChattingList
                    (content,
                            TableCategory.MY.getValue(),
                            time,
                            ""));

            chattingAdapter.notifyDataSetChanged();
            chattingRecyclerView.smoothScrollToPosition(chatLists.size());


            dbHelper.insertChattingData(content, time, from, myData.getId(), "");

            Intent intent = new Intent("SendChattingData");
            message = new MessageDTO("table" + tableNumber, myData.getId(), "isRead");
            intent.putExtra("sendToServer", gson.toJson(message));
            LocalBroadcastManager.getInstance(ChattingUI.this).sendBroadcast(intent);
        }

    }

    private void isReadUpdate(String line) {

        Log.d(TAG, "isReadUpdate 호출");

        MessageDTO message = gson.fromJson(line, MessageDTO.class);
        String from = message.getFrom();
        int fromTable = Integer.parseInt(from.replace("table", ""));

        if (tableNumber == fromTable) {

            for (int i = 0; i < chatLists.size(); i++) {
                chatLists.get(i).setRead("");
                Log.d(TAG, "setRead: ");
            }

            chattingAdapter.notifyDataSetChanged();

            dbHelper.upDateIsRead(myData.getId(), from);
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
