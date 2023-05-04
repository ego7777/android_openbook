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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.Chatting.Client;
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.FCMclass.SendNotification;
import com.example.openbook.R;
import com.example.openbook.TableInformation;
import com.example.openbook.View.TableList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PopUp extends Activity {

    String TAG = "POPUP_Activity";
    String get_id;
    int tableNumber;

    String title;
    String body;
    int clickTable;
    boolean orderCk;
    ArrayList<TableList> tableList;

    HashMap<Integer, TableInformation> tableInformationHashMap;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup);

        TextView popup_title = findViewById(R.id.popup_title);
        TextView popup_body = findViewById(R.id.popup_body);

        title = getIntent().getStringExtra("notificationTitle");

        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");


        if(title == null){
            title = getIntent().getStringExtra("title");
            Log.d(TAG, "title :" + title);
        }else{
            tableNumber = Integer.parseInt(title.replace("table", ""));
        }

        body = getIntent().getStringExtra("notificationBody");

        if(body == null){
            body = getIntent().getStringExtra("body");
            Log.d(TAG, "body :" + body);
        }

        get_id = getIntent().getStringExtra("notificationClickTable");

        if(get_id == null){
            get_id = getIntent().getStringExtra("get_id");
            Log.d(TAG, "get_id :" + get_id);
        }

        clickTable = getIntent().getIntExtra("clickTable", 0);
        Log.d(TAG, "clickTable :" + "table"+clickTable);

        tableInformationHashMap = (HashMap<Integer, TableInformation>) getIntent().getSerializableExtra("tableInformation");
        Log.d(TAG, "intent tableInformation :" + tableInformationHashMap);
        if(tableInformationHashMap == null){
            tableInformationHashMap = new HashMap<>();
            Log.d(TAG, "initial tableInformation");
        }
        orderCk = getIntent().getBooleanExtra("orderCk", false);
        Log.d(TAG, "PopUp orderCk :" + orderCk);

        popup_title.setText(title);
        popup_body.setText(body);


    }


    @Override
    protected void onResume() {
        super.onResume();

        Button popup_yes = findViewById(R.id.popup_button_yes);
        Button popup_no = findViewById(R.id.popup_button_no);

        /**
         * 받는 쪽 event
         */

        if(body.contains("요청")){
            popup_yes.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "요청포함");
                    // 여기서 yes를 누르면 그 후로는 바로 chatUI로 넘어가게 해야함

                    //수락을 누르면 상대방 테이블에 요청을 수락했다고 보내주기
                    SendNotification sendNotification = new SendNotification();
                    sendNotification.requestChatting(title,get_id, "","에서 채팅을 수락하였습니다.");


                    Intent intent = new Intent(PopUp.this, ChattingUI.class);
                    intent.putExtra("get_id", get_id);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("orderCk", orderCk);
                    intent.putExtra("tableList", tableList);


                    tableInformationHashMap.put(tableNumber,
                            new TableInformation("table"+tableNumber,
                                    false, tableNumber, true, false));
                    intent.putExtra("tableInformation", tableInformationHashMap);

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
                }
            });


            /**
             * 보낸 쪽 event
             */

        }else if(body.contains("수락")){
            popup_yes.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "수락포함");

                    Intent intent = new Intent(PopUp.this, ChattingUI.class);
                    intent.putExtra("get_id", get_id);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("orderCk", orderCk);
                    intent.putExtra("tableList", tableList);

                    tableInformationHashMap.put(tableNumber,
                            new TableInformation(null, false, 0, true, false));
                    intent.putExtra("tableInformation", tableInformationHashMap);
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
                    intent.putExtra("get_id", get_id);
//                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("orderCk", orderCk);
                    intent.putExtra("tableInformation", tableInformationHashMap);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    setResult(Activity.RESULT_OK, intent);

                    finish();
                }
            });

            /**
             * 프로필 포함 요청
             */
        }else if(body.contains("프로필")){
            popup_yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "yes click");

                    if(tableInformationHashMap == null){
                        tableInformationHashMap = new HashMap<>();
                        Log.d(TAG, "new tableInformation");
                    }

                    if(tableInformationHashMap.get(clickTable) != null){
                        tableInformationHashMap.get(clickTable).setWhoBuy(get_id);
                        tableInformationHashMap.get(clickTable).setUseTable(clickTable);
                    }else{
                        tableInformationHashMap.put(clickTable,
                                new TableInformation(get_id, false, clickTable, false, false));
                    }


                    Log.d(TAG, "tableInformation add :" + tableInformationHashMap.get(clickTable));

                    Intent intent = new Intent(PopUp.this, Table.class);
                    intent.putExtra("tableInformation", tableInformationHashMap);
                    intent.putExtra("get_id", get_id);
                    intent.putExtra("orderCk", orderCk);
                    intent.putExtra("tableList", tableList);
//                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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

                if(tableInformationHashMap == null){
                    tableInformationHashMap = new HashMap<>();
                    Log.d(TAG, "new tableInformation");
                }

                if(tableInformationHashMap.get(clickTable) != null){
                    tableInformationHashMap.get(clickTable).setWhoBuy(get_id);
                    tableInformationHashMap.get(clickTable).setUseTable(clickTable);
                    tableInformationHashMap.get(clickTable).setBlock(true);
                }else{
                    tableInformationHashMap.put(clickTable,
                            new TableInformation(get_id, false, clickTable, false, true));
                }


                Log.d(TAG, "tableInformation add :" + tableInformationHashMap.get(clickTable));

                Intent intent = new Intent(PopUp.this, Table.class);
                intent.putExtra("tableInformation", tableInformationHashMap);
                intent.putExtra("get_id", get_id);
                intent.putExtra("orderCk", orderCk);
//                intent.putExtra("tableList", tableList);
//                  intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        });



    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥 레이어 클릭해도 안닫히게 하기
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


}
