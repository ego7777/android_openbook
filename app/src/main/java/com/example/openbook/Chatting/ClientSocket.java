package com.example.openbook.Chatting;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


import com.example.openbook.Activity.Table;
import com.example.openbook.Data.TableList;
import com.example.openbook.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;


@RequiresApi(api = Build.VERSION_CODES.O)
public class ClientSocket extends Thread implements Serializable{

    private final String TAG = "ClientSocket";
    SocketAddress socketAddress;

    final int connection_timeout = 999999;
    BufferedWriter networkWrite;

    String get_id;

    public Socket socket;
    boolean loop;


    Context context;



    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;




    public ClientSocket(String get_id, Context context) {
        //서버 ip 주소와 사용할 포트번호로 소켓 어드레스 객체 생성
        this.get_id = get_id;
        this.context = context;
        socketAddress = new InetSocketAddress("3.36.255.141", 7777);

    }

    //액티비티가 onCreate 되면 자동으로 받는거고
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SendChattingData")) {
                String message = intent.getStringExtra("sendToServer");
                Log.d(TAG, "onReceive message: " + message);

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        sendToServer(message);
                    }
                });
                thread.start();

            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void run() {
        super.run();
        //
        try {
            /**
             * 1. 클라이언트 소켓생성
             */
            socket = new Socket();
            socket.setSoTimeout(connection_timeout);
            socket.setSoLinger(true, connection_timeout);


            /** 2. 소켓 연결
             /블록모드로 작동하는 connect() 메소드에서 반환되면 서버와 정상적으로 연결된 것
             */
            socket.connect(socketAddress, connection_timeout);
            Log.d(TAG, "소켓 연결 : " + socket.isConnected());


            //3. 데이터 입출력 메소드 설정

            BufferedReader networkReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            /**
             * 소켓이 생성이 되면 테이블 Id 값을 넘겨준다.
             */
            sendToServer(get_id + "_table");

            // 로컬 브로드캐스트 리시버 등록
            IntentFilter intentFilter = new IntentFilter("SendChattingData");
            LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);


            while(loop){

                String line = networkReader.readLine();
                Log.d(TAG, "line: " + line);



                if(line.contains("[")){

                    receiveTableInfo(line);

                }else if(line.contains("isRead")){
                    Log.d(TAG, "isRead: ");
                    receiveIsRead(line);

                }else{
                    receiveChattingData(line);
                }

                if(line == null){
                    Log.d(TAG, "line null: ");
                    break;
                }
            }

        } catch (Exception e) {
//            loop = false;
            Log.d(TAG, "소켓을 생성하지 못했습니다.");
            Log.d(TAG, "Exception " + e);

        }

    }

    private void sendToServer(String message){

        try {
            OutputStreamWriter o =  new OutputStreamWriter(socket.getOutputStream());
            networkWrite = new BufferedWriter(o);

            networkWrite.write(message);
            networkWrite.newLine();
            networkWrite.flush();
            Log.d(TAG, "id flush");

            networkWrite = null;

            loop = true;


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void receiveTableInfo(String line){

        sharedPreferences = context.getSharedPreferences("ActiveTable", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putString("ActiveTable", line);
        editor.commit();


        Intent intent =new Intent("tableInformationArrived");
        intent.putExtra("tableInformation", line);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private void receiveChattingData(String line){
        /**
         * sendMsg[0] : message
         * sendMsg[1] : 보낸 테이블 번호 (table + table_num)
         * sendMsg[2] : 안읽음(1)/읽음(0)
         */
        String sendMsg[] = line.split("_");


        LocalDateTime localTime = LocalDateTime.now();

        String time = localTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        //db에 저장
        DBHelper dbHelper = new DBHelper(context, 2);
        dbHelper.insertChattingData(sendMsg[0], time, get_id, sendMsg[1], sendMsg[2]);


        Intent intent = new Intent("chattingDataArrived");
        intent.putExtra("chattingData", line);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void receiveIsRead(String line){
        /**
         * sendMsg[1] : isRead
         * sendMsg[0] : 보낸 테이블 번호
         */
        Log.d(TAG, "receiveIsRead: ");

        String sendMsg[] = line.split("_");

        //table1 형태
        String receiver = sendMsg[1];
        Log.d(TAG, "receiver: " + receiver);

        DBHelper dbHelper = new DBHelper(context, 2);
        //db 수정하기
        dbHelper.upDateIsRead(get_id, receiver);

//        Cursor cursor = dbHelper.getTableData("chattingTable");
//        if(cursor.getString(3).equals(get_id)){
//            Log.d(TAG, "수정 됨?: " + cursor.getString(5));
//        }

        Log.d(TAG, "receiveIsRead: 1");

        Intent intent = new Intent("isReadArrived");
        intent.putExtra("isRead", line);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

        Log.d(TAG, "receiveIsRead: 2");


    }



    public void quit() {
        loop = false;
        try {
            if (socket != null) {
                socket.close();
                socket = null;
                Log.d(TAG, "quit: ");
            }


        } catch (IOException e) {
            Log.d(TAG, "quit: e" + e);
        }
    }


}




