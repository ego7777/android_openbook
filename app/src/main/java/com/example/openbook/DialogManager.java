package com.example.openbook;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.openbook.Adapter.AdminPopUpAdapter;
import com.example.openbook.Adapter.MenuAdapter;
import com.example.openbook.Category.CartCategory;
import com.example.openbook.Category.MenuCategory;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.Data.CartList;
import com.example.openbook.Data.MenuList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.Deco.menu_recyclerview_deco;
import com.example.openbook.FCM.SendNotification;
import com.example.openbook.QRcode.MakeQR;
import com.example.openbook.retrofit.AdminTableDTO;
import com.example.openbook.retrofit.SalesItemDTO;
import com.example.openbook.retrofit.TableInformationDTO;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jp.wasabeef.glide.transformations.BlurTransformation;


public class DialogManager {

    String TAG = "DialogManagerTAG";

    Gson gson;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    SendNotification sendNotification;

    ManageOrderItems manageOrderItems;

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
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = (int) (displayMetrics.widthPixels * 0.7);  // 화면 너비의 70%로 설정
        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView tableInfoTitle = dialog.findViewById(R.id.table_info_title);
        ImageView tableInfoImg = dialog.findViewById(R.id.table_info_img);
        TextView tableInfoText = dialog.findViewById(R.id.table_info_text);
        TextView tableInfoStatement = dialog.findViewById(R.id.table_info_statement);
        TextView tableInfoGender = dialog.findViewById(R.id.table_info_gender);
        TextView tableInfoMember = dialog.findViewById(R.id.table_info_member);
        TextView tableInfoClose = dialog.findViewById(R.id.table_info_close);

        tableInfoTitle.setText(id);

        if (tableDTO.getResult().equals("failed")) {
            MakeQR qr = new MakeQR();
            tableInfoImg.setImageBitmap(qr.clientQR(id));
            tableInfoImg.setClickable(false);
            tableInfoText.setVisibility(View.INVISIBLE);
            tableInfoStatement.setText("사진과 정보를 입력하시려면 다음 큐알로 입장해주세요 :)");
            tableInfoGender.setVisibility(View.INVISIBLE);
            tableInfoMember.setVisibility(View.INVISIBLE);

        } else {
            String imageUrl = BuildConfig.SERVER_IP + "/Profile/" + tableDTO.getImageUrl();
            Glide.with(context).clear(tableInfoImg);
            Glide.with(tableInfoImg.getContext())
                    .load(imageUrl)
                    .override(Target.SIZE_ORIGINAL)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(tableInfoImg);

            tableInfoText.setText("다시 등록하시려면 \n프로필 사진을 터치해주세요!");

            tableInfoStatement.setText(tableDTO.getStatement());
            tableInfoGender.setText(tableDTO.getGender());
            tableInfoMember.setText(tableDTO.getGuestNumber() + "명");

            tableInfoImg.setOnClickListener(view -> {
                tableInfoImg.setImageBitmap(new MakeQR().clientQR(id));
                tableInfoText.setVisibility(View.GONE);
            });

        }

        tableInfoClose.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    public Dialog otherTableDialog(Context context, String id, String table, TableInformationDTO tableDTO, boolean ticket) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.table_information_dialog);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = (int) (displayMetrics.widthPixels * 0.7);  // 화면 너비의 70%로 설정

        dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView tableTitle = dialog.findViewById(R.id.table_info_title);
        ImageView tableInfoImg = dialog.findViewById(R.id.table_info_img);
        TextView tableInfoText = dialog.findViewById(R.id.table_info_text);
        TextView tableInfoStatement = dialog.findViewById(R.id.table_info_statement);
        TextView tableInfoGender = dialog.findViewById(R.id.table_info_gender);
        TextView tableInfoMember = dialog.findViewById(R.id.table_info_member);
        TextView tableInfoClose = dialog.findViewById(R.id.table_info_close);

        tableTitle.setText(table);

        if (tableDTO.getResult().equals("failed")) {
            tableInfoText.setVisibility(View.INVISIBLE);
            tableInfoStatement.setText("정보를 입력하지 않은 테이블입니다.");
            tableInfoGender.setVisibility(View.INVISIBLE);
            tableInfoMember.setVisibility(View.INVISIBLE);
        } else {
            String imageUrl = BuildConfig.SERVER_IP + "/Profile/" + tableDTO.getImageUrl();

            tableInfoStatement.setText(tableDTO.getStatement());
            tableInfoGender.setText(tableDTO.getGender());
            tableInfoMember.setText(tableDTO.getGuestNumber() + "명");

            if (ticket) {
                tableInfoText.setVisibility(View.GONE);

                Glide.with(context).clear(tableInfoImg);
                Glide.with(context)
                        .load(imageUrl)
                        .override(Target.SIZE_ORIGINAL)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(tableInfoImg);
            } else {
                Glide.with(context).clear(tableInfoImg);
                Glide.with(context)
                        .load(imageUrl)
                        .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                        .into(tableInfoImg);

                tableInfoImg.setOnClickListener(view ->{
                    dialog.dismiss();
                    buyProfileTicketDialog(context, id, table).show();
                });

            }
        }

        tableInfoClose.setOnClickListener(view -> dialog.dismiss());

        return dialog;
    }

    public Dialog buyProfileTicketDialog(Context context, String from, String to) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup);

        TextView title = dialog.findViewById(R.id.popup_title);
        title.setText("프로필 티켓 구매");

        TextView body = dialog.findViewById(R.id.popup_body);
        body.setText("프로필 티켓을 구매하시겠습니까?\n가격: 2000원");

        Button yesButton = dialog.findViewById(R.id.popup_button_yes);
        yesButton.setText("확인");

        yesButton.setOnClickListener(view -> {
            //노티피케이션으로 티켓 구매 알리고, 나한테도 주문 내역에 들어가야함!!

            gson = new Gson();
            JsonObject menuItem = new JsonObject();
            menuItem.addProperty("menuName", "프로필 티켓");
            menuItem.addProperty("menuPrice", 2000);
            menuItem.addProperty("menuQuantity", 1);
            menuItem.addProperty("menuCategory",CartCategory.MENU.getValue());

            JsonArray menuItems = new JsonArray();
            menuItems.add(menuItem);

            Log.d(TAG, "buyProfileTicketDialog menuItems: " + gson.toJson(menuItems));

            //admin에게 주문한다
            Map<String, String> request = new HashMap<>();
            request.put("request", "Order");
            request.put("tableName", from);
            request.put("orderItemName", "프로필 티켓 1개");
            request.put("items", gson.toJson(menuItems));
            request.put("totalPrice", "2000");

            sendNotification = new SendNotification();
            sendNotification.sendMenu(request, result -> {
                if (result.equals("failed")) {
                    Toast.makeText(context, context.getResources().getString(R.string.menuOrderError), Toast.LENGTH_SHORT).show();
                }else{
                    //성공하면 클라이언트 주문 내역에 저장
                    manageOrderItems = new ManageOrderItems();
                    manageOrderItems.orderSharedPreference(context);

                    //프로필 티켓도 업데이트
                    sharedPreferences = context.getSharedPreferences("CustomerData", Context.MODE_PRIVATE);
                    editor = sharedPreferences.edit();

                    String profileTicket = sharedPreferences.getString("profileTicket", null);
                    HashMap<String, Boolean> profileTicketMap = gson.fromJson(profileTicket, HashMap.class);

                    if(profileTicketMap == null) {
                        profileTicketMap = new HashMap<>();

                    }
                    profileTicketMap.put(to, true);

                    editor.putString("profileTicket", gson.toJson(profileTicketMap));
                    editor.commit();
                }
                dialog.dismiss();
            });

        });

        Button noButton = dialog.findViewById(R.id.popup_button_no);
        noButton.setText("취소");

        noButton.setOnClickListener(view -> dialog.dismiss());

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

    public Dialog giftSelectDialog(Context context, String from, String to) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.send_gift_select_dialog);

        RecyclerView sendGiftRecyclerview = dialog.findViewById(R.id.send_gift_select_recyclerview);
        TextView sendGiftCancel = dialog.findViewById(R.id.send_gift_select_cancel);

        sendGiftCancel.setOnClickListener(view -> dialog.dismiss());

        sendGiftRecyclerview.setLayoutManager(new LinearLayoutManager
                (context, RecyclerView.HORIZONTAL, false));

        MenuAdapter menuAdapter = new MenuAdapter();

        sendGiftRecyclerview.setAdapter(menuAdapter);
        sendGiftRecyclerview.addItemDecoration(new menu_recyclerview_deco(context));

        DBHelper dbHelper = new DBHelper(context);
        final ArrayList<MenuList> menuLists = dbHelper.getTableData(new ArrayList());
        menuAdapter.setAdapterItem(menuLists);

        menuAdapter.setOnItemClickListener((view, name, price, position) -> {
            dialog.dismiss();

            MenuList menuItem = menuLists.get(position);

            giftSendDialog(context, to, from, menuItem).show();
        });

        return dialog;
    }

    public Dialog giftSendDialog(Context context, String to, String from, MenuList menuItem) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.send_gift_quantity_dialog);

        TextView menuName = dialog.findViewById(R.id.send_gift_quantity_menuName);
        TextView menuQuantity = dialog.findViewById(R.id.send_gift_quantity_menuQuantity);
        TextView menuPrice = dialog.findViewById(R.id.send_gift_quantity_price);
        Button sendGiftButton = dialog.findViewById(R.id.send_gift_button);
        TextView plus = dialog.findViewById(R.id.send_gift_quantity_plus);
        Button minus = dialog.findViewById(R.id.send_gift_quantity_minus);
        Button cancel = dialog.findViewById(R.id.send_gift_quantity_cancel);

        cancel.setOnClickListener(v -> dialog.dismiss());

        menuName.setText(menuItem.getMenuName());
        menuPrice.setText(String.valueOf(menuItem.getMenuPrice()));
        menuQuantity.setText(String.valueOf(1));

        plus.setOnClickListener(v -> {
            int oldQuantity = Integer.parseInt(menuQuantity.getText().toString());
            int newQuantity = oldQuantity + 1;
            menuQuantity.setText(String.valueOf(newQuantity));

            int totalPrice = newQuantity * menuItem.getMenuPrice();
            menuPrice.setText(String.valueOf(totalPrice));

        });

        minus.setOnClickListener(v -> {
            int oldQuantity = Integer.parseInt(menuQuantity.getText().toString());
            if (oldQuantity > 1) {
                int newQuantity = oldQuantity - 1;
                menuQuantity.setText(String.valueOf(newQuantity));

                int totalPrice = newQuantity * menuItem.getMenuPrice();
                menuPrice.setText(String.valueOf(totalPrice));
            } else {
                Toast.makeText(context, "선물 가능한 최소 개수는 1개 입니다.", Toast.LENGTH_SHORT).show();
            }

        });

        sendGiftButton.setOnClickListener(v -> {
            String quantity = menuQuantity.getText().toString();
            gson = new Gson();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("menuName", menuItem.getMenuName());
            jsonObject.addProperty("menuPrice", menuItem.getMenuPrice());
            jsonObject.addProperty("menuType", menuItem.getMenuType());
            jsonObject.addProperty("imageUrl", menuItem.getUrl());
            jsonObject.addProperty("menuType", menuItem.getMenuType());

            JsonArray jsonArray = new JsonArray();
            jsonArray.add(jsonObject);

            sendNotification = new SendNotification();
            sendNotification.sendGift(to, from, jsonArray.toString(), quantity);
            dialog.dismiss();
        });

        return dialog;

    }

    public Dialog giftReceiveDialog(Context context, String to, String from, String menuItem, String count) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.popup);

        TextView title = dialog.findViewById(R.id.popup_title);
        ImageView image = dialog.findViewById(R.id.popup_image);
        TextView body = dialog.findViewById(R.id.popup_body);
        Button yesButton = dialog.findViewById(R.id.popup_button_yes);
        Button noButton = dialog.findViewById(R.id.popup_button_no);

        title.setText("선물 도착");

        gson = new Gson();
        JsonArray jsonArray = gson.fromJson(menuItem, JsonArray.class);
        JsonObject item = jsonArray.get(0).getAsJsonObject();
        String menuName =  item.get("menuName").getAsString();
        int menuPrice =  item.get("menuPrice").getAsInt();
        String url = item.get("imageUrl").getAsString();

        Glide.with(context).load(url).into(image);

        String message;

        if (item.get("menuType").getAsInt() == MenuCategory.DRINK.getValue()) {
            message = from + " 에서 " + menuName + " " + count + "병을 선물하였습니다.";
        } else {
            message = from + " 에서 " + menuName + " " + count + "개를 선물하였습니다.";
        }
        body.setText(message);

       manageOrderItems = new ManageOrderItems();

        String fromMenuName = menuName + "(" + to + ")";
        String toMenuName = menuName + "(" + from + ")";

        String fromMenuItem = manageOrderItems.getMenuItem(fromMenuName, Integer.parseInt(count), menuPrice);
        String toMenuItem = manageOrderItems.getMenuItem(toMenuName, Integer.parseInt(count), 0);

        sendNotification = new SendNotification();

        yesButton.setOnClickListener(view -> {
            //수락했다고 from에게 알려주는 fcm을 호출한다 -> 주문내역에 추가한다

            sendNotification.notifyIsGiftAccept(from, to, menuItem, true);

            //admin에게 주문한다
            Map<String, String> request = new HashMap<>();
            request.put("request", "GiftMenuOrder");
            request.put("tableName", to);
            request.put("fromTable", from);
            request.put("fromMenuItem", fromMenuItem);
            request.put("toMenuItem", toMenuItem);

            sendNotification.sendMenu(request, result -> {
                if (result.equals("failed")) {
                    Toast.makeText(context, context.getResources().getString(R.string.menuOrderError), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });

        });

        noButton.setOnClickListener(view -> {
            Log.d(TAG, "giftReceiveDialog: no");
            sendNotification.notifyIsGiftAccept(from, to, menuItem, false);
            dialog.dismiss();
        });

        return dialog;
    }

}
