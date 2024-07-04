package com.example.openbook.Chatting;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.openbook.BuildConfig;
import com.example.openbook.DBHelper;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class ClientSocket extends Thread implements Serializable {

    private final String TAG = "ClientSocket";
    SocketAddress socketAddress;

    final int connectionTimeout = 999999;
    BufferedWriter networkWrite;
    String id;
    public Socket socket;
    boolean loop;
    Context context;
    Gson gson = new Gson();
    DBHelper dbHelper;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    public ClientSocket(String id, Context context) {
        this.id = id;
        this.context = context;
        socketAddress = new InetSocketAddress(BuildConfig.IP, 7777);

    }

    //액티비티가 onCreate 되면 자동으로 받는거고
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("SendChattingData")) {
                String message = intent.getStringExtra("sendToServer");
                Log.d(TAG, "onReceive message: " + message);
                String disconnect = intent.getStringExtra("socketDisconnect");
                Log.d(TAG, "onReceive disconnect: " + disconnect);
                if(message != null){
                    new Thread(() -> sendToServer(message)).start();

                }else if(disconnect != null){
                    quit(disconnect);
                }

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

            MessageDTO messageDTO = new MessageDTO("", id, "connect", "");
            sendToServer(gson.toJson(messageDTO));

            IntentFilter intentFilter = new IntentFilter("SendChattingData");
            LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);

            dbHelper = new DBHelper(context);
            sharedPreferences = context.getSharedPreferences("CustomerData", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            while (loop) {

                String newChat = networkReader.readLine();
                Log.d(TAG, "newChat: " + newChat);
                MessageDTO messageDiv = gson.fromJson(newChat, MessageDTO.class);

                switch (messageDiv.getMessage()) {
                    case "newTable":
                        updateNewTable(messageDiv.getFrom());
                        break;

                    case "isRead":
                        receiveIsRead(messageDiv.getFrom());
                        break;

                    case "existingTable":
                        Log.d(TAG, "existingTable: " + messageDiv.getFrom());
                        activeExistingTables(messageDiv.getFrom());
                        break;

                    case "disconnect" :
                        Log.d(TAG, "disconnect: " + messageDiv.getFrom());
                        updateDisconnectTable(messageDiv.getFrom());
                        break;

                    default:
                        receiveChattingData(newChat);
                        break;
                }

                if (newChat == null) {
                    Log.d(TAG, "newChat is null");
                    break;
                }
            }

        } catch (Exception e) {
            loop = false;
            Log.d(TAG, "소켓을 생성하지 못했습니다.");
            Log.d(TAG, "Exception " + e);

        }finally {
            loop = false;
            if (socket != null && !socket.isClosed()) {
                try {
                    // 서버에 종료를 알리는 메시지 전송
                    MessageDTO messageDTO = new MessageDTO("", id, "disconnect", "");
                    sendToServer(gson.toJson(messageDTO));

                    // 소켓 닫기
                    socket.close();
                    Log.d(TAG, "Socket closed in finally block.");
                } catch (IOException e) {
                    Log.d(TAG, "Failed to close socket in finally block: " + e);
                }
            }
        }

    }

    private void updateDisconnectTable(String disconnectTable){
        Log.d(TAG, "updateDisconnectTable: " + disconnectTable);

        String activeTable = sharedPreferences.getString("activeTableList", null);
        Set<Integer> activeTableSet;

        if(activeTable==null){
            activeTableSet = new HashSet<>();
        }else{
            Type tableType = new TypeToken<Set<Integer>>() {}.getType();
            activeTableSet = gson.fromJson(activeTable, tableType);
        }

        int tableNumber = Integer.parseInt(disconnectTable.replace("table", ""));

        activeTableSet.remove(tableNumber);
        Log.d(TAG, "updateDisconnectTable: " +gson.toJson(activeTableSet));
        editor.putString("activeTableList", gson.toJson(activeTableSet));
        editor.commit();

        dbHelper.deleteCompletedTableChatMessage(disconnectTable);

        Intent intent = new Intent("updateNewTable");
        intent.putExtra("disconnectTable", tableNumber);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
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
            Log.d(TAG, "sendToServer e: " + e.getMessage());
            e.printStackTrace();
        }

    }


    private void updateNewTable(String newTable) {

        String activeTable = sharedPreferences.getString("activeTableList", null);
        Set<Integer> activeTableSet;

        if(activeTable==null){
            activeTableSet = new HashSet<>();
        }else{
            Type tableType = new TypeToken<Set<Integer>>() {}.getType();
            activeTableSet = gson.fromJson(activeTable, tableType);

        }
        int tableNumber = Integer.parseInt(newTable.replace("table", ""));

        activeTableSet.add(tableNumber);
        Log.d(TAG, "updateNewTable add new table set: " + gson.toJson(activeTableSet));
        editor.putString("activeTableList", gson.toJson(activeTableSet));
        editor.commit();

        Intent intent = new Intent("updateNewTable");
        intent.putExtra("newTable", tableNumber);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private void activeExistingTables(String activeTableList){
        Log.d(TAG, "activeExistingTables: " + activeTableList);
        editor.putString("activeTableList", activeTableList);
        editor.commit();

        Intent intent = new Intent("updateNewTable");
        intent.putExtra("existingTables", activeTableList);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void receiveChattingData(String newChat) {
        Log.d(TAG, "receiveChattingData: " + newChat);

        MessageDTO messageDiv = gson.fromJson(newChat, MessageDTO.class);

        DBHelper dbHelper = new DBHelper(context);
        dbHelper.insertChattingData
                (messageDiv.getMessage(),
                messageDiv.getTime(),
                messageDiv.getFrom(),
                id,
                "1");

        String key = "isNotRead"  + messageDiv.getFrom();
        int isNotRead = sharedPreferences.getInt(key, 1000);

        if(isNotRead != 1000){
            isNotRead +=1;
        }else{
            isNotRead = 1;
        }
        editor.putInt(key, isNotRead);
        editor.commit();

        //여기서 쉐어드에 하트를 저장하자! 꺼내서 있으면 더해서 저장, 없으면 그냥 1저장!!!해서 onCreate가 테이블에서 되면! 꺼내서 붙이자!

        Intent intent = new Intent("newChatArrived");
        intent.putExtra("chat", newChat);
        intent.putExtra("isNotReadChat", messageDiv.getFrom());
        intent.putExtra("newChatTable", messageDiv.getFrom());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

    }

    private void receiveIsRead(String from) {
        Log.d(TAG, "receiveIsRead: ");

        dbHelper.upDateIsRead(id, from);

        Intent intent = new Intent("isReadArrived");
        intent.putExtra("readTable", from);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }


    public void quit(String messageDTO) {
        Log.d(TAG, "client socket quit");
        loop = false;
        try {
            if (socket != null) {
                new Thread(() -> {
                    sendToServer(messageDTO);
                    try {
                        socket.close();
                        socket = null;
                    } catch (IOException e) {
                        Log.d(TAG, "quit: e" + e);
                    }
                }).start();
            }

        } catch (Exception e) {
            Log.d(TAG, "quit: e" + e);
        }
    }

}




