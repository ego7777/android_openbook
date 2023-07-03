package com.example.openbook.Chatting;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Activity.Table;
import com.example.openbook.Adapter.ChattingAdapter;
import com.example.openbook.Activity.Menu;
import com.example.openbook.R;
import com.example.openbook.Data.TableInformation;
import com.example.openbook.Data.ChattingList;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChattingUI extends AppCompatActivity {

    String TAG = "chatUI";

    String get_id, time;
    int table_num;


    ArrayList<ChattingList> chatLists;
    ChattingAdapter chattingAdapter;
    RecyclerView chatting_view;
    EditText chat_edit;

    DBHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;
    int version = 1;

    Handler mMainHandler;
    Looper mServiceLooper;
    ServiceHandler mServiceHandler;
    HandlerThread thread;

    boolean loop = true;

    public static final int MSG_CONNECT = 1;
    public static final int MSG_SEND = 3;
    public static final int MSG_CLIENT_STOP = 4;
    public static final int MSG_SERVER_STOP = 5;


    LocalDateTime localTime = LocalDateTime.now();

    HashMap<Integer, TableInformation> tableInformationHashMap;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("chattingDataArrived")) {
                String message = intent.getStringExtra("chattingData");
                Log.d(TAG, "onReceive: " + message);
                chattingUpdate(message);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("chattingDataArrived");
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

        table_num = getIntent().getIntExtra("tableNumber", 0);
        get_id = getIntent().getStringExtra("get_id");

        tableInformationHashMap = (HashMap<Integer, TableInformation>) getIntent().getSerializableExtra("tableInformation");
        Log.d(TAG, "tableInformation :" + tableInformationHashMap);


        tableInformationHashMap.get(table_num).setChattingAgree(true);
        version++;

        dbHelper = new DBHelper(ChattingUI.this, version);
        sqLiteDatabase = dbHelper.getWritableDatabase();


        /**
         * AppBar 설정
         */
        TextView menu = findViewById(R.id.appbar_menu_menu);
        menu.setOnClickListener(view ->{
            moveActivity(Menu.class);
        });


        TextView table = findViewById(R.id.appbar_menu_table);
        table.setOnClickListener(view ->{
            moveActivity(Table.class);
        });

        TextView table_number = findViewById(R.id.appbar_menu_table_number);
        table_number.setText(get_id);
        Log.d(TAG, "get_id: " + get_id);

        /**
         * 채팅방 메뉴 설정
         */
        TextView chat_tableNum = findViewById(R.id.chatting_tableNum);
        chat_tableNum.setText("Table" + String.valueOf(table_num));

        chat_edit = findViewById(R.id.chatting_edit);
        TextView chat_send = findViewById(R.id.chatting_sendBtn);


        /**
         * 채팅방 화면: 리사이클러뷰로 내가 보낸 것은 ViewType 1으로, 상대방이 보낸 것은 ViewType 0로 설정해서 화면에 띄워준다
         */
        chatting_view = findViewById(R.id.chatting_recyclerview);
        chattingAdapter = new ChattingAdapter();
        chatting_view.setLayoutManager(new LinearLayoutManager(ChattingUI.this, RecyclerView.VERTICAL, false));
        chatting_view.setAdapter(chattingAdapter);

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
            if (res.getString(3).equals(get_id) && res.getString(4).equals("table" + table_num)) {
                chatLists.add(new ChattingList(res.getString(1), 1, res.getString(2), ""));

                //receiver 가 table_num 인 것은 viewType 0으로
            } else if (res.getString(3).equals("table" + table_num) && res.getString(4).equals(get_id)) {
                chatLists.add(new ChattingList(res.getString(1), 0, res.getString(2), ""));
            }
        }


        chattingAdapter.setAdapterItem(chatLists);

        chatting_view.scrollToPosition(chattingAdapter.getItemCount() - 1);


        //핸들러 스레드를 생성하고 실행시킨다.
        thread = new HandlerThread("HandlerThread");
        thread.start();

        //핸들러 스레드로부터 루퍼를 얻는다.
        mServiceLooper = thread.getLooper();

        //핸들러 스레드가 제공한 루퍼객체를 매개변수로 핸들러 객체 생성
        mServiceHandler = new ServiceHandler(mServiceLooper);

        //메인 스레드 핸들러
        mMainHandler = new Handler(Looper.getMainLooper()) {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                String m;
                switch (msg.what) {
                    case MSG_CONNECT:
                        m = "정상적으로 서버에 접속하였습니다.";
                        break;

                    case MSG_CLIENT_STOP:
                        m = "클라이언트가 접속을 종료하였습니다.";
                        break;

                    case MSG_SERVER_STOP:
                        //서버에 의해 연결이 종료될 때 호출
                        m = "서버가 접속을 종료하였습니다.";

                        Message toMain = mMainHandler.obtainMessage();
                        toMain.what = MSG_CONNECT;
                        mMainHandler.sendMessage(toMain);

                        break;

                    case MSG_SEND:
                        //메세지 전송 시 호출


                        /**
                         * sendMsg[0] : 내 테이블 번호 (get_id)
                         * sendMsg[1] : 메세지
                         * sendMsg[2] : 보낸 테이블 번호 (table + table_num)
                         */
                        String sendMsg[] = msg.obj.toString().split("_");

                        time = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                        chatLists.add(new ChattingList(sendMsg[1], 1, time, ""));
                        Log.d(TAG, "전송된 메세지 : " + sendMsg[1]);

                        chattingAdapter.setAdapterItem(chatLists);
                        chatting_view.smoothScrollToPosition(chatLists.size());

                        //정상적으로 리사이클러뷰에 올라가면 db에 저장
                        dbHelper.insertChattingData(sendMsg[1], time, get_id, sendMsg[2], "");

//                        imm.hideSoftInputFromWindow(chat_edit.getWindowToken(), 0);

                        msg.obj = null;
                        chat_edit.setText((String) msg.obj);

                        m = "메세지 전송 완료!";

                        break;

                    default:
                        m = "에러 발생!";
                        break;
                }
                Log.d(TAG, "handlerMessage : " + m);

            }
        };


        /**
         * 채팅방 나가면
         */
        TextView chat_back = findViewById(R.id.chatting_back);
        chat_back.setOnClickListener(view ->{
            moveActivity(Table.class);
        });


        /**
         * 보내기 버튼 누르면 상대방 채팅방에도 보내는 것으로...!!!
         */
        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (chat_edit.getText().toString() != null) {

                    //ClientSocket 핸들러로부터 메세지를 하나 반환받는다
                    Message msg = mServiceHandler.obtainMessage();

                    msg.what = MSG_SEND;
                    msg.obj = get_id + "_" + chat_edit.getText().toString() + "_table" + table_num;
                    Log.d(TAG, "msg 전송 :" + (String) msg.obj);

                    mServiceHandler.sendMessage(msg);

                }
            }
        });


        Message toMain = mMainHandler.obtainMessage();
        toMain.what = MSG_CONNECT;
        mMainHandler.sendMessage(toMain);


    }



    class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Message toMain = mMainHandler.obtainMessage();

            switch (msg.what) {
                case MSG_SEND:

                    Intent intent = new Intent("SendChattingData");
                    intent.putExtra("sendToServer", (String) msg.obj);
                    LocalBroadcastManager.getInstance(ChattingUI.this).sendBroadcast(intent);

//                  메세지가 성공적으로 전송되었다면 전송한 문자열 화면에 출력
                    toMain.what = MSG_SEND;
                    toMain.obj = msg.obj;
                    mMainHandler.sendMessage(toMain);
                    break;


                case MSG_CLIENT_STOP:
                case MSG_SERVER_STOP:
                    //quit 메소드 호출

                    Log.d(TAG, "handleMessage: quit");

                    break;
            }
        }

    } //ServiceHandler inner class

    public void chattingUpdate(String line) {

        Runnable showUpdate = new Runnable() {
            @Override
            public void run() {

                String temp[] = line.split("_");


                if (temp[0].equals("read")) {

                    for (int i = 0; i < chatLists.size(); i++) {
                        chatLists.get(i).setRead("읽음");
                    }
                    chattingAdapter.notifyItemChanged(chatLists.lastIndexOf(1));


                } else {
                    //table1 형태
                    String receiver = temp[1];

                    temp[1] = temp[1].replace("table", "");


                    if (String.valueOf(table_num).equals(temp[1])) {
                        //처음엔 읽었는지 안읽었는지 모르니까 공란으로 넘겨버림
                        chatLists.add(new ChattingList(temp[0], 0, localTime.format(DateTimeFormatter.ofPattern("HH:mm")), ""));
                        Log.d(TAG, "showUpdate : " + line);


                        chattingAdapter.notifyDataSetChanged();
                        chatting_view.smoothScrollToPosition(chatLists.size());

                        time = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                        dbHelper.insertChattingData(temp[0], time, receiver, get_id, "");
                        Log.d(TAG, "전송 받은거 저장");
                    } // 해당 테이블에 데이터 넣어주기
                } // 읽음 처리 if문

            }
        };

        mMainHandler.post(showUpdate);
    }

    public void moveActivity(Class activity){
        Intent intent = new Intent(ChattingUI.this, activity);
        intent.putExtra("get_id", get_id);
        intent.putExtra("orderCk", true);
        intent.putExtra("TableInformation", tableInformationHashMap);
        startActivity(intent);
    }

}
