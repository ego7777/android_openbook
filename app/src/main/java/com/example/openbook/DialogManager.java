package com.example.openbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.openbook.Activity.Table;
import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Adapter.MenuAdapter;
import com.example.openbook.Chatting.ClientSocket;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.MenuList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.Deco.menu_recyclerview_deco;
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.QRcode.MakeQR;
import com.example.openbook.retrofit.AdminTableDTO;
import com.example.openbook.retrofit.SalesItemDTO;
import com.example.openbook.retrofit.TableInformationDTO;



import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;


public class DialogManager {

    String TAG = "DialogManagerTAG";

    public Dialog progressDialog(Context context) {
        Dialog dialog = new Dialog(context);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(context));
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }


    public Dialog positiveBtnDialog(Context context, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("알림")
                .setPositiveButton("확인", (dialog, which) -> dialog.dismiss())
                .setIcon(R.drawable.openbook_logo);

        return builder.create();
    }

    public void noButtonDialog(Context context, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("알림")
                .setIcon(R.drawable.warning);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Handler handler = new Handler();

        handler.postDelayed(alertDialog::dismiss, 1000);
    }

    //Menu Activity 에서 Table Activity 로 넘어가는 dialog
    public void moveActivity(Context context, String message, String id, Boolean orderCk, ClientSocket clientSocket) {
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
    public void chattingRequest(Context context, String message, String clickTable, String get_id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage(message)
                .setTitle("채팅 신청")
                .setPositiveButton("확인", (dialog, which) -> {

                    builder.setMessage("상대방 테이블에게 프로필 사진 조회권을 보내시겠습니까?\n* 2,000원의 추가금이 발생합니다.")
                            .setTitle("프로필 사진 동봉")
                            .setPositiveButton("네", (dialog1, which1) -> {
                                //프로필 조회권 주고
                                SendNotification sendNotification = new SendNotification();
                                sendNotification.requestChatting(clickTable, get_id, "yesTicket",
                                        "에서 채팅을 요청하였습니다. 수락하시겠습니까?\n** 프로필 오픈 티켓 동봉 **");
                                //
                                //sendNotification class에서 저장하고, table.class에서 조회해서 까보기..!!!!!
                                dialog1.dismiss();
                            })
                            .setNegativeButton("아니오", (dialog12, which12) -> {
                                SendNotification sendNotification = new SendNotification();
                                sendNotification.requestChatting(clickTable, get_id, "noTicket",
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

    public Dialog popUpAdmin(Context context, ArrayList<OrderList> orderList) {
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
        handler.postDelayed(dialog::dismiss, 5000);

        return dialog;
    }

    public Dialog successOrder(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.order_complete);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView img = dialog.findViewById(R.id.serve_img);
        TextView text = dialog.findViewById(R.id.serve_text);

        Animation animation = AnimationUtils.loadAnimation(context, R.anim.order_complete);
        img.startAnimation(animation);
        text.startAnimation(animation);

        Handler handler = new Handler();

        handler.postDelayed(dialog::dismiss, 1000);
        return dialog;
    }

    public Dialog showReceiptDialog(Context context, ArrayList<OrderList> orderLists, String totalPrice) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.receipt_dialog);

        TextView receiptCancel = dialog.findViewById(R.id.receipt_cancel);
        TextView receiptTotalPrice = dialog.findViewById(R.id.receipt_total_price);
        RecyclerView receiptRecyclerView = dialog.findViewById(R.id.receipt_recyclerView);

        AdminPopUpAdapter menuReceiptAdapter = new AdminPopUpAdapter();
        receiptRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        receiptRecyclerView.setAdapter(menuReceiptAdapter);
        menuReceiptAdapter.setAdapterItem(orderLists);

        receiptTotalPrice.setText(totalPrice);

        receiptCancel.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    public Dialog showSalesItemsDialog(Context context,
                                       List<SalesItemDTO.SalesItemData> salesItem,
                                       String header) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.admin_sales_items_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView dialogCancel = dialog.findViewById(R.id.sales_items_dialog_cancel);
        TextView dialogHeader = dialog.findViewById(R.id.sales_items_dialog_header);
        dialogHeader.setText(header);

        String url = BuildConfig.SERVER_IP + "/MenuImages/";

        ImageView mainImage = dialog.findViewById(R.id.sales_items_main_image);
        ImageView sideImage = dialog.findViewById(R.id.sales_items_side_image);
        ImageView drinkImage = dialog.findViewById(R.id.sales_items_drink_image);

        TextView mainName = dialog.findViewById(R.id.sales_items_main_name);
        TextView sideName = dialog.findViewById(R.id.sales_items_side_name);
        TextView drinkName = dialog.findViewById(R.id.sales_items_drink_name);

        for (SalesItemDTO.SalesItemData item : salesItem) {
            String imageURL = url + item.getImageUrl();

            switch (item.getMenuCategory()) {
                case 0:
                    mainName.setText(item.getMenuName());
                    Glide.with(mainImage.getContext())
                            .load(imageURL)
                            .placeholder(getProgress(mainImage.getContext()))
                            .into(mainImage);
                    break;
                case 1:
                    sideName.setText(item.getMenuName());
                    Glide.with(sideImage.getContext())
                            .load(imageURL)
                            .placeholder(getProgress(sideImage.getContext()))
                            .into(sideImage);

                    break;
                case 2:
                    drinkName.setText(item.getMenuName());
                    Glide.with(drinkImage.getContext())
                            .load(imageURL)
                            .placeholder(getProgress(drinkImage.getContext()))
                            .into(drinkImage);
                    break;
            }
        }

        dialogCancel.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    public Dialog addMenu(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.admin_modify_menu);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView cancel = dialog.findViewById(R.id.admin_modify_menu_cancel);
        ImageView qrCode = dialog.findViewById(R.id.admin_modify_menu_qrCode);

        MakeQR makeQR = new MakeQR();
        qrCode.setImageBitmap(makeQR.adminQr());

        cancel.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    public Dialog myTableDialog(Context context,
                                TableInformationDTO tableDTO,
                                String id) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.table_information_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView tableInfoImg = dialog.findViewById(R.id.table_info_img);
        TextView tableInfoText = dialog.findViewById(R.id.table_info_text);
        TextView tableInfoStatement = dialog.findViewById(R.id.table_info_statement);
        TextView tableInfoGender = dialog.findViewById(R.id.table_info_gender);
        TextView tableInfoMember = dialog.findViewById(R.id.table_info_member);
        TextView tableInfoClose = dialog.findViewById(R.id.table_info_close);

        if (tableDTO.getResult().equals("notExist")) {
            MakeQR qr = new MakeQR();
            tableInfoImg.setImageBitmap(qr.clientQR(id));
            tableInfoImg.setClickable(false);
            tableInfoText.setVisibility(View.INVISIBLE);
            tableInfoStatement.setText("사진과 정보를 입력하시려면 다음 큐알로 입장해주세요 :)");
            tableInfoGender.setVisibility(View.GONE);
            tableInfoMember.setVisibility(View.GONE);

        } else {
            String imageUrl = BuildConfig.SERVER_IP + "/Profile/" + tableDTO.getImageUrl();
            Glide.with(tableInfoImg.getContext())
                    .load(imageUrl)
                    .into(tableInfoImg);

            tableInfoText.setText("다시 등록하시려면 \n프로필 사진을 터치해주세요!");

            tableInfoStatement.setText(tableDTO.getStatement());
            tableInfoGender.setText(tableDTO.getGender());
            tableInfoMember.setText(tableDTO.getUserCount());
        }

        tableInfoClose.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    public Dialog otherTableDialog(Context context, TableInformationDTO tableDTO, boolean ticket){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.table_information_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView tableInfoImg = dialog.findViewById(R.id.table_info_img);
        TextView tableInfoText = dialog.findViewById(R.id.table_info_text);
        TextView tableInfoStatement = dialog.findViewById(R.id.table_info_statement);
        TextView tableInfoGender = dialog.findViewById(R.id.table_info_gender);
        TextView tableInfoMember = dialog.findViewById(R.id.table_info_member);
        TextView tableInfoClose = dialog.findViewById(R.id.table_info_close);

        if(tableDTO.getResult().equals("notExist")){
            tableInfoText.setVisibility(View.INVISIBLE);
            tableInfoStatement.setText("정보를 입력하지 않은 테이블입니다.");
            tableInfoGender.setVisibility(View.INVISIBLE);
            tableInfoMember.setVisibility(View.INVISIBLE);
        }else{
            String imageUrl = BuildConfig.SERVER_IP + "/Profile/" + tableDTO.getImageUrl();

            tableInfoStatement.setText(tableDTO.getStatement());
            tableInfoGender.setText(tableDTO.getGender());
            tableInfoMember.setText(tableDTO.getUserCount());

            if(ticket){
                Glide.with(tableInfoImg.getContext())
                        .load(imageUrl)
                        .into(tableInfoImg);
            }else{
                Glide.with(tableInfoImg.getContext())
                        .load(imageUrl)
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                        .into(tableInfoImg);

                tableInfoImg.setOnClickListener(view ->{
                    /**
                     * 티켓이 없으면 구매할거냐고 물어보는 팝업이 하나 더 생기고, 티켓이 있으면 사용할거냐고 물어보는 팝업이 하나 더 띄워진다.
                     */
//                    Intent intent = new Intent(Table.this, PopUpProfile.class);
//                    intent.putExtra("title", "프로필 조회권 구매");
//                    intent.putExtra("body", "프로필 조회권을 구매하시겠습니까?\n** 프로필 조회권 2000원");
//
//                    intent.putExtra("myData", myData);
//                    intent.putExtra("clickTable", clickTable);
//                    intent.putExtra("chattingData", chattingDataHashMap);
//                    intent.putExtra("ticketData", ticketDataHashMap);
//                    intent.putExtra("tableList", tableList);
//
//                    startActivity(intent);
                });
            }
        }

        tableInfoClose.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    public Dialog adminTableInformationDialog(Context context, AdminTableDTO tableInfo) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.table_information_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView tableInfoImg = dialog.findViewById(R.id.table_info_img);
        TextView tableInfoText = dialog.findViewById(R.id.table_info_text);
        TextView tableInfoStatement = dialog.findViewById(R.id.table_info_statement);
        TextView tableInfoGender = dialog.findViewById(R.id.table_info_gender);
        TextView tableInfoMember = dialog.findViewById(R.id.table_info_member);
        TextView tableInfoClose = dialog.findViewById(R.id.table_info_close);

        tableInfoText.setVisibility(View.INVISIBLE);

        String imageUrl = BuildConfig.SERVER_IP + "Profile/" + tableInfo.getImageUrl();
        Glide.with(tableInfoImg.getContext()).load(imageUrl).into(tableInfoImg);

        tableInfoImg.setClickable(false);
        tableInfoStatement.setText(tableInfo.getStatement());
        tableInfoGender.setText(tableInfo.getGender());
        tableInfoMember.setText(tableInfo.getGuestNumber() + "명");

        tableInfoClose.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    private CircularProgressDrawable getProgress(Context context) {
        CircularProgressDrawable progressDrawable = new CircularProgressDrawable(context);

        progressDrawable.setStrokeWidth(5f);
        progressDrawable.setCenterRadius(30f);
        progressDrawable.setBackgroundColor(context.getColor(R.color.gray));
        progressDrawable.start();

        return progressDrawable;
    }

    public Dialog giftSelectDialog(Context context){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.send_gift_select_dialog);

        RecyclerView sendGiftRecyclerview = dialog.findViewById(R.id.send_gift_select_recyclerview);
        TextView sendGiftCancel = dialog.findViewById(R.id.send_gift_select_cancel);

        sendGiftCancel.setOnClickListener(view -> dialog.dismiss());

        sendGiftRecyclerview.setLayoutManager(new LinearLayoutManager
                (context, RecyclerView.HORIZONTAL, false));

        MenuAdapter menuAdapter = new MenuAdapter();
        ArrayList<MenuList> menuLists = new ArrayList<>();

        sendGiftRecyclerview.setAdapter(menuAdapter);
        sendGiftRecyclerview.addItemDecoration(new menu_recyclerview_deco(context));
        menuAdapter.setAdapterItem(menuLists);


        DBHelper dbHelper = new DBHelper(context);
        menuLists = dbHelper.getTableData(menuLists);
        Log.d(TAG, "menuLists size: " + menuLists.size());

        menuAdapter.setAdapterItem(menuLists);

        return dialog;
    }


}
