package com.example.openbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Activity.Menu;
import com.example.openbook.Activity.Table;
import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.Data.OrderList;
import com.example.openbook.FCM.SendNotification;

import java.util.ArrayList;


public class DialogManager {

    String TAG = "DialogManagerTAG";

    public Dialog progressDialog(Context context){
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(context));
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }


    public Dialog positiveBtnDialog(Context context, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("알림")
                .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.openbook_logo);

        AlertDialog alertDialog = builder.create();

        return alertDialog;
    }

    public void noButtonDialog(Context context, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("알림")
                .setIcon(R.drawable.warning);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Handler handler = new Handler();

        handler.postDelayed(() -> alertDialog.dismiss(), 1000);
    }

    //Menu Activity 에서 Table Activity 로 넘어가는 dialog
    public void moveActivity(Context context, String message, String id, Boolean orderCk, ClientSocket clientSocket){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setTitle("알람")
                .setPositiveButton("확인", (dialog, which) -> {
                    Intent intent = new Intent(context, Table.class);
                    intent.putExtra("get_id", id);
                    intent.putExtra("orderCk", orderCk);
                    intent.putExtra("clientSocket", clientSocket);
                    Log.d(TAG, "clientSocket : " + clientSocket.isAlive());
                    context.startActivity(intent);
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.warning);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }






    //Table.class에서 채팅하기 누르면 나오는 dialog
    public void chattingRequest(Context context, String message, String clickTable, String get_id){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("채팅 신청")
                .setPositiveButton("확인", (dialog, which) -> {

                    builder.setMessage("상대방 테이블에게 프로필 사진 조회권을 보내시겠습니까?\n* 2,000원의 추가금이 발생합니다.")
                            .setTitle("프로필 사진 동봉")
                            .setPositiveButton("네", (dialog1, which1) -> {
                                //프로필 조회권 주고
                                SendNotification sendNotification = new SendNotification();
                                sendNotification.requestChatting(clickTable, get_id,"yesTicket",
                                        "에서 채팅을 요청하였습니다. 수락하시겠습니까?\n** 프로필 오픈 티켓 동봉 **");
                                //
                                //sendNotification class에서 저장하고, table.class에서 조회해서 까보기..!!!!!
                                dialog1.dismiss();
                            })
                            .setNegativeButton("아니오", (dialog12, which12) -> {
                                SendNotification sendNotification = new SendNotification();
                                sendNotification.requestChatting(clickTable, get_id,"noTicket",
                                        "에서 채팅을 요청하였습니다. 수락하시겠습니까?");
                                dialog12.dismiss();
                            }).setIcon(R.drawable.heart);
                    AlertDialog alertDialog2 = builder.create();
                    alertDialog2.show();
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.heart);


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    public Dialog popUpAdmin(Context context, ArrayList<OrderList> orderList){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.admin_popup);

        TextView popUpTitle = dialog.findViewById(R.id.admin_popup_title);
        popUpTitle.setText(orderList.get(0).getTableName());

        RecyclerView popUpRecyclerview = dialog.findViewById(R.id.admin_popup_body_recyclerView);
        Button popUpButton = dialog.findViewById(R.id.admin_popup_button);

        AdminPopUpAdapter adapter = new AdminPopUpAdapter();
        popUpRecyclerview.setLayoutManager(new LinearLayoutManager(context));
        popUpRecyclerview.setAdapter(adapter);
        adapter.setAdapterItem(orderList);

        popUpButton.setOnClickListener(view -> dialog.dismiss());

        Handler handler = new Handler();
        handler.postDelayed(() -> dialog.dismiss(), 5000);

        return dialog;
    }

    public Dialog successOrder(Context context){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.order_complete);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView img = dialog.findViewById(R.id.serve_img);
        TextView text = dialog.findViewById(R.id.serve_text);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.order_complete);
        img.startAnimation(animation);
        text.startAnimation(animation);

        Handler handler = new Handler();

        handler.postDelayed(dialog::dismiss, 1000);
        return dialog;
    }

}
