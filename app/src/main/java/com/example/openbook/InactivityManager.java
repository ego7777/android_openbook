package com.example.openbook;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.TextView;

import com.example.openbook.Activity.Table;
import com.example.openbook.Data.MyData;
import com.example.openbook.Data.TableList;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class InactivityManager {
    public  Timer inactivityTimer;
    Context context;
    MyData myData;
    ArrayList<TableList> tableList;

    public InactivityManager(Context context, MyData myData, ArrayList<TableList> tableList){
        this.context = context;
        this.myData = myData;
        this.tableList = tableList;
    }

    public void startInactivityTimer() {
        inactivityTimer = new Timer();
        inactivityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                switchActivity();
            }
        }, 30000);

        showCountdownDialog();
    }

    public void resetInactivityTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
            startInactivityTimer();
        }
    }

    public void switchActivity(){
        Intent intent = new Intent(context, Table.class);
        intent.putExtra("myData", myData);
        intent.putExtra("tableList", tableList);
        context.startActivity(intent);
    }

    private void showCountdownDialog() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.diaglog_inactivity_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = (int) (displayMetrics.widthPixels * 0.7);
        int height = (int) (displayMetrics.heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);

        TextView count = dialog.findViewById(R.id.inactivity_count);
        Button initButton = dialog.findViewById(R.id.inactivity_button);


        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000);
                    count.setText(String.valueOf(secondsRemaining));
                }

                @Override
                public void onFinish() {
                    dialog.dismiss();
                }
            };
            dialog.setOnDismissListener(dialogInterface -> {
                if(countDownTimer != null){
                    countDownTimer.cancel();
                }
            });

            dialog.show();

            initButton.setOnClickListener(view -> {
                dialog.dismiss();
                countDownTimer.cancel();
                resetInactivityTimer();
            });

            countDownTimer.start();
        }, 20000);
    }

}
