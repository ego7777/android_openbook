package com.example.openbook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;

import com.example.openbook.Activity.Table;
import com.example.openbook.FCMclass.SendNotification;

public class DialogCustom {

    String TAG = "DialogCustom";


    public void showAlertDialog(Context context, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("알람")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.warning);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void HandlerAlertDialog(Context context, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("알람")
                .setIcon(R.drawable.warning);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                alertDialog.dismiss();
            }
        }, 1000);
    }

    //Menu Activity 에서 Table Activity 로 넘어가는 dialog
    public void moveActivity(Context context, String message, String id, Boolean orderCk){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle("알람")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(context, Table.class);
                        intent.putExtra("id", id);
                        intent.putExtra("orderCk", orderCk);
                        context.startActivity(intent);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.warning);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }






    //Table.class에서 채팅하기 누르면 나오는 dialog
    public void chattingRequest(Context context, String message, String clickTable, String get_id){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("채팅 신청")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        builder.setMessage("상대방 테이블에게 프로필 사진 조회권을 보내시겠습니까?\n* 2,000원의 추가금이 발생합니다.")
                                .setTitle("프로필 사진 동봉")
                                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //프로필 조회권 주고
                                        SendNotification sendNotification = new SendNotification();
                                        sendNotification.requestChatting(clickTable, get_id);
                                    }
                                })
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SendNotification sendNotification = new SendNotification();
                                        sendNotification.requestChatting(clickTable, get_id);
                                    }
                                }).setIcon(R.drawable.heart);
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(R.drawable.heart);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }


}
