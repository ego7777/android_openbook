package com.example.openbook.Activity;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.CartAdapter;
import com.example.openbook.Adapter.MenuAdapter;
import com.example.openbook.Adapter.SideListViewAdapter;
import com.example.openbook.Chatting.Client;
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.Deco.menu_recyclerview_deco;
import com.example.openbook.DialogCustom;
import com.example.openbook.DrawableMethod;
import com.example.openbook.FCMclass.FCM;
import com.example.openbook.R;
import com.example.openbook.TableInformation;
import com.example.openbook.View.CartList;
import com.example.openbook.View.MenuList;
import com.example.openbook.View.SideList;
import com.example.openbook.View.TableList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;


import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Menu extends AppCompatActivity {

    String TAG = "menuTAG";

    int totalPrice;
    int myTable;
    boolean orderCk = false;
    boolean infoCk = false;

    public static ClientSocket clientSocket;
    boolean loop = false;

    ArrayList<MenuList> menuLists;
    ArrayList<CartList> cartLists;

    ArrayList<TableList> tableList;

    HashMap<Integer, TableInformation> tableInformationHashMap;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String get_id;

    TextView table;
    RecyclerView cartView;
    CartAdapter cartAdapter;
    TextView orderPrice;
    TextView order;

    RecyclerView menuGrid;
    MenuAdapter menuAdapter;

    ListView navigation;
    SideListViewAdapter sideAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu2);

        get_id = getIntent().getStringExtra("get_id");
        orderCk = getIntent().getBooleanExtra("orderCk", false);


        tableInformationHashMap = (HashMap<Integer, TableInformation>) getIntent().getSerializableExtra("tableInformation");


        if(tableInformationHashMap == null){
            Log.d(TAG, "onCreate tableInformation null");
        }else{
            Log.d(TAG, "intent tableInformation size:" + tableInformationHashMap.size());
        }


        /**
         * 로그인을 성공하면 id, token을 firebase realtime db에 저장
         */
        Intent fcm = new Intent(getApplicationContext(), FCM.class);
        fcm.putExtra("get_id", get_id);
        startService(fcm);


        /**
         * AppBar: 로그인하면 table number 바로 나오는거
         */
        TextView table_number = findViewById(R.id.table_number);


        if(get_id.length() > 0) {
            table_number.setText(get_id);
        } else {
            table_number.setText(get_id);
        }
        Log.d(TAG, "onCreate: ");


        TextView cart_header = findViewById(R.id.cart_header);


    } // onCreate()


    @Override
    protected void onStart() {
        super.onStart();
        //액티비티가 사용자에게 보여질 때, 사용자와 상호작용 X

        table = findViewById(R.id.table);

        /**
         * 장바구니
         */
        cartView = findViewById(R.id.cart);
        cartView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        cartAdapter = new CartAdapter();

        cartLists = new ArrayList<>();
        cartView.setAdapter(cartAdapter);


        orderPrice = findViewById(R.id.order_price);
        order = findViewById(R.id.order);


        /**
         * 안주
         */
        menuGrid = findViewById(R.id.menu_grid);
        menuGrid.setLayoutManager(new GridLayoutManager(this, 3));
        menuGrid.addItemDecoration(new menu_recyclerview_deco(Menu.this));

        menuAdapter = new MenuAdapter();

        menuLists = new ArrayList<>();
        menuGrid.setAdapter(menuAdapter);

        int img[] = {R.drawable.salad, R.drawable.soup, R.drawable.dish, R.drawable.burger, R.drawable.ramen, R.drawable.pasta,
                R.drawable.beer_mug, R.drawable.beer, R.drawable.soju, R.drawable.cocktail, 0, 0,
                R.drawable.nachos, R.drawable.tteokbokki, R.drawable.fries};

        String menu_name[] = {
                "샐러드",
                "전골",
                "목살스테이크",
                "수제버거",
                "해물라면",
                "파스타",
                "생맥주",
                "병맥주",
                "소주",
                "칵테일",
                null,
                null,
                "나초",
                "국물떡볶이",
                "감자튀김",
        };

        int menu_price[] = {9000, 15000, 18000, 9000, 8000, 15000, 4500, 5000, 5000, 6000, 0, 0, 5000, 6000, 5000};


        for (int i = 0; i < img.length; i++) {
            if (i == 10 || i == 11) {
                menuLists.add(new MenuList(0, null, 0, 0));
            } else {
                menuLists.add(new MenuList(img[i], menu_name[i], menu_price[i], 1));
            }
        }

        menuAdapter.setAdapterItem(menuLists);


        /**
         * 사이드 네비게이션
         */
        navigation = findViewById(R.id.navigation);
        sideAdapter = new SideListViewAdapter();

        String side[] = {"메인안주", "주류", "사이드", "직원호출", "결제(test)"};

        for (int i = 0; i < side.length; i++) {
            sideAdapter.addItem(new SideList(side[i]));
        }
        navigation.setAdapter(sideAdapter);


        /**
         * 액티비티 전환 시 sp에 저장된 장바구니 데이터 가져와서 뿌려주기
         */
        pref = getSharedPreferences("Order", MODE_PRIVATE);

        editor = pref.edit();

        String getName = pref.getString("name", "");
        String getCount = pref.getString("count", "");
        String getPrice = pref.getString("price", "");


        if (!getName.isEmpty()) {

            String splitName[] = getName.split("###");
            String splitCount[] = getCount.split("###");
            String splitPrice[] = getPrice.split("###");


            int changeCount[] = new int[splitCount.length];
            int changePrice[] = new int[splitPrice.length];


            for (int i = 0; i < splitName.length; i++) {

                changeCount[i] = Integer.parseInt(splitCount[i]);
                changePrice[i] = Integer.parseInt(splitPrice[i]);

                cartLists.add(new CartList(splitName[i], changePrice[i], changeCount[i], 1));
                totalPrice = totalPrice + changePrice[i] * changeCount[i];
            }

            cartAdapter.setAdapterItem(cartLists);
            orderPrice.setText(String.valueOf("합계: " + totalPrice + " 원"));
        }

    } //onStart()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        //사용자와 상호작용 하는 단계, 어플 기능은 여기서


        /**
         * Appbar: table 클래스로 이동
         */
        table.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(clientSocket != null && clientSocket.getTableList()!= null){
                    tableList = clientSocket.getTableList();
                }

                Intent intent = new Intent(getApplicationContext(), Table.class);
                intent.putExtra("get_id", get_id);
                intent.putExtra("orderCk", orderCk);
                intent.putExtra("tableList", tableList);
                intent.putExtra("tableInformation", tableInformationHashMap);
                startActivity(intent);
            }
        });


        /**
         * 장바구니 클릭 이벤트
         */
        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onPlusClick(View view, int position) {
                int add = cartLists.get(position).getMenu_count() + 1;
                cartLists.get(position).setMenu_count(add);
                totalPrice = totalPrice + cartLists.get(position).getMenu_price();
                orderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");
                cartAdapter.setAdapterItem(cartLists);
                sharedPreference();

            }

            @Override
            public void onMinusClick(View view, int position) {
                int minus = cartLists.get(position).getMenu_count() - 1;
                totalPrice = totalPrice - cartLists.get(position).getMenu_price();

                if (minus == 0) {
                    cartLists.remove(position);
                } else {
                    cartLists.get(position).setMenu_count(minus);
                }

                orderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");


                cartAdapter.setAdapterItem(cartLists);
                sharedPreference();
            }


            @Override
            public void onDeleteClick(View view, int position) {
                int delete_count = cartLists.get(position).getMenu_count();
                int delete_price = cartLists.get(position).getMenu_price();

                totalPrice = totalPrice - (delete_price * delete_count);
                orderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");

                cartLists.remove(position);

                cartAdapter.setAdapterItem(cartLists);
                sharedPreference();
            }

        });


        /**
         * 클릭하면 장바구니에 담기게
         */
        menuAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {

            int pos = 1000;
            int menuCount;

            @Override
            public void onItemClick(View view, int id, String name, int price, int position) {

                //중복되는 메뉴의 포지션 값 get
                for (int i = 0; i < cartLists.size(); i++) {
                    if (cartLists.get(i).getMenu_name().equals(name)) {
                        pos = i;
                        Log.d(TAG, "pos : " + pos);
                    }
                }

                //포지션 값이 초기값이면 새롭게 추가하고, 아니면 개수만 올려서 다시 적용
                if (pos == 1000) {
                    cartLists.add(new CartList(name, price, 1, 1));
                } else {
                    menuCount = cartLists.get(pos).getMenu_count() + 1;
                    cartLists.get(pos).setMenu_count(menuCount);
                }

                cartAdapter.setAdapterItem(cartLists);

                pos = 1000;


                totalPrice = totalPrice + price;
                orderPrice.setText(String.valueOf("합계: " + totalPrice + " 원"));

                sharedPreference();

            }
        });


        sideAdapter.setOnItemClickListener(new SideListViewAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(View view, String where) {
                if (where.equals("메인안주")) {
                    menuGrid.smoothScrollToPosition(0);

                } else if (where.equals("주류")) {
                    menuGrid.smoothScrollToPosition(9);

                } else if (where.equals("사이드")) {
                    menuGrid.smoothScrollToPosition(12);

                } else if (where.equals("직원호출")) {
                    Intent intent = new Intent(Menu.this, CallServer.class);
                    startActivity(intent);

                } else if (where.equals("결제(test)")) {

                    /**
                     /1. 로컬db 내용을 서버에 전달해서 저장
                     2. 저장 완료하면 로컬db 날리기
                     */

                    Client client = new Client();
                    client.quit();
                    Log.d(TAG, "소켓 완전히 종료");

                }
            }
        });


        /**
         * 1. 주문내역 관리자한테 넘어가기
         * 2. 채팅온거 표시 -> 카트 밑에 빈공간에
         * 3. 읽으면 <읽음> 표시만 하기…………
         * 4. 채팅 신청 (하트대신!!!)-> 궁금하면 사진 까봐 -> 채팅하기
         */

        Handler handler = new Handler();

        DialogCustom dialogCustom = new DialogCustom();

        order.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                if (cartLists.size() == 0) {
                    dialogCustom.HandlerAlertDialog(Menu.this, "메뉴가 존재하지 않습니다.");

                } else {

                    RequestBody formBody = new FormBody.Builder()
                            .add("json", getJson(get_id, cartLists))
                            .build();

                    Request request = new Request.Builder()
                            .url("http://3.36.255.141/saveOrder.php")
                            .post(formBody)
                            .build();

                    final OkHttpClient client = new OkHttpClient();


                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            orderCk = false;
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "네트워크에 문제가 발생하였습니다.\n잠시 후 다시 주문해주세요.", Toast.LENGTH_LONG).show();
                            Log.d(TAG, "order failure: " + e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String body = response.body().string();

                                        if (body.equals("주문완료")) {
                                            orderCk = true;
                                            cartLists = new ArrayList<>();
                                            cartAdapter.setAdapterItem(cartLists);
                                            totalPrice = 0;
                                            orderPrice.setText("");

                                            if(clientSocket == null){
                                                clientSocket = new ClientSocket("3.36.255.141", 7777, get_id, getApplicationContext(), tableList);
                                                clientSocket.start();
                                                Log.d(TAG, "소켓 시작");
//                                                loop = true;

//                                                handler.postDelayed(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        if(clientSocket.socket.isConnected()){
//                                                            updateTable updateTable = new updateTable();
//                                                            updateTable.start();
//                                                            Log.d(TAG, "updateTable start");
//                                                        }
//                                                    }
//                                                }, 1000);


                                            }


                                        } else {
                                            orderCk = false;
                                            Toast.makeText(getApplicationContext(), "서버에 문제가 발생하였습니다." +
                                                    "\n잠시 후 다시 주문해주세요.", Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "response : " + body);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });


                        }
                    }); //response


                    editor.remove("name");
                    editor.remove("count");
                    editor.remove("price");
                    editor.commit();


                    Dialog dlg = new Dialog(Menu.this);
                    dlg.setContentView(R.layout.order_complete);
                    dlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dlg.show();

                    ImageView img = dlg.findViewById(R.id.serve_img);
                    TextView text = dlg.findViewById(R.id.serve_text);


                    Animation animation = AnimationUtils.loadAnimation(Menu.this, R.anim.order_complete);
                    img.startAnimation(animation);
                    text.startAnimation(animation);

                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dlg.dismiss();
                        }
                    }, 1000);


                    if (infoCk == false) {
                        infoCk = true;
                        orderCk = true;
                        dialogCustom.moveActivity(Menu.this,
                                "테이블 정보를 입력하기 위한 페이지로 이동하겠습니까?",
                                get_id, orderCk, tableList);

                    }

                }
            }
        });

        myTable = Integer.parseInt(get_id.replace("table", ""));


        if(tableList == null){
            tableList = new ArrayList();
            Log.d(TAG, "onResume tableList initial one");
        }else{
            Log.d(TAG, "intent tableList size :" + tableList.size());
        }

        DrawableMethod drawableToBitmap = new DrawableMethod();

        byte[] myTableImage = drawableToBitmap.makeBitmap(getDrawable(R.drawable.my_table_border));
        Log.d(TAG, "myTableImage :" + myTableImage);
        byte[] otherTableImage = drawableToBitmap.makeBitmap(getDrawable(R.drawable.table_border));
        Log.d(TAG, "otherTableImage : " + otherTableImage);



        for(int i=1; i<21; i++){
            if(i == myTable){
                tableList.add(new TableList("my Table",myTableImage, 0));
            }else{
                tableList.add(new TableList(i, otherTableImage, 1));
            }
        }

        Log.d(TAG, "tableList :" + tableList.size());


    }


    public void sharedPreference() {

        String name_temp = null;
        String count_temp = null;
        String price_temp = null;


        for (int i = 0; i < cartLists.size(); i++) {

            if (name_temp == null) {
                name_temp = cartLists.get(i).getMenu_name() + "###";
                count_temp = cartLists.get(i).getMenu_count() + "###";
                price_temp = cartLists.get(i).getMenu_price() + "###";
            } else {
                name_temp = name_temp + cartLists.get(i).getMenu_name() + "###";
                count_temp = count_temp + cartLists.get(i).getMenu_count() + "###";
                price_temp = price_temp + cartLists.get(i).getMenu_price() + "###";
            }

        }

        editor.putString("name", name_temp);
        editor.putString("count", count_temp);
        editor.putString("price", price_temp);
        editor.commit();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getJson(String get_id, ArrayList<CartList> list) {
        JSONObject obj = new JSONObject();
        try {
            JSONArray jArray = new JSONArray();//배열이 필요할때
            for (int i = 0; i < list.size(); i++)//배열
            {
                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
                sObject.put("menu", list.get(i).getMenu_name());
                sObject.put("price", list.get(i).getMenu_price());
                sObject.put("number", list.get(i).getMenu_count());
                jArray.put(sObject);
            }
            obj.put("table", get_id);
            obj.put("orderTime", getTime());
            obj.put("item", jArray);//배열을 넣음

            Log.d(TAG, "getJson: " + obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    public String getTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String getTime = dateFormat.format(date);

        return getTime;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

//        editor.remove("name");
//        editor.remove("count");
//        editor.remove("price");
//        editor.commit();

        Log.d(TAG, "onDestroy: ");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    public class updateTable extends Thread {

        BufferedReader networkReader;
        DrawableMethod drawableToBitmap = new DrawableMethod();

        byte[] orderTableImage = drawableToBitmap.makeBitmap(getDrawable(R.drawable.table_boder_order));


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            super.run();

            try {
                networkReader = new BufferedReader(
                        new InputStreamReader(clientSocket.socket.getInputStream()));
                Log.d(TAG, "networkReader :" + networkReader.ready());
                Log.d(TAG, "UI socket 연결 :" + clientSocket.socket.isConnected());


            } catch (IOException e) {
                e.printStackTrace();
            }


            while (loop) {
                Log.d(TAG, "while loop start");
                try {
                    String line = networkReader.readLine();
                    Log.d(TAG, "run: " + line);


                    if(line.equals(Integer.toString(myTable))){
                        //넘기고
                    }else{
                        tableList.get(Integer.parseInt(line)-1).setBytes(orderTableImage);
                        tableList.get(Integer.parseInt(line)-1).setViewType(2);
                    }


                    //서버로부터 FIN 패킷(서버로 연결된 세션의 종료를 알리는 패킷)을 수신하면 read() 메소드는 null을 반환
                    if (line == null) {
                        Log.d(TAG, "break");
                        break;
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
