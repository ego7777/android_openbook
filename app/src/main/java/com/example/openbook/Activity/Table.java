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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.TableAdapter;
import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.DialogCustom;
import com.example.openbook.DrawableMethod;
import com.example.openbook.ImageLoadTask;
import com.example.openbook.QRcode.MakeQR;
import com.example.openbook.R;
import com.example.openbook.Data.TableInformation;
import com.example.openbook.Data.TableList;
import com.example.openbook.TableQuantity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
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

    ArrayList<TableList> tableList;
    HashMap<Integer, TableInformation> tableInformationHashMap;


    int clickTable, myTable;
    TableAdapter adapter;

    OkHttpClient okHttpClient = new OkHttpClient();

    TextView appbarMenu, requestChatting, checkInformation;
    LinearLayout table_sidebar;
    String get_id, paymentStyle;

    boolean orderCk = false;
    boolean loop = false;

    ImageLoadTask task;
    String url;

    //액티비티가 onCreate 되면 자동으로 받는거고
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: 1");
            if (intent.getAction().equals("tableInformationArrived")) {
                Log.d(TAG, "onReceive: 2");
                String message = intent.getStringExtra("tableInformation");
                Log.d(TAG, "onReceive: " + message);
                tableUpdate(message);
            }
        }
    };


    DrawableMethod drawableMethod;
    int tablePosition;
    String responseBody;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_activity);

        get_id = getIntent().getStringExtra("get_id");
        orderCk = getIntent().getBooleanExtra("orderCk", false);

        paymentStyle = getIntent().getStringExtra("paymentStyle");

        myTable = Integer.parseInt(get_id.replace("table", ""));

        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");




        // 로컬 브로드캐스트 리시버 등록
        IntentFilter intentFilter = new IntentFilter("tableInformationArrived");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);





        tableInformationHashMap = (HashMap<Integer, TableInformation>) getIntent().getSerializableExtra("tableInformation");
        Log.d(TAG, "tableInformation :" + tableInformationHashMap);

        if (tableInformationHashMap == null) {
            tableInformationHashMap = new HashMap<>();
            Log.d(TAG, "onCreate tableInformation initial one");
        } else {
            Log.d(TAG, "intent tableInformation size :" + tableInformationHashMap.size());

        }

        TextView table_num = findViewById(R.id.appbar_menu_table_number);
        table_num.setText(get_id);


        /**
         * Appbar: Menu 누르면 이동
         */
        appbarMenu = findViewById(R.id.appbar_menu_menu);

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

        IntentFilter intentFilter = new IntentFilter("tableInformationArrived");
        LocalBroadcastManager.getInstance(Table.this).registerReceiver(broadcastReceiver, intentFilter);


        SharedPreferences sharedPreferences = getSharedPreferences("ActiveTable", MODE_PRIVATE);

        //onCreate 되기 이전에 데이터들은 저장이 되었다가 가져오는 것
        String activeTable = sharedPreferences.getString("ActiveTable", null);
        if (tableList != null) {

            tableUpdate(activeTable);

        }else{
            TableQuantity tableQuantity = new TableQuantity();
            int tableFromDB = tableQuantity.getTableQuantity();

            tableList = new ArrayList<>();

            for (int i = 1; i < tableFromDB + 1; i++) {
                if (i == myTable) {
                    tableList.add(new TableList(get_id, (Drawable) null, 0));
                } else {
                    tableList.add(new TableList(i, (Drawable) null, 1));
                }
            }

            tableUpdate(activeTable);
        }


        /**
         * 여기는 bitmapArray 로 바꿔주는 로직이여
         */
//        drawableMethod = new DrawableMethod();
//
//        byte[] orderTableImage = drawableMethod.makeBitmap(getDrawable(R.drawable.table_border_order));
//        Log.d(TAG, "orderTableImage :" + orderTableImage);

        appbarMenu.setOnClickListener(this::moveToMenu);


        /**
         * table 누르면 옆에 사이드 메뉴 popup
         */
        adapter.setOnItemClickListener(new TableAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                table_sidebar.setVisibility(View.VISIBLE);
                clickTable = position + 1;
                Log.d(TAG, "onItemClick: clickTable " + clickTable);
                Log.d(TAG, "onItemClick: position " + position);

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

                if (!orderCk) {
                    alertDialog.showAlertDialog(Table.this, "주문 후 채팅이 가능합니다.");

                } else if (clickTable == myTable) {

                    alertDialog.showAlertDialog(Table.this,
                            "나의 채팅방 입니다. 다른 테이블과 채팅해보세요!");

                } else if (tableList.get(clickTable - 1).getViewType() == 2) {

                    if (tableInformationHashMap.get(clickTable) == null ||
                            !tableInformationHashMap.get(clickTable).isChattingAgree()) {
                        alertDialog.chattingRequest(Table.this,
                                clickTable + "번 테이블과 채팅을 하시겠습니까?" +
                                        "\n<추신> 채팅 전 테이블 정보를 입력하는 것을 추천드립니다!",
                                "table" + clickTable, get_id);
                        Log.d(TAG, "채팅 신청");

                    } else if (tableInformationHashMap.get(clickTable).isChattingAgree()) {

                        Intent intent = new Intent(Table.this, ChattingUI.class);
                        intent.putExtra("tableNumber", clickTable);
                        intent.putExtra("get_id", get_id);
                        intent.putExtra("orderCk", orderCk);
                        intent.putExtra("paymentStyle", paymentStyle);
                        intent.putExtra("tableInformation", tableInformationHashMap);

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

                Dialog dlg = new Dialog(Table.this);
                dlg.setContentView(R.layout.table_infomation);
                dlg.show();


                ImageView table_info_img = dlg.findViewById(R.id.table_info_img);
                TextView table_info_text = dlg.findViewById(R.id.table_info_text);
                TextView statement = dlg.findViewById(R.id.statement);
                TextView table_info_gender = dlg.findViewById(R.id.table_info_gender);
                TextView table_info_member = dlg.findViewById(R.id.table_info_member);
                TextView table_info_close = dlg.findViewById(R.id.table_info_close);


                try {
                    String result = requestTableInfo();
                    /**
                     *  등록을 했으면 등록된 정보를 보여주고 등록 안했으면 하단 set
                     */
                    if (clickTable == myTable) {

                        if (result.equals("없음")) {
                            MakeQR makeQR = new MakeQR();
                            table_info_img.setImageBitmap(makeQR.clientQR(get_id));
                            table_info_img.setClickable(false);

                            table_info_text.setVisibility(View.INVISIBLE);

                            statement.setText("사진과 정보를 입력하시려면 다음 큐알로 입장해주세요 :)");
                            table_info_gender.setVisibility(View.INVISIBLE);
                            table_info_member.setVisibility(View.INVISIBLE);

                        } else if (result.startsWith("{")) {
                            JSONObject jsonObject = new JSONObject(result);

                            String url = "http://3.36.255.141/image/"
                                    + jsonObject.getString("img");
                            Log.d(TAG, "url :" + url);

                            task = new ImageLoadTask(Table.this, true, url, table_info_img);
                            task.execute();

                            table_info_text.setText("다시 등록하시려면 \n프로필 사진을 터치해주세요!");

                            statement.setText(jsonObject.getString("statement"));
                            table_info_gender.setText(jsonObject.getString("gender"));
                            table_info_member.setText(jsonObject.getString("guestNum"));


                        }//if-else 문
                    } else {
                        if (result.equals("없음")) {
                            table_info_text.setVisibility(View.INVISIBLE);
                            statement.setText("정보를 입력하지 않은 테이블입니다.");
                            table_info_gender.setVisibility(View.INVISIBLE);
                            table_info_member.setVisibility(View.INVISIBLE);

                            /**
                             * table 정보 있을 때
                             */
                        } else if (result.startsWith("{")) {
                            JSONObject jsonObject = new JSONObject(result);

                            url = "http://3.36.255.141/image/" + jsonObject.getString("img");

                            Log.d(TAG, "url :" + url);


                            if (tableInformationHashMap == null ||
                                    tableInformationHashMap.get(clickTable) == null ||
                                    tableInformationHashMap.get(clickTable).getUseTable() == 0) {

                                Log.d(TAG, "팝업 안에서 발생 null");
                                task = new ImageLoadTask(Table.this, false, url, table_info_img);
                                task.execute();


                            } else {

                                if (tableInformationHashMap.get(clickTable) == null) {
                                    Log.d(TAG, "팝업 안에서 발생 getUseTable null");
                                    task = new ImageLoadTask(Table.this, false, url, table_info_img);
                                    task.execute();

                                } else if (tableInformationHashMap.get(clickTable).getUseTable() == clickTable) {
                                    Log.d(TAG, "팝업 안에서 발생 getUseTable :" + tableInformationHashMap.get(clickTable).getUseTable());
                                    tableInformationHashMap.get(clickTable).setUsage(true);
                                    Log.d(TAG, "table 조회 :" + tableInformationHashMap.get(clickTable).isChattingAgree());
                                    task = new ImageLoadTask(Table.this, true, url, table_info_img);
                                    task.execute();
                                    table_info_text.setVisibility(View.INVISIBLE);
                                    table_info_img.setClickable(false);
                                }


                            }

                            statement.setText(jsonObject.getString("statement"));
                            table_info_gender.setText(jsonObject.getString("gender"));
                            table_info_member.setText(jsonObject.getString("guestNum"));

                        }
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
                            table_info_img.setImageBitmap(makeQR.clientQR(get_id));
                            table_info_text.setVisibility(View.INVISIBLE);

                        } else {

                            Intent intent = new Intent(Table.this, PopUp.class);
                            intent.putExtra("title", "프로필 조회권 구매");
                            intent.putExtra("body", "프로필 조회권을 구매하시겠습니까?\n** 프로필 조회권 2000원");

                            intent.putExtra("get_id", get_id);
                            intent.putExtra("clickTable", clickTable);
                            intent.putExtra("orderCk", orderCk);
                            intent.putExtra("tableInformation", tableInformationHashMap);

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
                    Log.d(TAG, "onResponse: " + body);
                    responseBody = body;
                } else {
                    Log.d(TAG, "onResponse: fail");
                }
            }
        });

        if (responseBody == null) {
            Thread.sleep(250);
        }

        return responseBody;
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(Table.this).unregisterReceiver(broadcastReceiver);
    }



    public void moveToMenu(View view) {
        Intent intent = new Intent(Table.this, Menu.class);
        intent.putExtra("get_id", get_id);
        intent.putExtra("orderCk", orderCk);
        intent.putExtra("tableList", tableList);
        intent.putExtra("tableInformation", tableInformationHashMap);
        intent.putExtra("paymentStyle", paymentStyle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void tableUpdate(String line) {
        Log.d(TAG, "tableUpdate: ");
        int table[];

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



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        get_id = data.getStringExtra("get_id");
        Log.d(TAG, "onActivityResult get_id :" + get_id);

        orderCk = data.getBooleanExtra("orderCk", orderCk);
        Log.d(TAG, "onActivityResult orderCk :" + orderCk);

        tableInformationHashMap = data.getParcelableExtra("tableInformation");
        Log.d(TAG, "onActivityResult tableInformationHashMap :" + tableInformationHashMap);

    }
}



