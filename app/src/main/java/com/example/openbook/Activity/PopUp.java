package com.example.openbook.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.openbook.Chatting.ChattingUI;
import com.example.openbook.R;

public class PopUp extends Activity {

    String TAG = "POPUP_Activity";
    String get_id;
    int tableNumber;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup);

        TextView popup_title = findViewById(R.id.popup_title);
        TextView popup_body = findViewById(R.id.popup_body);

        String title = getIntent().getStringExtra("notificationTitle");
        String body = getIntent().getStringExtra("notificationBody");
        get_id = getIntent().getStringExtra("notificationClickTable");

        popup_title.setText(title);
        popup_body.setText(body);

        tableNumber = Integer.parseInt(title.replace("table", ""));

    }


    @Override
    protected void onResume() {
        super.onResume();

        Button popup_yes = findViewById(R.id.popup_button_yes);
        popup_yes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PopUp.this, ChattingUI.class);
                intent.putExtra("id", get_id);
                intent.putExtra("tableNumber", tableNumber);
                startActivity(intent);
                finish();
            }
        });

        Button popup_no = findViewById(R.id.popup_button_no);
        popup_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥 레이어 클릭해도 안닫히게 하기
        if(event.getAction() == MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
