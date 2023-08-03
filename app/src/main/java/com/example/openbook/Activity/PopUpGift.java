package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.openbook.Data.ChattingData;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.TableList;
import com.example.openbook.Data.TicketData;
import com.example.openbook.R;

import java.util.ArrayList;
import java.util.HashMap;

public class PopUpGift extends Activity {

    MyData myData;
    HashMap<String, ChattingData> chattingDataHashMap;
    HashMap<String, TicketData> ticketDataHashMap;
    ArrayList<TableList> tableList;

    String from, menuName;

    TextView popup_title, popup_body;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup);

        popup_title = findViewById(R.id.popup_title);
        popup_body = findViewById(R.id.popup_body);

        from = getIntent().getStringExtra("from");
        menuName = getIntent().getStringExtra("menuName");

        popup_title.setText("선물이 도착했습니다 :)");
        popup_body.setText(from + "에서 " + menuName + "을 선물하였습니다\n"
                + from + "에게 감사의 인사를 보내볼까요?");

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
        popup_no.setVisibility(View.GONE);
        popup_yes.setText("확인");

        popup_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PopUpGift.this, Table.class);
                intent.putExtra("myData", myData);
                intent.putExtra("chattingData", chattingDataHashMap);
                intent.putExtra("ticketData", ticketDataHashMap);
                intent.putExtra("tableList", tableList);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
        });
    }
}
