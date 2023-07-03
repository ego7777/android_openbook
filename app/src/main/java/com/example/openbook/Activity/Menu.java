package com.example.openbook.Activity;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.KakaoPay;
import com.example.openbook.SaveOrderDeleteData;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class Menu extends AppCompatActivity {

    String TAG = "menuTAG";

    int totalPrice, myTable;
    boolean orderCk = false;
    boolean infoCk = false;

    ArrayList<MenuList> menuLists;
    ArrayList<CartList> cartLists;
    ArrayList<TableList> tableList;
    ListView menuNavigation;

    HashMap<Integer, TableInformation> tableInformationHashMap;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    String get_id, paymentStyle;
    int tableFromDB;

    TextView appbarMenuTable, appbarOrderList, cartOrderPrice;

    CartAdapter cartAdapter;
    MenuAdapter menuAdapter;
    SideListViewAdapter sideAdapter;

    Button cartOrderButton, menuClose;

    RecyclerView menuRecyclerview;

    ClientSocket clientSocket;

    OkHttpClient okHttpClient;

    DBHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;

    JSONArray menujArray;

    SendNotification sendNotification;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_activity);

        get_id = getIntent().getStringExtra("get_id");
        paymentStyle = getIntent().getStringExtra("paymentStyle");
        tableFromDB = getIntent().getIntExtra("tableFromDB", 20);
        Log.d(TAG, "tableFromDB: " + tableFromDB);
        orderCk = getIntent().getBooleanExtra("orderCk", false);

        clientSocket = (ClientSocket) getIntent().getSerializableExtra("clientSocket");


        tableInformationHashMap = (HashMap<Integer, TableInformation>) getIntent().getSerializableExtra("tableInformation");
        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

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

        sendNotification = new SendNotification();

        if (paymentStyle.equals("before")) {
            sendNotification.usingTable(get_id, "사용");
        }


        /**
         * AppBar: 로그인하면 table number 바로 나오는거
         */
        TextView table_number = findViewById(R.id.appbar_menu_table_number);
        table_number.setText(get_id);

        menuClose = findViewById(R.id.menu_close);

        if (paymentStyle.equals("after")) {
            menuClose.setVisibility(View.GONE);
        }
        appbarMenuTable = findViewById(R.id.appbar_menu_table);
        appbarOrderList = findViewById(R.id.appbar_menu_orderList);


        /**
         * 장바구니
         */
        RecyclerView cartRecyclerview = findViewById(R.id.menu_cart_recyclerview);
        cartRecyclerview.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        cartAdapter = new CartAdapter();

        cartLists = new ArrayList<>();
        cartRecyclerview.setAdapter(cartAdapter);

        /**
         * 액티비티 전환 시 sp에 저장된 장바구니 데이터 가져와서 뿌려주기
         */
        pref = getSharedPreferences("cart_list", MODE_PRIVATE);
        editor = pref.edit();

        cartOrderPrice = findViewById(R.id.cart_order_price);
        cartOrderButton = findViewById(R.id.cart_order_button);

        /**
         * 안주
         */
        menuRecyclerview = findViewById(R.id.menu_recyclerview);
        menuRecyclerview.setLayoutManager(new GridLayoutManager(this, 3));
        menuRecyclerview.addItemDecoration(new menu_recyclerview_deco(Menu.this));

        menuAdapter = new MenuAdapter();

        menuLists = new ArrayList<>();
        menuRecyclerview.setAdapter(menuAdapter);


        int version = 1;
        version ++;

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
        menuNavigation = findViewById(R.id.menu_navigation);
        sideAdapter = new SideListViewAdapter();

        String side[] = {"메인안주", "주류", "사이드", "직원호출"};

        for (int i = 0; i < side.length; i++) {
            sideAdapter.addItem(new SideList(side[i]));
        }
        menuNavigation.setAdapter(sideAdapter);


    } // onCreate()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();
        //액티비티가 사용자에게 보여질 때, 사용자와 상호작용 X

        String returnOrderList = getIntent().getStringExtra("orderList");
        Log.d(TAG, "onStart_returnOrderList : " + returnOrderList);
        Log.d(TAG, "onStart_PaymentStyle :" + paymentStyle);

        if (returnOrderList != null) {
            SaveOrderDeleteData orderSave = new SaveOrderDeleteData();
            try{
                boolean success = orderSave.orderSave(returnOrderList);

                if (success == true) {
                    successOrder();
                    Log.d(TAG, "successOrder 성공: ");
                } else {
                    orderCk = false;
                }
            }catch (InterruptedException e){
                e.printStackTrace();
            }

        }

        String savedJson = pref.getString("cart_list", null);


        if (savedJson != null) {
            cartLists = getSharedPreference(savedJson);
            cartAdapter.setAdapterItem(cartLists);

            for (CartList menu : cartLists) {
                totalPrice += menu.getMenu_price() * menu.getMenu_quantity();
            }

            cartOrderPrice.setText(String.valueOf(totalPrice));
        }

    } //onStart()


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onResume() {
        super.onResume();
        //사용자와 상호작용 하는 단계, 어플 기능은 여기서

        menuClose.setOnClickListener(view -> {
            //admin에게 fcm 날리기
            Log.d(TAG, "menuClose Click: ");
            sendNotification.usingTable(get_id, "종료");

            editor.remove("cart_list");
            editor.commit();
            finish();

        });


        /**
         * Appbar: table 클래스로 이동
         */
        appbarMenuTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (clientSocket != null && clientSocket.getTableList() != null) {
                    tableList = clientSocket.getTableList();

                }

                Intent intent = new Intent(getApplicationContext(), Table.class);
                intent.putExtra("get_id", get_id);
                intent.putExtra("orderCk", orderCk);
                intent.putExtra("tableList", tableList);
//                intent.putExtra("clientSocket", clientSocket);
                intent.putExtra("paymentStyle", paymentStyle);
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
                cartOrderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");
                cartAdapter.setAdapterItem(cartLists);

                saveSharedPreference();
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

                cartOrderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");
                cartAdapter.setAdapterItem(cartLists);
                saveSharedPreference();
            }


            @Override
            public void onDeleteClick(View view, int position) {
                int delete_quantity = cartLists.get(position).getMenu_quantity();
                int delete_price = cartLists.get(position).getMenu_price();

                totalPrice = totalPrice - (delete_price * delete_quantity);
                cartOrderPrice.setText("합계: " + String.valueOf(totalPrice) + "원");

                cartLists.remove(position);

                cartAdapter.setAdapterItem(cartLists);
                saveSharedPreference();
            }
        });


        /**
         * 클릭하면 장바구니에 담기게
         */
        menuAdapter.setOnItemClickListener(new MenuAdapter.OnItemClickListener() {

            int pos = 1000;
            int menuQuantity;

            @Override
            public void onItemClick(View view, String name, int price, int position) {

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
                cartOrderPrice.setText(String.valueOf("합계: " + totalPrice + " 원"));

                saveSharedPreference();

            }
        });


        sideAdapter.setOnItemClickListener(new SideListViewAdapter.OnItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onItemClick(View view, String where) {
                if (where.equals("메인안주")) {
                    menuRecyclerview.smoothScrollToPosition(0);

                } else if (where.equals("주류")) {
                    menuRecyclerview.smoothScrollToPosition(9);

                } else if (where.equals("사이드")) {
                    menuRecyclerview.smoothScrollToPosition(12);

                } else if (where.equals("직원호출")) {
                    Intent intent = new Intent(Menu.this, CallServer.class);
                    startActivity(intent);

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

        cartOrderButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {

                if (cartLists.size() == 0) {
                    dialogCustom.HandlerAlertDialog(Menu.this, "메뉴가 존재하지 않습니다.");

                } else {

                    if (paymentStyle.equals("after")) {
                        // fcm으로 날리고
                        sendNotification.sendMenu(adminOrderMenuList(cartLists));
                        Log.d(TAG, "onClick: ");
                        successOrder();

                        if (clientSocket == null) {
                            clientSocket = new ClientSocket(get_id, Menu.this, tableList);
                            clientSocket.start();
                            Log.d(TAG, "연결?: ");
                        }



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
                    }

                } // cartList에 데이터 if-else
            }
        }); //주문하기 click event


        myTable = Integer.parseInt(get_id.replace("table", ""));


        if (tableList == null) {
            Log.d(TAG, "onResume tableList null: ");

            tableList = new ArrayList<>();

            for (int i = 1; i < tableFromDB + 1; i++) {
                if (i == myTable) {
                    tableList.add(new TableList(get_id, (Drawable) null, 0));
                } else {
                    tableList.add(new TableList(i, (Drawable) null, 1));
                }
            }


        } else {
            Log.d(TAG, "onResume tableList not null: ");
        }

        Log.d(TAG, "tableList :" + tableList.size());


    } // onResume

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

    public String adminOrderMenuList(ArrayList<CartList> cartLists) {
        JSONObject jsonObject = new JSONObject();
        try {
//
            menujArray = new JSONArray();//배열이 필요할때

            for (int i = 0; i < cartLists.size(); i++)//배열
            {
                //배열 내에 들어갈 json
                JSONObject sObject = new JSONObject();
                sObject.put("menu", cartLists.get(i).getMenu_name());
                sObject.put("price", cartLists.get(i).getMenu_price());
                sObject.put("quantity", cartLists.get(i).getMenu_quantity());
                menujArray.put(sObject);
            }

            jsonObject.put("item", menujArray);
            jsonObject.put("menuName", getOrderMenuName(cartLists));
            jsonObject.put("tableName", get_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }

    public void saveSharedPreference() {

        JSONArray jsonArray = new JSONArray();

        for (CartList menu : cartLists) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("menu", menu.getMenu_name());
                jsonObject.put("quantity", menu.getMenu_quantity());
                jsonObject.put("price", menu.getMenu_price());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jsonArray.put(jsonObject);
        }

        String json = jsonArray.toString();
        editor.putString("cart_list", json);
        editor.commit();

    }

    public ArrayList getSharedPreference(String savedJson) {
        try {
            JSONArray jsonArray = new JSONArray(savedJson);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String menuName = jsonObject.getString("menu");
                int menuQuantity = jsonObject.getInt("quantity");
                int menuPrice = jsonObject.getInt("price");
                cartLists.add(new CartList(menuName, menuPrice, menuQuantity, 1));
            }
        } catch (JSONException e) {

        }
        return cartLists;
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

                String url = "http://3.36.255.141/menuImage/" + jsonObject.getString("img");
                String menu = jsonObject.getString("menu");
                int price = jsonObject.getInt("price");
                int menuType = jsonObject.getInt("type");

                menuLists.add(new MenuList(url, menu, price, menuType, 1));


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


    public void successOrder() {
        orderCk = true;
        cartLists = new ArrayList<>();
        cartAdapter.setAdapterItem(cartLists);
        totalPrice = 0;
        cartOrderPrice.setText("");

        editor.remove("cart_list");
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
}
