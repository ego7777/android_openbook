package com.example.openbook.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.CartAdapter;
import com.example.openbook.Adapter.RequestAdapter;

import com.example.openbook.R;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.RequestList;

import java.util.ArrayList;

public class CallServer extends AppCompatActivity {

    String TAG = "CallTAG";
    ArrayList<RequestList> request_item;
    ArrayList<CartList> cartLists;
    int count;
    int pos = 1000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.call_server);


        TextView close = findViewById(R.id.call_server_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });


        /**
         * 요구사항 리사이클러뷰 생성
         */
        RecyclerView request = findViewById(R.id.request_item);
        RequestAdapter request_adapter = new RequestAdapter();

        request.setLayoutManager(new GridLayoutManager(CallServer.this, 5));
        request.setAdapter(request_adapter);

        request_item = new ArrayList<>();

        String request_what[] = {"가위",
                "담요",
                "앞치마",
                "육수추가",
                "국자",
                "얼음",
                "물",
                "물티슈",
                "앞접시",
        "소주잔",
        "맥주잔",
        "수저",
        "젓가락",
        "부탄가스",
        "냅킨",
        "병따개"};

        for(int i=0; i<request_what.length; i++){
            request_item.add(new RequestList(request_what[i]));
            Log.d(TAG, "추가되니 : " + request_item.get(i));
        }


        request_adapter.setAdapterItem(request_item);


        /**
         * 직원만 부르기 연결
         */
        TextView call_server = findViewById(R.id.call_server);
        call_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "직원을 호출하였습니다.", Toast.LENGTH_LONG).show();
                //뭔가 뿅! 하는거
            }
        });






        /**
         * 요구사항 장바구니
         */
        RecyclerView cart = findViewById(R.id.request_item_cart);
        cart.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        CartAdapter cartAdapter = new CartAdapter();
        cart.setAdapter(cartAdapter);

        cartLists = new ArrayList<>();




        /**
         * 아이템을 클릭하면 장바구니로 넣어진다
         */
        request_adapter.setOnItemClickListener(new RequestAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position, String name) {

                //중복되는 메뉴의 포지션 값 get
                for (int i = 0; i < cartLists.size(); i++) {
                    if (cartLists.get(i).getMenu_name().equals(name)) {
                        pos = i;
                        Log.d(TAG, "pos : " + position);
                    }
                }


                //포지션 값이 초기값이면 새롭게 추가하고, 아니면 개수만 올려서 다시 적용
                if (pos == 1000) {
                    cartLists.add(new CartList(name, 1, 0));
                }else {
                    count = cartLists.get(position).getMenu_count() + 1;
                    cartLists.get(position).setMenu_count(count);
                }

                cartAdapter.setAdapterItem(cartLists);

                pos=1000;

            }
        });


        /**
         * 장바구니 plus, minus, delete 기능
         */

        cartAdapter.setOnItemClickListener(new CartAdapter.OnItemClickListener() {
            @Override
            public void onPlusClick(View view, int position) {
                int add = cartLists.get(position).getMenu_count() + 1;
                cartLists.get(position).setMenu_count(add);
                cartAdapter.setAdapterItem(cartLists);

            }

            @Override
            public void onMinusClick(View view, int position) {
                int minus = cartLists.get(position).getMenu_count()-1;


                if(minus==0){
                    cartLists.remove(position);
                }else{
                    cartLists.get(position).setMenu_count(minus);
                }


                cartAdapter.setAdapterItem(cartLists);

            }

            @Override
            public void onDeleteClick(View view, int position) {
                cartLists.remove(position);

                cartAdapter.setAdapterItem(cartLists);

            }


        });


        /**
         * 전체 삭제
         */
        TextView delete_all = findViewById(R.id.requst_delete_all);
        delete_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartLists = new ArrayList<>();
                cartAdapter.setAdapterItem(cartLists);

            }
        });


        /**
         * 요청하기
         */
        TextView please =findViewById(R.id.please);
        please.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartLists = new ArrayList<>();
                cartAdapter.setAdapterItem(cartLists);
                Toast.makeText(getApplicationContext(), "요청이 완료되었습니다. ", Toast.LENGTH_LONG).show();
            }
        });



    }
}
