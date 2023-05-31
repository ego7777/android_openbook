package com.example.openbook.Activity;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Deco.menu_recyclerview_deco;
import com.example.openbook.DialogCustom;
import com.example.openbook.FCM.FCM;
import com.example.openbook.KakaoPay;
import com.example.openbook.R;
import com.example.openbook.Data.TableInformation;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.MenuList;
import com.example.openbook.Data.SideList;
import com.example.openbook.Data.TableList;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;

import java.util.ArrayList;
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

    ArrayList<MenuList> menuLists;
    ArrayList<CartList> cartLists;
    ArrayList<TableList> tableList;

    HashMap<Integer, TableInformation> tableInformationHashMap;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String get_id;
    String paymentStyle;

    TextView table;

    CartAdapter cartAdapter;
    TextView orderPrice;
    TextView order;

    RecyclerView menuGrid;
    MenuAdapter menuAdapter;

    ListView navigation;
    SideListViewAdapter sideAdapter;

    ClientSocket clientSocket;

    OkHttpClient okHttpClient;

    DBHelper dbHelper;

    JSONArray menujArray;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        get_id = getIntent().getStringExtra("get_id");
        paymentStyle = getIntent().getStringExtra("paymentStyle");
        orderCk = getIntent().getBooleanExtra("orderCk", false);

        clientSocket = (ClientSocket) getIntent().getSerializableExtra("clientSocket");

        tableInformationHashMap = (HashMap<Integer, TableInformation>) getIntent().getSerializableExtra("tableInformation");


        if (tableInformationHashMap == null) {
            Log.d(TAG, "onCreate tableInformation null");
        } else {
            Log.d(TAG, "menu.class intent tableInformation size:" + tableInformationHashMap.size());
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
        TextView table_number = findViewById(R.id.appbar_menu_table_number);
        table_number.setText(get_id);


        TextView cart_header = findViewById(R.id.cart_header);
        table = findViewById(R.id.appbar_menu_table);

        /**
         * 장바구니
         */
        RecyclerView cartView = findViewById(R.id.cart);
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


        SQLiteDatabase sqLiteDatabase;

        int version = 1;

        dbHelper = new DBHelper(Menu.this, version);
        sqLiteDatabase = dbHelper.getWritableDatabase();

        Cursor res = dbHelper.getTableData("menuListTable");

        okHttpClient = new OkHttpClient();

        if (res.getCount() == 0) {
            Log.d(TAG, "메뉴 db 새로 받아오기");

            Request request = new Request.Builder()
                    .url("http://3.36.255.141/MenuDbLookUp.php")
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d(TAG, "onFailure: " + e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseData = response.body().string();
                    Log.d(TAG, "onResponse: " + responseData);

                    if (responseData.contains("[")) {
                        setMenuList(responseData);
                    } else {
                        Log.d(TAG, "onResponse : " + responseData);
                    }
                }
            });


        } else {
            Log.d(TAG, "메뉴db 있는거 사용");
            while (res.moveToNext()) {
                menuLists.add(new MenuList(res.getString(3),//img
                        res.getString(1), //name
                        res.getInt(2), //price
                        res.getInt(4), //menuType
                        1));
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


    } // onCreate()


    @Override
    protected void onStart() {
        super.onStart();
        //액티비티가 사용자에게 보여질 때, 사용자와 상호작용 X

        String returnOrderList = getIntent().getStringExtra("orderList");
        Log.d(TAG, "onStart_returnOrderList : " + returnOrderList);
        Log.d(TAG, "onStart_PaymentStyle :" + paymentStyle);

        if (returnOrderList != null) {
            saveOrder(returnOrderList);
        }


        /**
         * 액티비티 전환 시 sp에 저장된 장바구니 데이터 가져와서 뿌려주기
         */
        pref = getSharedPreferences("Order", MODE_PRIVATE);

        editor = pref.edit();

        String getName = pref.getString("name", "");
        String getQuantity = pref.getString("quantity", "");
        String getPrice = pref.getString("price", "");


        if (!getName.isEmpty()) {

            String splitName[] = getName.split("###");
            String splitQuantity[] = getQuantity.split("###");
            String splitPrice[] = getPrice.split("###");


            int changeQuantity[] = new int[splitQuantity.length];
            int changePrice[] = new int[splitPrice.length];


            for (int i = 0; i < splitName.length; i++) {

                changeQuantity[i] = Integer.parseInt(splitQuantity[i]);
                changePrice[i] = Integer.parseInt(splitPrice[i]);

                cartLists.add(new CartList(splitName[i], changePrice[i], changeQuantity[i], 1));
                totalPrice = totalPrice + changePrice[i] * changeQuantity[i];
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

                if (clientSocket != null && clientSocket.getTableList() != null) {
                    tableList = clientSocket.getTableList();

                }

                Intent intent = new Intent(getApplicationContext(), Table.class);
                intent.putExtra("get_id", get_id);
                intent.putExtra("orderCk", orderCk);

                intent.putExtra("clientSocket", clientSocket);

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
                int add = cartLists.get(position).getMenu_quantity() + 1;
                cartLists.get(position).setMenu_quantity(add);
                totalPrice = totalPrice + cartLists.get(position).getMenu_price();
                orderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");
                cartAdapter.setAdapterItem(cartLists);
                sharedPreference();


            }

            @Override
            public void onMinusClick(View view, int position) {
                int minus = cartLists.get(position).getMenu_quantity() - 1;
                totalPrice = totalPrice - cartLists.get(position).getMenu_price();

                if (minus == 0) {
                    cartLists.remove(position);
                } else {
                    cartLists.get(position).setMenu_quantity(minus);
                }

                orderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");
                cartAdapter.setAdapterItem(cartLists);
                sharedPreference();
            }


            @Override
            public void onDeleteClick(View view, int position) {
                int delete_quantity = cartLists.get(position).getMenu_quantity();
                int delete_price = cartLists.get(position).getMenu_price();

                totalPrice = totalPrice - (delete_price * delete_quantity);
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
            int menuQuantity;

            @Override
            public void onItemClick(View view,  String name, int price, int position) {

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
                    menuQuantity = cartLists.get(pos).getMenu_quantity() + 1;
                    cartLists.get(pos).setMenu_quantity(menuQuantity);
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
                    clientSocket.quit();
                    Log.d(TAG, "결제해서 소켓 종료");

                }
            }
        });


        /**
         * 1. 주문내역 관리자한테 넘어가기
         * 2. 채팅온거 표시 -> 카트 밑에 빈공간에
         * 3. 읽으면 <읽음> 표시만 하기…………
         * 4. 채팅 신청 (하트대신!!!)-> 궁금하면 사진 까봐 -> 채팅하기
         */



        DialogCustom dialogCustom = new DialogCustom();


        /**
         * paymentStyle = "before" -> 바로 결제 붙여서 서버로 넘아가고 db에 저장
         * paymentStyle = "after" -> 이면 그냥 주문 fcm으로 날리고
         * -> admin에서 팝업 확인 누르면 -> admin s.p(or any db)에 메뉴 정보 담아서
         * -> admin에서 결제하면 -> db로 넘어가도록...!!!!
         */

        order.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                if (cartLists.size() == 0) {
                    dialogCustom.HandlerAlertDialog(Menu.this, "메뉴가 존재하지 않습니다.");

                } else {

                    if (paymentStyle.equals("after")) {
                        // fcm으로 날리고

                    } else if (paymentStyle.equals("before")) {

                        // 여기에 카카오 페이를 붙이겠읍니다.....
                        Intent intent = new Intent(Menu.this, KakaoPay.class);
                        intent.putExtra("menuName", getOrderMenuName(cartLists));
                        intent.putExtra("menuPrice", totalPrice);
                        intent.putExtra("jsonOrderList", getJson(get_id, cartLists));
                        intent.putExtra("paymentStyle", paymentStyle);
                        intent.putExtra("get_id", get_id);
                        startActivity(intent);


                    } else {
                        Log.d(TAG, "order click : paymentStyle이 없어..");
                    }


                    if (infoCk == false) {
                        infoCk = true;
                        orderCk = true;

//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                dialogCustom.moveActivity(Menu.this,
//                                        "테이블 정보를 입력하기 위한 페이지로 이동하겠습니까?",
//                                        get_id, orderCk, clientSocket);
//                                Log.d(TAG, "clientSocket :" + clientSocket.isAlive());
//                            }
//                        }, 1000);
                    }

                }
            }
        });

        myTable = Integer.parseInt(get_id.replace("table", ""));


        if (tableList == null) {
            tableList = new ArrayList();


            Log.d(TAG, "onResume tableList initial one");
        } else {
            Log.d(TAG, "menu.class intent tableList size :" + tableList.size());
        }
//

        SharedPreferences preference = getSharedPreferences("TableNumber", MODE_PRIVATE);
        int table = preference.getInt("tableNumber", 20);
        Log.d(TAG, "SharedPreference table :" + table);

//        DrawableMethod drawableToBitmap = new DrawableMethod();

//        byte[] myTableImage = drawableToBitmap.makeBitmap(getDrawable(R.drawable.my_table_border));
//        Log.d(TAG, "myTableImage :" + myTableImage);
//        byte[] otherTableImage = drawableToBitmap.makeBitmap(getDrawable(R.drawable.table_border));
//        Log.d(TAG, "otherTableImage : " + otherTableImage);


        for (int i = 1; i < table+1; i++) {
            if (i == myTable) {
                tableList.add(new TableList("my Table", getDrawable(R.drawable.my_table_border), 0));
            } else {
                tableList.add(new TableList(i, getDrawable(R.drawable.table_border), 1));
            }
        }

        Log.d(TAG, "tableList :" + tableList.size());


    }

    public String getOrderMenuName(ArrayList<CartList> cartLists) {

        int menuQuantity = cartLists.size();

        String menuName;

        if (menuQuantity == 1) {
            menuName = cartLists.get(0).getMenu_name();
        } else {
            menuName = cartLists.get(0).getMenu_name() + " 외" + Integer.toString(menuQuantity - 1);
        }
        Log.d(TAG, "menuName :" + menuName);

        return menuName;
    }


    public void sharedPreference() {

        String name_temp = null;
        String quantity_temp = null;
        String price_temp = null;


        for (int i = 0; i < cartLists.size(); i++) {

            if (name_temp == null) {
                name_temp = cartLists.get(i).getMenu_name() + "###";
                quantity_temp = cartLists.get(i).getMenu_quantity() + "###";
                price_temp = cartLists.get(i).getMenu_price() + "###";
            } else {
                name_temp = name_temp + cartLists.get(i).getMenu_name() + "###";
                quantity_temp = quantity_temp + cartLists.get(i).getMenu_quantity() + "###";
                price_temp = price_temp + cartLists.get(i).getMenu_price() + "###";
            }

        }

        editor.putString("name", name_temp);
        editor.putString("quantity", quantity_temp);
        editor.putString("price", price_temp);
        editor.commit();


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getJson(String get_id, ArrayList<CartList> list) {
        JSONObject obj = new JSONObject();
        try {
            menujArray = new JSONArray();//배열이 필요할때
            for (int i = 0; i < list.size(); i++)//배열
            {
                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
                sObject.put("menu", list.get(i).getMenu_name());
                sObject.put("price", list.get(i).getMenu_price());
                sObject.put("quantity", list.get(i).getMenu_quantity());
                menujArray.put(sObject);
            }
            obj.put("table", get_id);
            obj.put("orderTime", "시간");
            obj.put("item", menujArray);//배열을 넣음

            Log.d(TAG, "getJson: " + obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }


    public void setMenuList(String jsonData) {

        try {
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String url = "http://3.36.255.141/menuImage/"+jsonObject.getString("img");
                String menu = jsonObject.getString("menu");
                int price = jsonObject.getInt("price");
                int menuType = jsonObject.getInt("type");

                menuLists.add(new MenuList(url,menu, price, menuType,1));



                dbHelper.insertMenuData(jsonObject.getString("menu"),
                        jsonObject.getInt("price"),
                        url,
                        jsonObject.getInt("type"));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                menuAdapter.notifyDataSetChanged();
            }
        });

    }

    public void saveOrder(String returnOrderList) {

        RequestBody formBody = new FormBody.Builder()
                .add("json", returnOrderList)
                .build();

        Request request = new Request.Builder()
                .url("http://3.36.255.141/saveOrder.php")
                .post(formBody)
                .build();


        okHttpClient.newCall(request).enqueue(new Callback() {
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
                    @RequiresApi(api = Build.VERSION_CODES.O)
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

//                                            SendNotification sendNotification = new SendNotification();
//
//                                            sendNotification.sendMenu(get_id, menujArray.toString());

                                if (clientSocket == null) {
                                    clientSocket = new ClientSocket("3.36.255.141", 7777, get_id, tableList);
                                    clientSocket.start();
                                    Log.d(TAG, "소켓 시작");
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

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dlg.dismiss();
            }
        }, 1000);

    }

    public void menuListSorting(){
        int mainMenu = 0;
        int drink =0;
        int sideMenu = 0;


        for(int i=0; i<menuLists.size(); i++){
            if(menuLists.get(i).getMenuType() ==1){
                mainMenu = mainMenu+1;
            }else if(menuLists.get(i).getMenuType() == 2){
                drink = drink +1;
            }else if(menuLists.get(i).getMenu_price() ==3){
                sideMenu = sideMenu +1;
            }
        }

//        if(mainMenu/3 == 1){
//            menuLists.add(mainMenu, new MenuList(null, null, 0,3, 0));
//            menuLists.add(mainMenu+1, new MenuList(null, null, 0, 3, 0));
//        }else if(mainMenu/3 == 2){
//            menuLists.add(mainMenu, new MenuList(null, null, 0,0, 0));
//        }
//
//
//
//        if(drink/3 == 1){
//            menuLists.add(drink, new MenuList(null, null, 0,0, 0));
//            menuLists.add(mainMenu+1, new MenuList(null, null, 0, 0, 0));
//        }else if(mainMenu/3 == 2){
//            menuLists.add(mainMenu, new MenuList(null, null, 0,0, 0));
//        }
//
//        if(mainMenu/3 == 1){
//            menuLists.add(mainMenu, new MenuList(null, null, 0,0, 0));
//            menuLists.add(mainMenu+1, new MenuList(null, null, 0, 0, 0));
//        }else if(mainMenu/3 == 2){
//            menuLists.add(mainMenu, new MenuList(null, null, 0,0, 0));
//        }
    }
}
