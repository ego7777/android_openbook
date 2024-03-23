package com.example.openbook.Chatting;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SocketService extends Service {
    private SocketThread socketThread;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 시작되었을 때 실행되는 로직
        socketThread = new SocketThread();
        socketThread.start();
        return START_STICKY; // 서비스가 강제로 종료되었을 때 재시작
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료되었을 때 실행되는 로직
        if (socketThread != null) {
            socketThread.stopThread();
        }
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class SocketThread extends Thread {
        private boolean running = true;

        @Override
        public void run() {
            // 소켓 연결 및 데이터 수신 작업 수행
            while (running) {
                // 소켓 데이터 수신
                String data = receiveDataFromSocket();

                // 데이터 처리
                processData(data);
            }
        }

        public void stopThread() {
            running = false;
        }
    }

    private String receiveDataFromSocket() {
//        try {
            // 소켓으로부터 데이터를 읽어옴
//            InputStream inputStream = socket.getInputStream();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

//            String data = reader.readLine(); // 한 줄씩 데이터를 읽음
//            return data;
//        } catch (IOException e) {
//            e.printStackTrace();
            // 예외 처리 로직
//        }

        return null;
    }



    private void processData(String data) {
        // 데이터의 형태에 따라 각각의 작업을 수행하여 UI를 업데이트
        if (data.startsWith("A")) {
            // A로 시작하는 경우 Table 액티비티의 UI 업데이트 수행
            updateTableUI(data);
        } else if (data.startsWith("B")) {
            // B로 시작하는 경우 Chatting 액티비티의 UI 업데이트 수행
            updateChattingUI(data);
        }
    }

    private void updateTableUI(String data) {
        // Table 액티비티의 UI 업데이트 작업 수행
    }

    private void updateChattingUI(String data) {
        // Chatting 액티비티의 UI 업데이트 작업 수행
    }
}
