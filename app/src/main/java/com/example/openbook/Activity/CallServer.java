package com.example.openbook.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.example.openbook.Adapter.CartAdapter;
import com.example.openbook.Adapter.RequestAdapter;

import com.example.openbook.Category.CartCategory;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.TableList;
import com.example.openbook.Data.TicketData;
import com.example.openbook.DialogManager;
import com.example.openbook.R;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.RequestList;

import java.util.ArrayList;
import java.util.HashMap;

public class CallServer extends AppCompatActivity {

    String TAG = "CallTAG";
    ArrayList<RequestList> requestList;
    ArrayList<CartList> cartLists;

    boolean menuExist = false;
    MyData myData;
    ArrayList<TableList> tableLists;
    DialogManager dialogManager;


    //액티비티가 onCreate 되면 자동으로 받는거고
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "giftArrived":
                    String from = intent.getStringExtra("from");
                    String menuItem = intent.getStringExtra("menuItem");
                    String count = intent.getStringExtra("count");

                    dialogManager.giftReceiveDialog(CallServer.this, myData.getId(), from, menuItem, count).show();
                    break;

                case "isGiftAccept":
                    from = intent.getStringExtra("from");
                    boolean isAccept = intent.getBooleanExtra("isAccept", false);
                    String message;
                    if(isAccept){
                        message = from + "에서 선물을 수락하였습니다.";
                        //여기서 메뉴 주문 메뉴 저장하기
                    }else{
                        message = from + "에서 선물을 거절하였습니다.";
                    }
                    dialogManager.positiveBtnDialog(CallServer.this, message).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        tableLists = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

        dialogManager = new DialogManager();

        TextView close = findViewById(R.id.call_server_close);
        close.setOnClickListener(view -> {

            Intent intent = new Intent(CallServer.this, Menu.class);
            intent.putExtra("myData", myData);
            intent.putExtra("tableList", tableLists);

            startActivity(intent);
        });


        RecyclerView request = findViewById(R.id.request_item);
        RequestAdapter requestAdapter = new RequestAdapter();

        request.setLayoutManager(new GridLayoutManager(CallServer.this, 5));
        request.setAdapter(requestAdapter);


        requestList = new ArrayList<>();

        String[] service = getResources().getStringArray(R.array.service);


        for (String item : service) {
            requestList.add(new RequestList(item));
        }

        requestAdapter.setAdapterItem(requestList);


        /**
         * 직원만 부르기 연결
         */
        TextView call_server = findViewById(R.id.call_server);
        call_server.setOnClickListener(view -> {
            Toast.makeText(getApplicationContext(), "직원을 호출하였습니다.", Toast.LENGTH_LONG).show();
            //뭔가 뿅! 하는거
        });


        /**
         * 요구사항 장바구니
         */
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, 1);
        dividerItemDecoration.setDrawable(AppCompatResources.getDrawable(this, R.drawable.divider_gray));



        RecyclerView cart = findViewById(R.id.request_item_cart);
        cart.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        cart.addItemDecoration(dividerItemDecoration);

        RecyclerView.ItemAnimator animator = cart.getItemAnimator();
        if(animator instanceof SimpleItemAnimator){
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        CartAdapter cartAdapter = new CartAdapter();
        cart.setAdapter(cartAdapter);

        cartLists = new ArrayList<>();


        /**
         * 아이템을 클릭하면 장바구니로 넣어진다
         */
        requestAdapter.setOnItemClickListener((view, position, name) -> {

            for(int i=0; i<cartLists.size(); i++){
                if (cartLists.get(i).getMenuName().equals(name)) {
                    menuExist = true;
                    int newQuantity = cartLists.get(i).getMenuQuantity() + 1;
                    cartLists.get(i).setMenuQuantity(newQuantity);
                    cartAdapter.notifyItemChanged(i);
                    break;
                }
            }

            if (!menuExist) {
                cartLists.add(new CartList(name, 1, CartCategory.SERVER));
                cartAdapter.setAdapterItem(cartLists);
            }

            menuExist = false;

        });


        /**
         * 장바구니 plus, minus, delete 기능
         */

        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onPlusClick(View view, int position) {
                int addQuantity = cartLists.get(position).getMenuQuantity() + 1;
                cartLists.get(position).setMenuQuantity(addQuantity);
                cartAdapter.notifyItemChanged(position);
            }

            @Override
            public void onMinusClick(View view, int position) {
                int reduceQuantity = cartLists.get(position).getMenuQuantity() - 1;

                if (reduceQuantity == 0) {
                    cartLists.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                } else {
                    cartLists.get(position).setMenuQuantity(reduceQuantity);
                    cartAdapter.notifyItemChanged(position);
                }
            }

            @Override
            public void onDeleteClick(View view, int position) {
                cartLists.remove(position);
                cartAdapter.notifyItemRemoved(position);
            }


        });


        /**
         * 전체 삭제
         */
        TextView delete_all = findViewById(R.id.requst_delete_all);
        delete_all.setOnClickListener(view -> {
            cartLists = new ArrayList<>();
            cartAdapter.setAdapterItem(cartLists);

        });


        /**
         * 요청하기
         */
        TextView please = findViewById(R.id.please);
        please.setOnClickListener(view -> {
            cartLists = new ArrayList<>();
            cartAdapter.setAdapterItem(cartLists);
            Toast.makeText(getApplicationContext(), "요청이 완료되었습니다. ", Toast.LENGTH_LONG).show();
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("giftArrived");
        intentFilter.addAction("isGiftAccept");
        LocalBroadcastManager.getInstance(CallServer.this).registerReceiver(broadcastReceiver, intentFilter);

    }
}
