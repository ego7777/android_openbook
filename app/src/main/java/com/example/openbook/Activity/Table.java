package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Adapter.TableAdapter;
import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.ChattingData;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.OrderList;
import com.example.openbook.DialogCustom;
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.ImageLoadTask;
import com.example.openbook.QRcode.MakeQR;
import com.example.openbook.R;
import com.example.openbook.Data.TicketData;
import com.example.openbook.Data.TableList;
import com.example.openbook.TableQuantity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Table extends AppCompatActivity {

    String TAG = "TableTAG";
    MyData myData;
    HashMap<String, TicketData> ticketDataHashMap;
    HashMap<String, ChattingData> chattingDataHashMap;
    ArrayList<TableList> tableList;

    //다이얼로그
    ImageView table_info_img;
    TextView table_info_statement, table_info_text, table_info_gender, table_info_member;
    Button table_info_close;


    int clickTable, myTable;
    TableAdapter adapter;

    OkHttpClient okHttpClient = new OkHttpClient();

    TextView appbarMenu, appbarOrderList, requestChatting, checkInformation;
    LinearLayout table_sidebar;

    ImageLoadTask task;
    String url;

    //액티비티가 onCreate 되면 자동으로 받는거고
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("tableInformationArrived")) {

                String message = intent.getStringExtra("tableInformation");
                tableUpdate(message);

            }else if (intent.getAction().equals("chattingRequestArrived")) {
                String fcmData = intent.getStringExtra("fcmData");

                SendToPopUpChatting sendToPopUpChatting = new SendToPopUpChatting();
                sendToPopUpChatting.sendToPopUpChatting(Table.this, myData,
                                chattingDataHashMap, ticketDataHashMap, tableList, fcmData);
            }
        }
    };


    int tablePosition;
    String responseBody;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_activity);

        overridePendingTransition(0, 0);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        Log.d(TAG, "myData Id: " + myData.getId());
        Log.d(TAG, "myData IsOrder: " + myData.isOrder());

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
        ticketDataHashMap = (HashMap<String, TicketData>) getIntent().getSerializableExtra("ticketData");
        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");
        Log.d(TAG, "tableList size: " + tableList.size());


        if(!myData.getId().equals("구글로그인")){
            myTable = Integer.parseInt(myData.getId().replace("table", ""));
            TextView table_num = findViewById(R.id.appbar_menu_table_number);
            table_num.setText(myData.getId());
        }


        /**
         * Appbar: Menu 누르면 이동
         */
        appbarMenu = findViewById(R.id.appbar_menu_menu);

        appbarOrderList = findViewById(R.id.appbar_menu_orderList);

        RecyclerView table_grid = findViewById(R.id.tableGrid);
        adapter = new TableAdapter(tableList, myTable);

        //그리드 레이아웃 설정
        table_grid.setLayoutManager(new GridLayoutManager(this, 5));

        //어댑터 연결
        table_grid.setAdapter(adapter);


        //오른쪽 사이드 메뉴
        table_sidebar = findViewById(R.id.table_sidebar);
        table_sidebar.setVisibility(View.INVISIBLE);


        requestChatting = findViewById(R.id.chatting);
        checkInformation = findViewById(R.id.take_info);

    } //onCreate


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        //로컬 브로드 캐스트 등록
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("tableInformationArrived");
        intentFilter.addAction("chattingRequestArrived");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);


        SharedPreferences sharedPreferences = getSharedPreferences("ActiveTable", MODE_PRIVATE);

        //onCreate 되기 이전에 데이터들은 저장이 되었다가 가져오는 것
        String activeTable = sharedPreferences.getString("ActiveTable", null);
        Log.d(TAG, "activeTable: " + activeTable);

        if (tableList != null ) {

            tableUpdate(activeTable);

        } else {
            TableQuantity tableQuantity = new TableQuantity();
            int tableFromDB = tableQuantity.getTableQuantity();

            tableList = new ArrayList<>();

            for (int i = 1; i < tableFromDB + 1; i++) {
                if (i == myTable) {
                    tableList.add(new TableList(myData.getId(), (Drawable) null, 0));
                } else {
                    tableList.add(new TableList(i, (Drawable) null, 1));
                }
            }

            tableUpdate(activeTable);
        }



        appbarMenu.setOnClickListener(this::moveToMenu);

        appbarOrderList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showReceiptDialog();
            }
        });


        /**
         * table 누르면 옆에 사이드 메뉴 popup
         */
        adapter.setOnItemClickListener(new TableAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                table_sidebar.setVisibility(View.VISIBLE);
                clickTable = position + 1;

                if(myData.getPaymentStyle().equals("before")){
                    requestChatting.setVisibility(View.GONE);
                }

            }
        });


        DialogCustom alertDialog = new DialogCustom();


        /**
         * 채팅하기 누르면 팝업 뜨고 채팅할 수 있도록 :
         * 1. 내가 주문을 안했으면 주문하라고 팝업이 뜨고
         * 2. 상대방이 주문을 안했으면 알려주기
         */
        requestChatting.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                if (!myData.isOrder()) {
                    alertDialog.showAlertDialog(Table.this, "주문 후 채팅이 가능합니다.");

                } else if (clickTable == myTable) {

                    alertDialog.showAlertDialog(Table.this,
                            "나의 채팅방 입니다. 다른 테이블과 채팅해보세요!");

                } else if (tableList.get(clickTable - 1).getViewType() == 2) {

                    if (chattingDataHashMap == null ||
                            !chattingDataHashMap.get("table" + clickTable).isChattingAgree()) {

                        alertDialog.chattingRequest(Table.this,
                                clickTable + "번 테이블과 채팅을 하시겠습니까?" +
                                        "\n<추신> 채팅 전 테이블 정보를 입력하는 것을 추천드립니다!",
                                "table" + clickTable, myData.getId());
                        Log.d(TAG, "채팅 신청");

                    } else if (chattingDataHashMap.get("table" + clickTable).isChattingAgree()) {

                        Intent intent = new Intent(Table.this, ChattingUI.class);
                        intent.putExtra("tableNumber", clickTable);
                        intent.putExtra("myData", myData);
                        intent.putExtra("chattingData", chattingDataHashMap);
                        intent.putExtra("ticketData", ticketDataHashMap);
                        intent.putExtra("tableList", tableList);

                        startActivity(intent);

                    }

                } else if (tableList.get(clickTable - 1).getViewType() != 2) {
                    Log.d(TAG, "비어있는 테이블");
                    alertDialog.showAlertDialog(Table.this,
                            "비어있는 테이블이거나 아직 주문하지 않은 테이블 입니다.");
                } // if-else  list.length>0 끝
            } // onClick
        }); //setOnClickListener


        /**
         * info 누르면 해당 테이블 정보 볼 수 있게
         */
        checkInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Dialog dlg = new Dialog(Table.this, R.style.RadiusDialogStyle);
                dlg.setContentView(R.layout.table_information_dialog);
                dlg.show();


                table_info_img = dlg.findViewById(R.id.table_info_img);
                table_info_text = dlg.findViewById(R.id.table_info_text);
                table_info_statement = dlg.findViewById(R.id.table_info_statement);
                table_info_gender = dlg.findViewById(R.id.table_info_gender);
                table_info_member = dlg.findViewById(R.id.table_info_member);
                table_info_close = dlg.findViewById(R.id.table_info_close);


                try {
                    String result = requestTableInfo();
                    Log.d(TAG, "url result: " + result);
                    /**
                     *  등록을 했으면 등록된 정보를 보여주고 등록 안했으면 하단 set
                     */
                    if (clickTable == myTable) {

                        handleMyTableInfo(result);

                    } else {
                        handleOtherTableInfo(result);

                    }
                } catch (InterruptedException | JSONException e) {
                    e.printStackTrace();
                }


                /**
                 * 사진을 누르면 돈내고 사진 깔거냐고 물어보기
                 */

                table_info_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (clickTable == myTable) {
                            MakeQR makeQR = new MakeQR();
                            table_info_img.setImageBitmap(makeQR.clientQR(myData.getId()));
                            table_info_text.setVisibility(View.INVISIBLE);

                        } else {

                            Intent intent = new Intent(Table.this, PopUpProfile.class);
                            intent.putExtra("title", "프로필 조회권 구매");
                            intent.putExtra("body", "프로필 조회권을 구매하시겠습니까?\n** 프로필 조회권 2000원");

                            intent.putExtra("myData", myData);
                            intent.putExtra("clickTable", clickTable);
                            intent.putExtra("chattingData", chattingDataHashMap);
                            intent.putExtra("ticketData", ticketDataHashMap);
                            intent.putExtra("tableList", tableList);

                            startActivity(intent);
                            dlg.dismiss();
                        }


                    }
                });


                table_info_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });


            }
        }); //info-click

    } //onResume

    private String requestTableInfo() throws InterruptedException {

        //                 GET 요청 객체 생성
        Request.Builder builder = new Request.Builder()
                .url("http://3.36.255.141/tableInfoCk.php")
                .get();

        builder.addHeader("table", "table" + clickTable);
        Request request = builder.build();
        Log.d(TAG, "request :" + request);

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String body = response.body().string();
                    Log.d(TAG, "requestTableInfo onResponse: " + body);
                    responseBody = body;
                } else {
                    Log.d(TAG, "requestTableInfo onResponse: fail");
                }
            }
        });


        Thread.sleep(250);


        return responseBody;
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(Table.this).unregisterReceiver(broadcastReceiver);
    }


    public void moveToMenu(View view) {
        Intent intent = new Intent(Table.this, Menu.class);
        intent.putExtra("myData", myData);
        intent.putExtra("chattingData", chattingDataHashMap);
        intent.putExtra("ticketData", ticketDataHashMap);
        intent.putExtra("tableList", tableList);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void tableUpdate(String line) {
        Log.d(TAG, "tableUpdate: ");
        int table[];

        if(line == null){
            Log.d(TAG, "tableUpdate line null: " + line);
            return;
        }

        try {
            JSONArray jsonArray = new JSONArray(line);

            table = new int[jsonArray.length()];


            for (int j = 0; j < jsonArray.length(); j++) {
                JSONObject jsonObject = jsonArray.getJSONObject(j);
                table[j] = jsonObject.getInt("table");
            }

            Arrays.sort(table);
            Log.d(TAG, "new table :" + Arrays.toString(table));

            for (int i = 0; i < table.length; i++) {
                if (table[i] != myTable) {

                    int color = getColor(R.color.skyblue);
                    tablePosition = table[i] - 1;
                    tableList.get(tablePosition).setViewType(2);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.changeItemColor(tablePosition, color);
                        }
                    });

                } else {
                    Log.d(TAG, "같음");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleMyTableInfo(String result) throws JSONException {

        if (result.equals("없음")) {
            MakeQR makeQR = new MakeQR();
            table_info_img.setImageBitmap(makeQR.clientQR(myData.getId()));
            table_info_img.setClickable(false);

            table_info_text.setVisibility(View.INVISIBLE);

            table_info_statement.setText("사진과 정보를 입력하시려면 다음 큐알로 입장해주세요 :)");
            table_info_gender.setVisibility(View.GONE);
            table_info_member.setVisibility(View.GONE);

        } else if (result.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(result);

            String url = "http://3.36.255.141/image/"
                    + jsonObject.getString("img");
            Log.d(TAG, "url :" + url);

            task = new ImageLoadTask(Table.this, true, url, table_info_img);
            task.execute();

            table_info_text.setText("다시 등록하시려면 \n프로필 사진을 터치해주세요!");

            table_info_statement.setText(jsonObject.getString("statement"));
            table_info_gender.setText(jsonObject.getString("gender"));
            table_info_member.setText(jsonObject.getString("guestNum"));
        }
    }

    private void handleOtherTableInfo(String result) throws JSONException {

        if (result.equals("없음")) {
            table_info_text.setVisibility(View.INVISIBLE);
            table_info_statement.setText("정보를 입력하지 않은 테이블입니다.");
            table_info_gender.setVisibility(View.INVISIBLE);
            table_info_member.setVisibility(View.INVISIBLE);

            /**
             * table 정보 있을 때
             */
        } else if (result.startsWith("{")) {
            JSONObject jsonObject = new JSONObject(result);

            url = "http://3.36.255.141/image/" + jsonObject.getString("img");


            if(ticketDataHashMap != null){
                Log.d(TAG, "ticket: " + ticketDataHashMap.get("table"+clickTable).getUseTable());
            }
            //전체를 조회해서...?


            /**
             * 티켓 유무
             */
            if (ticketDataHashMap == null ||
                    ticketDataHashMap.get("table"+clickTable) == null) {

                Log.d(TAG, "티켓 없어서 블러 처리");
                task = new ImageLoadTask(Table.this, false, url, table_info_img);
                task.execute();


            } else if (ticketDataHashMap.get("table" + clickTable).getUseTable().equals("table" + clickTable)) {
                //만약 티켓을 가지고 있다면?


                Log.d(TAG, "티켓 사용 유무 :" + ticketDataHashMap.get("table" + clickTable).getUseTable());

                ticketDataHashMap.get("table" + clickTable).setIsUsed(true);
                Log.d(TAG, "table 조회 :" + ticketDataHashMap.get("table" + clickTable).isUsed());
                task = new ImageLoadTask(Table.this, true, url, table_info_img);
                task.execute();
                table_info_text.setVisibility(View.INVISIBLE);
                table_info_img.setClickable(false);

                SendNotification sendNotification = new SendNotification();

                String whoBuy = ticketDataHashMap.get("table" + clickTable).getWhoBuy();
                sendNotification.sendMenu(sendTicketToAdmin(whoBuy));

            }

            table_info_statement.setText(jsonObject.getString("statement"));
            table_info_gender.setText(jsonObject.getString("gender"));
            table_info_member.setText(jsonObject.getString("guestNum"));

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        myData = (MyData) data.getSerializableExtra("myData");
        Log.d(TAG, "onActivityResult myData Id: " + myData.getId());
        Log.d(TAG, "onActivityResult myData IsOrder: " + myData.isOrder());

        chattingDataHashMap = (HashMap<String, ChattingData>) data.getSerializableExtra("chattingData");
        Log.d(TAG, "onActivityResult chattingData: " + chattingDataHashMap);

        ticketDataHashMap = (HashMap<String, TicketData>) data.getSerializableExtra("ticketData");
        Log.d(TAG, "onActivityResult ticketData :" + ticketDataHashMap);

    }

    public String sendTicketToAdmin(String whoBuy) {
        JSONObject jsonObject = new JSONObject();

        try {
            JSONArray menujArray = new JSONArray();//배열이 필요할때
            JSONObject object = new JSONObject();

            object.put("menu", "profileTicket");
            object.put("price", 2000);
            object.put("quantity", 1);
            menujArray.put(object);

            jsonObject.put("item", menujArray);
            jsonObject.put("menuName", "profileTicket");
            jsonObject.put("tableName", whoBuy);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


}



