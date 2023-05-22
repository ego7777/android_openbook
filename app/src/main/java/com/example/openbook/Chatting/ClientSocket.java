package com.example.openbook.Chatting;


import android.os.Build;

import android.util.Log;

import androidx.annotation.RequiresApi;


import com.example.openbook.Data.TableList;

import java.io.BufferedWriter;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;



@RequiresApi(api = Build.VERSION_CODES.O)
public class ClientSocket extends Thread implements Serializable{

    private final String TAG = "ClientSocket";
    SocketAddress socketAddress;

    final int connection_timeout = 999999;
    BufferedWriter networkWrite;

    String get_id;

    public static Socket getSocket() {
        return socket;
    }

    private static Socket socket;
    boolean loop;

//    Context context;

    public ArrayList<TableList> getTableList() {
        return tableList;
    }

    ArrayList<TableList> tableList;



    public ClientSocket(String ip, int port, String get_id, ArrayList<TableList> tableList) {
        //서버 ip 주소와 사용할 포트번호로 소켓 어드레스 객체 생성
        this.get_id = get_id;
        this.tableList = tableList;
        socketAddress = new InetSocketAddress(ip, port);

    }


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

//            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());


            OutputStreamWriter o = new OutputStreamWriter(socket.getOutputStream());
            //outputStream: 출력 스트림
            networkWrite = new BufferedWriter(o);
//            Log.d(TAG, "run: " + networkWrite);


            /**
             * 소켓이 생성이 되면 테이블 Id 값을 넘겨준다.
             *
             */

//            objectOutputStream.writeObject(get_id+"_table");
//            objectOutputStream.flush();

            networkWrite.write(get_id + "_table");
            networkWrite.newLine();
            networkWrite.flush();
            Log.d(TAG, "id flush");

            networkWrite = null;





        } catch (Exception e) {
//            loop = false;
            Log.d(TAG, "소켓을 생성하지 못했습니다.");
            Log.d(TAG, "Exception " + e);

        }

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




