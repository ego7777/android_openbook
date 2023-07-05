package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class PopUpProfile extends Activity {

    String TAG = "profilePopUpTAG";
    /**
     * 해당 액티비티는 내가 눌러서 내가 구매하는 프로필 티켓
     */

    String title, body;
    int clickTable;
    ArrayList<TableList> tableList;

    MyData myData;
    HashMap<String, ChattingData> chattingDataHashMap;
    HashMap<String, TicketData> ticketDataHashMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        Log.d(TAG, "myData id: " + myData.getId());

        chattingDataHashMap = (HashMap<String, ChattingData>) getIntent().getSerializableExtra("chattingData");
        Log.d(TAG, "chattingData: " + chattingDataHashMap);

        ticketDataHashMap = (HashMap<String, TicketData>) getIntent().getSerializableExtra("ticketData");
        Log.d(TAG, "ticketData: " + ticketDataHashMap);

        TextView popup_title = findViewById(R.id.popup_title);
        TextView popup_body = findViewById(R.id.popup_body);

        title = getIntent().getStringExtra("title");
        body = getIntent().getStringExtra("body");
        clickTable = getIntent().getIntExtra("clickTable", 0);
        Log.d(TAG, "clickTable: " + clickTable);

        tableList = (ArrayList<TableList>) getIntent().getSerializableExtra("tableList");

        popup_title.setText(title);
        popup_body.setText(body);


    }

    @Override
    protected void onResume() {
        super.onResume();

        Button popup_yes = findViewById(R.id.popup_button_yes);
        Button popup_no = findViewById(R.id.popup_button_no);


        popup_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "yes click");

                if (ticketDataHashMap == null) {
                    ticketDataHashMap = new HashMap<>();
                    Log.d(TAG, "new ticketHashMap");
                }

                String selectedTable = "table" + clickTable;

                // 티켓이 없으니까 해당 액티비티가 나온 것. 고로 데이터만 만들어주면 됨.
                ticketDataHashMap.put(selectedTable,
                        new TicketData(myData.getId(), false, selectedTable));

//                }

                Log.d(TAG, "ticket add :" + ticketDataHashMap.get(selectedTable).getUseTable());

                Intent intent = new Intent(PopUpProfile.this, Table.class);
                intent.putExtra("myData", myData);
                intent.putExtra("chattingData", chattingDataHashMap);
                intent.putExtra("ticketData", ticketDataHashMap);
                intent.putExtra("tableList", tableList);
                startActivity(intent);
                finish();

            }
        });

        popup_no.setOnClickListener(view ->{
            Intent intent = new Intent(PopUpProfile.this, Table.class);
            intent.putExtra("myData", myData);
            intent.putExtra("chattingData", chattingDataHashMap);
            intent.putExtra("ticketData", ticketDataHashMap);
            intent.putExtra("tableList", tableList);
            startActivity(intent);
            finish();
        });

    }
}
