package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.TableAdapter;
import com.example.openbook.BlurImage;
import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.DialogCustom;
import com.example.openbook.ImageLoadTask;
import com.example.openbook.QRcode.MakeQR;
import com.example.openbook.R;
import com.example.openbook.View.TableList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Table extends AppCompatActivity {

    ArrayList<TableList> tableList;

    String TAG = "TableTAG";

    int clickTable;
    TableAdapter adapter;

    int list[];
    String gender[];
    String guestNum[];
    JSONArray yes_arr;
    JSONArray no_arr;

    OkHttpClient okHttpClient = new OkHttpClient();

    TextView menu;
    LinearLayout table_sidebar;
    TextView chatting;
    TextView info;

    int myTable;
    String get_id;
    boolean chattingAgree = false;
    boolean orderCk = false;
    String ticket = "noTicket";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table);

        get_id = getIntent().getStringExtra("id");
        orderCk = getIntent().getBooleanExtra("orderCk", false);
        Log.d(TAG, "orderCk :" + orderCk);

        ticket = getIntent().getStringExtra("profileTicket");
        Log.d(TAG, "profileTicket" + ticket);

        TextView table_num = findViewById(R.id.table_number);
        table_num.setText(get_id);


        /**
         * Appbar: Menu 누르면 이동
         */
        menu = findViewById(R.id.menu);

        RecyclerView table_grid = findViewById(R.id.tableGrid);
        adapter = new TableAdapter();

        //그리드 레이아웃 설정
        table_grid.setLayoutManager(new GridLayoutManager(this, 5));

        //어댑터 연결
        table_grid.setAdapter(adapter);


        //리스트 추가 후 어댑터 연결
        tableList = new ArrayList<>();

        myTable = Integer.parseInt(get_id.replace("table", ""));

        /**
         * 테이블 그리드 만들기 1
         */
        for(int i=1; i<21; i++){
            if(i == myTable){
                tableList.add(new TableList("my table", getDrawable(R.drawable.my_table_border),0));
            }else{
                tableList.add(new TableList(i, getDrawable(R.drawable.table_border),1));
            }
        }

        adapter.setAdapterItem(tableList);

        checkOrderTable(get_id, myTable);


        //오른쪽 사이드 메뉴
        table_sidebar = findViewById(R.id.table_sidebar);
        table_sidebar.setVisibility(View.INVISIBLE);


        chatting = findViewById(R.id.chatting);
        info = findViewById(R.id.take_info);
    } //onCreate

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");

        chattingAgree = getIntent().getBooleanExtra("chattingAgree", false);

//        myTable = Integer.parseInt(get_id.replace("table", ""));

//        /**
//         * 테이블 그리드 만들기 1
//         */
//        for(int i=1; i<21; i++){
//            if(i == myTable){
//                tableList.add(new TableList("my table", getDrawable(R.drawable.my_table_border),0));
//            }else{
//                tableList.add(new TableList(i, getDrawable(R.drawable.table_border),1));
//            }
//        }
//
//        adapter.setAdapterItem(tableList);
//
//        checkOrderTable(get_id, myTable);

//        orderTableUpdate();
//        => http로 데이터 받아오기 전에 코드를 실행해서 NullPointerException이 나와버림,,,,,,
//        그렇다고 runOnUiThread를 쓰면 밑에 클릭 이벤트 누르면 **반짝**거림 click한 테이블 색깔 적용도 안되고

    } //onStart


    @Override
    protected void onResume() {
        super.onResume();

        boolean orderCk = getIntent().getBooleanExtra("orderCk", false);

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });

        //table 누르면 옆에 사이드 메뉴 뜨게
        adapter.setOnItemClickListener(new TableAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                table_sidebar.setVisibility(View.VISIBLE);
                clickTable = position+1;
                Log.d(TAG, "onItemClick: clickTable " + clickTable);
                Log.d(TAG, "onItemClick: position " + position);

                /**
                 * 테이블 그리드 만들기 2
                 */

                for(int i=0; i<20; i++){
                    if(i == myTable-1){
                        tableList.get(i).setTableColor(getDrawable(R.drawable.my_table_border));
                    }else if(i != position){
                        tableList.get(i).setTableColor(getDrawable(R.drawable.table_border));
                    }
                }

//                checkOrderTable(get_id, myTable);

                if(yes_arr.length() != 0){
                    for(int i = 0; i<list.length; i++){
                        tableList.get(list[i]-1).setTableColor(getDrawable(R.drawable.table_boder_order));
                        tableList.get(list[i]-1).setTableGender(gender[i]);
                        tableList.get(list[i]-1).setTableGuestNum(guestNum[i]);
                    }
                }else{
                    for(int i =0; i<list.length; i++){
                        tableList.get(list[i]-1).setTableColor(getDrawable(R.drawable.table_boder_order));
                    }
                }

//
                tableList.get(position).setTableColor(getDrawable(R.drawable.table_border_click));
            }
        });

        DialogCustom alertDialog = new DialogCustom();


        /**
         * 채팅하기 누르면 팝업 뜨고 채팅할 수 있도록 :
         * 1. 내가 주문을 안했으면 주문하라고 팝업이 뜨고
         * 2. 상대방이 주문을 안했으면 알려주기
         */
        chatting.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                if (orderCk == false) {
                    alertDialog.showAlertDialog(Table.this,
                            "주문 후 채팅이 가능합니다.");

                }else if(clickTable == myTable){
                    alertDialog.showAlertDialog(Table.this,
                            "나의 채팅방 입니다. 다른 테이블과 채팅해보세요!");
                }else if(list.length>0) {

                    //list(들어와 있는 애들이면 그냥 채팅이 가능하게 해주는 것인데 여기서 fcm을 보내는
                    for (int i = 0; i < list.length; i++) {
                        if (list[i] == clickTable) {

                            if(chattingAgree == false){
                                alertDialog.chattingRequest(Table.this,
                                        clickTable + "번 테이블과 채팅을 하시겠습니까?" +
                                                "\n<추신> 채팅 전 테이블 정보를 입력하는 것을 추천드립니다!",
                                        "table" + clickTable, get_id);
                                Log.d(TAG, "채팅 신청");

                            }else if(chattingAgree == true){
                                Intent intent = new Intent(Table.this, ChattingUI.class);
                                intent.putExtra("tableNumber", clickTable);
                                intent.putExtra("id", get_id);
                                intent.putExtra("chattingAgree", chattingAgree);
                                startActivity(intent);
                            }

                            break; // 얘가 안된다 망할..^^

//
                        }else{

                            alertDialog.showAlertDialog(Table.this,
                                    "비어있는 테이블이거나 아직 주문하지 않은 테이블 입니다.");
                        }
                    }//for 끝

                } // if-else  list.length>0 끝

            } // onClick
        }); //setOnClickListener



        /**
         * info 누르면 해당 테이블 정보 볼 수 있게
         */
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dlg = new Dialog(Table.this);
                dlg.setContentView(R.layout.table_infomation);
                dlg.show();


                ImageView table_info_img = dlg.findViewById(R.id.table_info_img);
                TextView statement = dlg.findViewById(R.id.statement);
                TextView table_info_gender = dlg.findViewById(R.id.table_info_gender);
                TextView table_info_member = dlg.findViewById(R.id.table_info_member);
                TextView table_info_close = dlg.findViewById(R.id.table_info_close);


                // GET 요청 객체 생성
                Request.Builder builder = new Request.Builder()
                        .url("http://3.36.255.141/tableInfoCk.php")
                        .get();

                builder.addHeader("table", "table"+clickTable);
                Request request = builder.build();
                Log.d(TAG, "request :" + request);

                if(clickTable == myTable){
                    /**
                     *  등록을 했으면 등록된 정보를 보여주고 등록 안했으면 하단 set
                     */
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d(TAG, "onFailure: " + e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String body = response.body().string();

                                        if(body.equals("없음")){
                                            MakeQR makeQR = new MakeQR();
                                            table_info_img.setImageBitmap(makeQR.myQR(get_id));
                                            statement.setText("사진과 정보를 입력하시려면 다음 큐알로 입장해주세요 :)");
                                            table_info_gender.setVisibility(View.INVISIBLE);
                                            table_info_member.setVisibility(View.INVISIBLE);

                                        }else if(body.startsWith("{")){
                                            JSONObject jsonObject = new JSONObject(body);

                                            String url = "http://3.36.255.141/image/"
                                                    + jsonObject.getString("img");
                                            Log.d(TAG, "url :" + url);

                                            /**
                                             * 사진을 누르면 돈내고 사진 깔거냐고 물어보기
                                             */


                                            ImageLoadTask task = new ImageLoadTask(Table.this, true, url,table_info_img);



                                            task.execute();

                                            statement.setText(jsonObject.getString("statement"));
                                            table_info_gender.setText(jsonObject.getString("gender"));
                                            table_info_member.setText(jsonObject.getString("guestNum"));

                                        }//if-else문

                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                    }
                                } //run
                            }); // runOnUiThread
                        } //onResponse
                    });

                }else{
                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d(TAG, "onFailure: " + e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String body = response.body().string();

                                        if(body.equals("없음")){

                                            statement.setText("정보를 입력하지 않은 테이블입니다.");
                                            table_info_gender.setVisibility(View.INVISIBLE);
                                            table_info_member.setVisibility(View.INVISIBLE);


                                        }else if(body.startsWith("{")){
                                            JSONObject jsonObject = new JSONObject(body);

                                            String url = "http://3.36.255.141/image/"+ jsonObject.getString("img");


                                            ImageLoadTask task;

                                            Log.d(TAG, "ticket :" + ticket);


                                            task = new ImageLoadTask(Table.this, false, url,table_info_img);
                                            Log.d(TAG, "아 제발 진짜 왜그래 :" + ticket);


//                                            ImageLoadTask task = new ImageLoadTask(Table.this, true,url,table_info_img);
                                            task.execute();


                                            statement.setText(jsonObject.getString("statement"));
                                            table_info_gender.setText(jsonObject.getString("gender"));
                                            table_info_member.setText(jsonObject.getString("guestNum"));
//
                                        }

                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            });
                        }
                    });

                }


                /**
                 * 이미지 클릭시 조회권 구매하기
                 */
                table_info_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       //팝업이 나오고
                        alertDialog.buyProfileTicket(Table.this,
                               "프로필 조회권을 구매하시겠습니까?\n** 프로필 조회권 2000원",
                               get_id);
                    }
                });

                table_info_close.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dlg.dismiss();
                    }
                });


            }
        });

    } //onResume



    public void checkOrderTable(String get_id, int myTable){

        // GET 요청 객체 생성
        Request.Builder builder = new Request.Builder()
                .url("http://3.36.255.141/infoCk.php")
                .get();

        builder.addHeader("table", get_id);

        Request request = builder.build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                e.printStackTrace();
                Log.d(TAG, "failure: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                Log.d(TAG, "onResponse: " + body);

                try {
                    JSONArray jArray = new JSONArray(body);

                    yes_arr = jArray.getJSONArray(0);
                    no_arr = jArray.getJSONArray(1);

                    list = new int[yes_arr.length()+no_arr.length()];
                    gender = new String[yes_arr.length()];
                    guestNum = new String[yes_arr.length()];

                    //정보 입력한 애들
                    for (int i = 0; i < yes_arr.length(); i++) {
                        JSONObject jsonObject = yes_arr.getJSONObject(i);

                        list[i] = Integer.parseInt(jsonObject.getString("tableName").replace("table", ""));
                        gender[i] = jsonObject.getString("gender");
                        guestNum[i] = jsonObject.getString("guestNum");
                    }

                    //정보 입력 안한애들
                    for(int i=0; i< no_arr.length(); i++){
                        list[yes_arr.length()+i] = Integer.parseInt(no_arr.getString(i).replace("table", ""));
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "list :" + Arrays.toString(list));


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for(int i = 0; i<yes_arr.length(); i++){
                            tableList.get(list[i]-1).setTableColor(getDrawable(R.drawable.table_boder_order));
                            tableList.get(list[i]-1).setTableGender(gender[i]);
                            tableList.get(list[i]-1).setTableGuestNum(guestNum[i]);
                        }

                        for(int i=0; i<no_arr.length(); i++){
                            tableList.get(list[yes_arr.length()+i]-1).setTableColor(getDrawable(R.drawable.table_boder_order));
                        }

                        tableList.get(myTable-1).setTableColor(getDrawable(R.drawable.my_table_border));
                        adapter.notifyDataSetChanged();
                    }
                });


            }
        });
    }




    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }



}



