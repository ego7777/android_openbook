package com.example.openbook.Chatting;

import android.content.Intent;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.ChattingAdapter;
import com.example.openbook.Activity.Menu;
import com.example.openbook.R;
import com.example.openbook.View.ChattingList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChattingUI extends AppCompatActivity {

    String TAG ="chatUI";

    String get_id;
    int table_num;
    String time;


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

    Boolean loop = true;

    public static final int MSG_CONNECT = 1;
    public static final int MSG_RECEIVE = 2;
    public static final int MSG_SEND = 3;
    public static final int MSG_CLIENT_STOP = 4;
    public static final int MSG_SERVER_STOP = 5;
    public static final int MSG_ERROR = 6;


    LocalDateTime localTime = LocalDateTime.now();
    ClientSocket clientSocket;

    BufferedWriter networkWrite = null;
    updateUI updateUI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chatting);

        table_num = getIntent().getIntExtra("tableNumber",0);
        get_id = getIntent().getStringExtra("id");
        clientSocket = (ClientSocket) getIntent().getSerializableExtra("clientSocket");


        version ++;

        dbHelper = new DBHelper(ChattingUI.this,  version);
        sqLiteDatabase = dbHelper.getWritableDatabase();

        clientSocket = new ClientSocket("3.36.255.141", 7777, get_id);
        clientSocket.start();

        /**
         * AppBar 설정
         */
        TextView menu = findViewById(R.id.menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Menu.class);
                intent.putExtra("id", get_id);
                startActivity(intent);
            }
        });

        TextView table = findViewById(R.id.table);
        table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        TextView table_number = findViewById(R.id.table_number);
        table_number.setText(get_id);

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
        Cursor res = dbHelper.getChattingData();

        if(res.getCount() == 0){
            Log.d(TAG, "SQlite에서 데이터를 찾을 수 없습니다.");
        }


        while(res.moveToNext()){
            //sender가 get_id인 것은 viewType 1로
            if(res.getString(3).equals(get_id) && res.getString(4).equals("table" + table_num)){
                chatLists.add(new ChattingList(res.getString(1), 1, res.getString(2),""));

                //receiver가 table_num인 것은 viewType 0으로
            }else if(res.getString(3).equals("table" + table_num) && res.getString(4).equals(get_id)){
                chatLists.add(new ChattingList(res.getString(1), 0, res.getString(2),""));
            }
        }

        Log.d(TAG, "getSenderData : " + res.toString());

        chattingAdapter.setAdapterItem(chatLists);
        chatting_view.requestFocus(chatLists.size());

        if(clientSocket.socket.isConnected()){
            updateUI = new updateUI();
            updateUI.start();
        }



        //핸들러 스레드를 생성하고 실행시킨다.
        thread = new HandlerThread("HandlerThread");
        thread.start();

        //핸들러 스레드로부터 루퍼를 얻는다.
        mServiceLooper = thread.getLooper();

        //핸들러 스레드가 제공한 루퍼객체를 매개변수로 핸들러 객체 생성
        mServiceHandler = new ServiceHandler(mServiceLooper);

        //메인 스레드 핸들러
        mMainHandler = new Handler(Looper.getMainLooper()){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                String m;
                switch (msg.what){
                    case MSG_CONNECT:
                        m = "정상적으로 서버에 접속하였습니다.";
                        break;

                    case MSG_CLIENT_STOP:
                        m="클라이언트가 접속을 종료하였습니다.";
                        break;

                    case MSG_SERVER_STOP:
                        //서버에 의해 연결이 종료될 때 호출
//                        text.setText((String)msg.obj);
                        m ="서버가 접속을 종료하였습니다.";
                        break;

                    case MSG_SEND:
                        //메세지 전송 시 호출
                        m="메세지 전송 완료!";

                        /**
                         * sendMsg[0] : 내 테이블 번호 (get_id)
                         * sendMsg[1] : 메세지
                         * sendMsg[2] : 보낸 테이블 번호 (table + table_num)
                         */
                        String sendMsg[] = msg.obj.toString().split("_");

                        time =  localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                        chatLists.add(new ChattingList(sendMsg[1], 1, time, ""));
                        Log.d(TAG, "전송된 메세지 : " +sendMsg[1]);

                        chattingAdapter.setAdapterItem(chatLists);
                        chatting_view.smoothScrollToPosition(chatLists.size());

                        //정상적으로 리사이클러뷰에 올라가면 db에 저장
                        dbHelper.insertData(sendMsg[1], time, get_id, sendMsg[2], "");

//                        imm.hideSoftInputFromWindow(chat_edit.getWindowToken(), 0);

                        msg.obj=null;
                        chat_edit.setText((String)msg.obj);

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
        chat_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




        /**
         * 보내기 버튼 누르면 상대방 채팅방에도 보내는 것으로...!!!
         */
        chat_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(chat_edit.getText().toString() != null){

                    //ClientSocket 핸들러로부터 메세지를 하나 반환받는다
                    Message msg = mServiceHandler.obtainMessage();

                    msg.what = MSG_SEND;
                    msg.obj = get_id + "_" + chat_edit.getText().toString()+ "_table" + table_num;
                    Log.d(TAG, "msg 전송 :" + (String) msg.obj);

                    mServiceHandler.sendMessage(msg);

                }
            }
        });


    }

    class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(Message msg){
            Message toMain = mMainHandler.obtainMessage();

            switch (msg.what){
                case MSG_SEND:
                    try{
                        networkWrite = new BufferedWriter
                                (new OutputStreamWriter(clientSocket.socket.getOutputStream()));

                        networkWrite.write((String) msg.obj);
                        networkWrite.newLine();
                        networkWrite.flush();

//                        메세지가 성공적으로 전송되었다면 전송한 문자열 화면에 출력
                        toMain.what =MSG_SEND;

                    }catch (IOException e){
                        toMain.what = MSG_ERROR;
                        Log.d(TAG, "handleMessage: e " + e);
                    }
                    toMain.obj = msg.obj;
                    mMainHandler.sendMessage(toMain);
                    break;


                case MSG_CLIENT_STOP: case MSG_SERVER_STOP:
                    //quit 메소드 호출
                    clientSocket.quit();
//                    Log.d(TAG, "handleMessage: quit");
                    clientSocket = null;
                    break;
            }
        }

    } //ServiceHandler inner class

    public class updateUI extends Thread{

        BufferedReader networkReader;

        {
            try {
                networkReader = new BufferedReader(
                        new InputStreamReader(clientSocket.socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            super.run();

            while (loop) {
                try {
                    String line = networkReader.readLine();

                    //서버로부터 FIN 패킷(서버로 연결된 세션의 종료를 알리는 패킷)을 수신하면 read() 메소드는 null을 반환
                    if (line == null)
                        break;

                    Runnable showUpdate = new Runnable() {
                        @Override
                        public void run() {

                            String temp[] = line.split("_");
//                            int chatListLast = 0;

                            if(temp[0].equals("read")){

                                for(int i=0; i<chatLists.size(); i++) {
                                    chatLists.get(i).setRead("읽음");
                                }
//                                Log.d(TAG, "chatListLast : " + chatListLast);

//                                chatLists.get(chatListLast).setRead("읽음");
                                chattingAdapter.notifyItemChanged(chatLists.lastIndexOf(1));



                            }else{
                                //table1 형태
                                String receiver = temp[1];

                                temp[1] = temp[1].replace("table", "");


                                if(String.valueOf(table_num).equals(temp[1])){
                                    //처음엔 읽었는지 안읽었는지 모르니까 공란으로 넘겨버림
                                    chatLists.add(new ChattingList(temp[0], 0, localTime.format(DateTimeFormatter.ofPattern("HH:mm")), ""));
                                    Log.d(TAG, "showUpdate : " +line);
                                    chattingAdapter.setAdapterItem(chatLists);
                                    chatting_view.smoothScrollToPosition(chatLists.size());

                                    time =  localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

                                    dbHelper.insertData(temp[0], time, receiver, get_id, "");
                                    Log.d(TAG, "전송 받은거 저장");
                                } // 해당 테이블에 데이터 넣어주기
                            } // 읽음 처리 if문

                        }
                    };

                    mMainHandler.post(showUpdate);

                } catch (InterruptedIOException e) {

                } catch (IOException e) {
                    Log.d(TAG, "run: e " + e);
                    break;
                }
            }

        }
    }
}
