package com.example.openbook;

import android.app.Activity;
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
    public Timer inactivityTimer;
    public CountDownTimer countDownTimer;
    Activity activity;
    MyData myData;
    ArrayList<TableList> tableList;
    Dialog dialog;

    public InactivityManager(Activity activity, MyData myData, ArrayList<TableList> tableList) {
        this.activity = activity;
        this.myData = myData;
        this.tableList = tableList;
    }

    public void startInactivityTimer() {
        inactivityTimer = new Timer();

        inactivityTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Handler(Looper.getMainLooper()).post(() -> showCountdownDialog());
            }
        }, 2000);
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

    public void stopTimer() {
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        if (dialog != null || dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void switchActivity() {
        Intent intent = new Intent(activity, Table.class);
        intent.putExtra("myData", myData);
        intent.putExtra("tableList", tableList);
        activity.startActivity(intent);
    }

    public void showCountdownDialog() {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        dialog = new Dialog(activity);
        dialog.setContentView(R.layout.diaglog_inactivity_notification);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);

        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        int width = (int) (displayMetrics.widthPixels * 0.7);
        int height = (int) (displayMetrics.heightPixels * 0.7);
        dialog.getWindow().setLayout(width, height);

        TextView count = dialog.findViewById(R.id.inactivity_count);
        Button initButton = dialog.findViewById(R.id.inactivity_button);


        countDownTimer = new CountDownTimer(10000, 1000) {
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
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
        });

        initButton.setOnClickListener(view -> {
            dialog.dismiss();
            countDownTimer.cancel();
            resetInactivityTimer();
        });

        countDownTimer.start();

    }

}
