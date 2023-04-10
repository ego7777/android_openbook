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
import com.example.openbook.FCMclass.SendNotification;
import com.example.openbook.R;

public class PopUp extends Activity {

    String TAG = "POPUP_Activity";
    String get_id;
    int tableNumber;
    boolean agree = false;
    String title;
    String body;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup);

        TextView popup_title = findViewById(R.id.popup_title);
        TextView popup_body = findViewById(R.id.popup_body);

        title = getIntent().getStringExtra("notificationTitle");
        body = getIntent().getStringExtra("notificationBody");
        get_id = getIntent().getStringExtra("notificationClickTable");

        popup_title.setText(title);
        popup_body.setText(body);

        tableNumber = Integer.parseInt(title.replace("table", ""));

    }


    @Override
    protected void onResume() {
        super.onResume();

        Button popup_yes = findViewById(R.id.popup_button_yes);


        if(body.contains("요청")){
            popup_yes.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "요청포함");
                    // 여기서 yes를 누르면 그 후로는 바로 chatUI로 넘어가게 해야함
                    SendNotification sendNotification = new SendNotification();
                    sendNotification.requestChatting(title,get_id, "에서 채팅을 수락하였습니다.");

                    agree = true;
                    Intent intent = new Intent(PopUp.this, ChattingUI.class);
                    intent.putExtra("id", get_id);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("chattingAgree", agree);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();

                }
            });
        }else if(body.contains("수락")){
            popup_yes.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "수락포함");
                    agree = true;
                    Intent intent = new Intent(PopUp.this, ChattingUI.class);
                    intent.putExtra("id", get_id);
                    intent.putExtra("tableNumber", tableNumber);
                    intent.putExtra("chattingAgree", agree);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            });

        }



        Button popup_no = findViewById(R.id.popup_button_no);
        popup_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agree = false;
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
