package com.example.openbook.Chatting;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openbook.BuildConfig;
import com.example.openbook.MessageDTO;
import com.google.gson.Gson;

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


public class ClientSocket extends Thread implements Serializable {

    private final String TAG = "ClientSocket";
    SocketAddress socketAddress;

    final int connectionTimeout = 999999;
    BufferedWriter networkWrite;
    String id;

    public Socket socket;
    boolean loop;
    Context context;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    Gson gson = new Gson();

    public ClientSocket(String id, Context context) {
        this.id = id;
        this.context = context;
        socketAddress = new InetSocketAddress(BuildConfig.IP, 7777);

    }

    //액티비티가 onCreate 되면 자동으로 받는거고
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("SendChattingData")) {
                String message = intent.getStringExtra("sendToServer");
                Log.d(TAG, "onReceive message: " + message);

                Thread thread = new Thread(() -> sendToServer(message));
                thread.start();

            }
        }
    };


    @Override
    public void run() {
        super.run();

        try {

            socket = new Socket();
            socket.setSoTimeout(connectionTimeout);
            socket.setSoLinger(true, connectionTimeout);

            socket.connect(socketAddress, connectionTimeout);

            BufferedReader networkReader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            MessageDTO messageDTO = new MessageDTO("", id, "add");
            sendToServer(gson.toJson(messageDTO));

            // 로컬 브로드캐스트 리시버 등록
            IntentFilter intentFilter = new IntentFilter("SendChattingData");
            LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);


            while (loop) {

                String line = networkReader.readLine();
                MessageDTO messageDiv = gson.fromJson(line, MessageDTO.class);

                if (line.contains("[")) {

                    receiveTableInfo(line);

                } else if (messageDiv.getIsRead() == 0) {
                    Log.d(TAG, "isRead: ");
                    receiveIsRead(line);

                } else {
                    receiveChattingData(line);
                }

                if (line == null) {
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

    private void sendToServer(String message) {

        try {
            OutputStreamWriter o = new OutputStreamWriter(socket.getOutputStream());
            networkWrite = new BufferedWriter(o);

            networkWrite.write(message);
            networkWrite.newLine();
            networkWrite.flush();
            Log.d(TAG, "sendToServer: " + message);

            networkWrite = null;

            loop = true;


        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void receiveTableInfo(String line) {

        sharedPreferences = context.getSharedPreferences("CustomerData", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        editor.putString("ActiveTable", line);
        editor.commit();


        Intent intent = new Intent("tableInformationArrived");
        intent.putExtra("tableInformation", line);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private void receiveChattingData(String line) {

        Intent intent = new Intent("chattingDataArrived");
        intent.putExtra("chattingData", line);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private void receiveIsRead(String line) {
        Log.d(TAG, "receiveIsRead: ");

        MessageDTO messageDTO = gson.fromJson(line, MessageDTO.class);
        String from = messageDTO.getFrom();
        int isRead = messageDTO.getIsRead();


        DBHelper dbHelper = new DBHelper(context, 2);
        //db 수정하기
        dbHelper.upDateIsRead(id, from);

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




