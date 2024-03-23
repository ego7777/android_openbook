package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.Data.ChattingData;
import com.example.openbook.Data.MyData;
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.R;
import com.example.openbook.Data.TicketData;
import com.example.openbook.Data.TableList;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PopUpChatting extends Activity {

    String TAG = "chattingPopUpTAG";
    int tableNumber;

    String fcmData, title, body;

    int clickTable;

    MyData myData;
    HashMap<String, ChattingData> chattingDataHashMap;
    HashMap<String, TicketData> ticketDataHashMap;
    ArrayList<TableList> tableList;

    TextView popup_title, popup_body;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup);

        popup_title = findViewById(R.id.popup_title);
        popup_body = findViewById(R.id.popup_body);


        fcmData = getIntent().getStringExtra("fcmData");
        Log.d(TAG, "fcm Data: " + fcmData);

        handleFCMData(fcmData);



        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

        myData = (MyData) getIntent().getSerializableExtra("myData");
//        Log.d(TAG, "myData ID :" + myData.getId());
//        Log.d(TAG, "myData IsOrder: " + myData.isOrder());

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
//        Log.d(TAG, "intent chattingData: " + chattingDataHashMap);

        ticketDataHashMap = (HashMap<String, TicketData>) getIntent().getSerializableExtra("ticketData");
//        Log.d(TAG, "intent ticketData :" + ticketDataHashMap);

        if(chattingDataHashMap == null){
            chattingDataHashMap = new HashMap<>();
        }

        if(ticketDataHashMap == null){
            ticketDataHashMap = new HashMap<>();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        Button popup_yes = findViewById(R.id.popup_button_yes);
        Button popup_no = findViewById(R.id.popup_button_no);


        /**
         * 받는 쪽 event
         */

        if (body.contains("요청")) {

            popup_yes.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    // 여기서 yes를 누르면 그 후로는 바로 chatUI로 넘어가게 해야함
                    String requestTable = "table" + tableNumber;

                    //수락을 누르면 상대방 테이블에 요청을 수락했다고 보내주기
                    SendNotification sendNotification = new SendNotification();
                    sendNotification.requestChatting(title, myData.getId(), "", "에서 채팅을 수락하였습니다.");

                    Intent intent = new Intent(PopUpChatting.this, ChattingUI.class);
                    intent.putExtra("myData", myData);
                    intent.putExtra("tableNumber", tableNumber);

                    ticketDataHashMap.put(requestTable, new TicketData(requestTable, false, requestTable));
                    chattingDataHashMap.put(requestTable, new ChattingData(requestTable, true, false));

                    intent.putExtra("chattingData", chattingDataHashMap);
                    intent.putExtra("ticketData", ticketDataHashMap);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }
            });


            popup_no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /**
                     * 해당 테이블에 요청 못하게 막기
                     */

                    String requestTable = "table" + tableNumber;

                    Intent intent = new Intent(PopUpChatting.this, Table.class);
                    intent.putExtra("myData", myData);
                    intent.putExtra("ticketData", ticketDataHashMap);

                    chattingDataHashMap.put(requestTable, new ChattingData(requestTable, false, false));
                    intent.putExtra("chattingData", chattingDataHashMap);

                    startActivity(intent);
                    finish();
                }
            });


            /**
             * 보낸 쪽 event
             */

        } else if (body.contains("수락")) {
            popup_yes.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "수락포함");

                    Intent intent = new Intent(PopUpChatting.this, ChattingUI.class);
                    intent.putExtra("myData", myData);
                    intent.putExtra("tableNumber", tableNumber);

                    ticketDataHashMap.put("table" + tableNumber, new TicketData(null, false, null));
                    chattingDataHashMap.put("table" + tableNumber,
                            new ChattingData("table" + tableNumber, true, false));

                    intent.putExtra("ticketData", ticketDataHashMap);
                    intent.putExtra("chattingData", chattingDataHashMap);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });

            popup_no.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("myData", myData);
                    intent.putExtra("chattingData", chattingDataHashMap);
                    intent.putExtra("ticketData", ticketDataHashMap);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    setResult(Activity.RESULT_OK, intent);

                    finish();
                }
            });

        }


        popup_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 해당 테이블에 요청 못하게 막기
                 */

                if (ticketDataHashMap == null) {
                    ticketDataHashMap = new HashMap<>();
                    Log.d(TAG, "new tableInformation");
                }

                String tableNumberTemp = "table" + clickTable;

                if (ticketDataHashMap.get(tableNumberTemp) != null) {
                    ticketDataHashMap.get(tableNumberTemp).setWhoBuy(myData.getId());
                    ticketDataHashMap.get(tableNumberTemp).setUseTable(tableNumberTemp);

                    chattingDataHashMap.get(ticketDataHashMap).setBlock(true);
                } else {
                    ticketDataHashMap.put(tableNumberTemp,
                            new TicketData(myData.getId(), false, tableNumberTemp));
                    chattingDataHashMap.put(tableNumberTemp,
                            new ChattingData(myData.getId(), false, true));
                }


                Log.d(TAG, "tableInformation add :" + ticketDataHashMap.get(tableNumberTemp));

                Intent intent = new Intent(PopUpChatting.this, Table.class);
                intent.putExtra("myData", myData);
                intent.putExtra("chattingData", chattingDataHashMap);
                intent.putExtra("ticketData", ticketDataHashMap);
                startActivity(intent);
                finish();
            }
        });


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥 레이어 클릭해도 안닫히게 하기
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    public void handleFCMData(String fcmData) {

        try {
            JSONObject jsonObject = new JSONObject(fcmData);
            Log.d(TAG, "json: ");
            title = jsonObject.getString("title");
            Log.d(TAG, "json title: " + title);
            body = jsonObject.getString("body");
            Log.d(TAG, "json body: " + body);
            String ticket = jsonObject.getString("ticket");
            Log.d(TAG, "ticket: " + ticket);
            String myTableID = jsonObject.getString("clickTable");
            Log.d(TAG, "clickTable: " + myTableID);

            tableNumber = Integer.parseInt(title.replace("table", ""));

            popup_title.setText(title);
            popup_body.setText(body);

        } catch (JSONException e) {
            Log.d(TAG, "handleFCMData error: " + e);
        }


    }



}
