package com.example.openbook;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

import androidx.annotation.NonNull;

import com.example.openbook.Activity.Table;
import com.example.openbook.FCMclass.SendNotification;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DialogCustom {

    String TAG = "DialogCustom";
    String ticket;

    public String getTicket() {
        return ticket;
    }



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
                        intent.putExtra("get_id", id);
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
                                        sendNotification.requestChatting(clickTable, get_id,"yesTicket",
                                                "에서 채팅을 요청하였습니다. 수락하시겠습니까?\n** 프로필 오픈 티켓 동봉 **");
                                        // 여기서 realtimeBase로 바로 넣어서 조회하기...!?!??
                                        //sendNotification class에서 저장하고, table.class에서 조회해서 까보기..!!!!!
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        SendNotification sendNotification = new SendNotification();
                                        sendNotification.requestChatting(clickTable, get_id,"noTicket",
                                                "에서 채팅을 요청하였습니다. 수락하시겠습니까?");
                                        dialog.dismiss();
                                    }
                                }).setIcon(R.drawable.heart);
                        AlertDialog alertDialog2 = builder.create();
                        alertDialog2.show();
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

    public void buyProfileTicket(Context context, String message, String get_id){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle("프로필 조회권 구매")
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setPositiveButton("구매", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //json으로 만들어서 db에 넣자
                        RequestBody formBody = new FormBody.Builder()
                                .add("json", json(get_id))
                                .build();

                        Request request = new Request.Builder()
                                .url("http://3.36.255.141/saveOrder.php")
                                .post(formBody)
                                .build();

                        final OkHttpClient client = new OkHttpClient();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                                Log.d(TAG, "onFailure: " + e);
                            }

                            @Override
                            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                                String body = response.body().string();

                                Log.d(TAG, "onResponse: " + body);
                                if(body.equals("주문완료")){
                                    ticket = "yesTicket";

                                    Log.d(TAG, "Dialog ticket :"+ticket);
                                }
                            }
                        });
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }



    public String json(String tableName){

        JSONObject obj = new JSONObject();
        try {
            JSONArray jArray = new JSONArray();//배열이 필요할때

            JSONObject sObject = new JSONObject();//배열 내에 들어갈 json

            sObject.put("menu", "ticket");
            sObject.put("price", 2000);
            sObject.put("number", 1);
            sObject.put("ticket", "anything");
            jArray.put(sObject);

            obj.put("table", tableName);
            obj.put("orderTime", getTime());
            obj.put("item", jArray);//배열을 넣음

            Log.d(TAG, "getJson: " + obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj.toString();
    }

    public String getTime() {
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String getTime = dateFormat.format(date);

        return getTime;
    }



}
