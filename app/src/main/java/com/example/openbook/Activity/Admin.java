package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.openbook.Adapter.AdminTableAdapter;
import com.example.openbook.Data.AdminData;
import com.example.openbook.BuildConfig;
import com.example.openbook.Chatting.DBHelper;
import com.example.openbook.PaymentCategory;
import com.example.openbook.retrofit.AdminTableDTO;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.OrderList;
import com.example.openbook.DialogManager;
import com.example.openbook.FCM.FCM;
import com.example.openbook.ImageLoadTask;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.TableListDTO;
import com.example.openbook.SaveOrderDeleteData;
import com.example.openbook.R;
import com.example.openbook.TableQuantity;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.lang.reflect.Type;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class Admin extends AppCompatActivity {

    String TAG = "AdminTAG";
    AdminTableAdapter adapter;

    TextView appbarAdminSales, appbarAdminAddMenu, appbarAdminModifyTable;
    TextView adminSidebarMenu, adminSidebarInfo, adminSidebarPay;

    String gender, guestNumber, tableName, tableStatement, menuName;
    int totalPrice, tableIdentifier;

    DBHelper dbHelper;
    int version = 2;

    SharedPreferences sharedPreference;
    SharedPreferences.Editor editor;

    String afterPaymentList;

    //다이얼로그
    ImageView tableInfoImg;
    TextView tableInfoStatement, tableInfoText, tableInfoGender, tableInfoMember;
    Button tableInfoClose;
    AdminData adminData;
    RetrofitService service;
    ArrayList<AdminTableList> adminTableLists;
    Gson gson;
    String tableRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_activity);

        adminData = getIntent().getParcelableExtra("adminData");
        Log.d(TAG, "adminData: " + adminData);

        sharedPreference = getSharedPreferences("AdminInfo", MODE_PRIVATE);
        editor = sharedPreference.edit();
        gson = new Gson();

        if (adminData == null || adminData.getId() == null) {
            String id = sharedPreference.getString("id", null);
            adminData = new AdminData(id, null, false);
            Log.d(TAG, "id null admin: " + adminData.getId());
        }

        RecyclerView tableGrid = findViewById(R.id.admin_grid);
        adapter = new AdminTableAdapter();
        tableGrid.setLayoutManager(new GridLayoutManager(this, 3));
        tableGrid.setAdapter(adapter);


        if (adminData.getAdminTableLists() != null) {
            Log.d(TAG, "adminTableList size : " + adminData.getAdminTableLists().size());
            adapter.setAdapterItem(adminData.getAdminTableLists());

        } else {
            adminData.setAdminTableLists(loadAdminTableList());
            adapter.setAdapterItem(adminData.getAdminTableLists());
        }

        afterPaymentList = getIntent().getStringExtra("orderList");
        Log.d(TAG, "orderList: " + afterPaymentList);


//        sharedPreference = getSharedPreferences("oldMenuSummary", MODE_PRIVATE);
//        editor = sharedPreference.edit();

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);


//            int table = tableQuantity.getTableQuantity();
//            Log.d(TAG, "tableQuantity : " + table);

//            for (int i = 1; i < table + 1; i++) {
//
//                String summary = sharedPreference.getString("table" + i + "menu", null);
//                Log.d(TAG, "summary: " + summary);
//
//                int price = sharedPreference.getInt("table" + i + "price", 0);
//                Log.d(TAG, "price: " + price);
//
//                String gender = sharedPreference.getString("table" + i + "gender", null);
//                Log.d(TAG, "gender: " + gender);
//
//                String guestNumber = sharedPreference.getString("table" + i + "guestNumber", null);
//                Log.d(TAG, "guestNumber: " + guestNumber);
//
//                int viewType = sharedPreference.getInt("table" + i + "viewType", 0);
//                Log.d(TAG, "viewType: " + viewType);
//
//                int identifier = sharedPreference.getInt("table" + i + "tableIdentifier", 0);
//                Log.d(TAG, "identifier: " + identifier);
//
//
//                if (summary != null) {
//                    adminTableList.add(new AdminTableList("table" + i,
//                            summary, String.valueOf(price), gender, guestNumber, viewType, identifier));
//
//                } else {
//                    adminTableList.add(new AdminTableList("table" + i,
//                            null,
//                            null,
//                            null,
//                            null,0, 0));
//                }
//
//            }// for문 끝


        /**
         * 로그인을 성공하면 id, token을 firebase realtime db에 저장
         */
        if (!adminData.isFcmExist()) {
            boolean fcmExist = sharedPreference.getBoolean("isFcmExist", false);

            if(!fcmExist){
                FCM fcm = new FCM();
                fcm.getToken(adminData.getId().hashCode());
                adminData.setFcmExist(true);

                editor.putBoolean("isFcmExist", true);
                editor.commit();
            }
        }


        TextView appbarAdminId = findViewById(R.id.appbar_admin_id);
        appbarAdminId.setText(adminData.getId());

        appbarAdminSales = findViewById(R.id.appbar_admin_sales);
        appbarAdminAddMenu = findViewById(R.id.appbar_admin_addMenu);
        appbarAdminModifyTable = findViewById(R.id.appbar_admin_modifyTable);

        adminSidebarMenu = findViewById(R.id.admin_sidebar_menu);
        adminSidebarInfo = findViewById(R.id.admin_sidebar_info);
        adminSidebarPay = findViewById(R.id.admin_sidebar_pay);


        dbHelper = new DBHelper(Admin.this, version);
        version++;


    }


    @Override
    protected void onStart() {
        super.onStart();

        tableRequest= getIntent().getStringExtra("tableRequest");
        Log.d(TAG, "onStart tableRequest: " + tableRequest);

        if(tableRequest != null){
            try {
                JSONObject requestJson = new JSONObject(tableRequest);
                String request = (String) requestJson.get("request");
                String tempTableName = requestJson.getString("tableName").replace("table", "");
                int tableNumber = Integer.parseInt(tempTableName) - 1;

                updateTable(request, tableNumber);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }





        gender = null;
        gender = getIntent().getStringExtra("gender");
        editor.putString(tableName + "gender", gender);

        guestNumber = null;
        guestNumber = getIntent().getStringExtra("guestNumber");
        editor.putString(tableName + "guestNumber", guestNumber);


        tableName = null;
        tableName = getIntent().getStringExtra("tableName");

        tableStatement = getIntent().getStringExtra("tableStatement");
        editor.putString(tableName + "tableStatement", tableStatement);

        tableIdentifier = getIntent().getIntExtra("tableIdentifier", 0);
        Log.d(TAG, "Intent tableIdentifier: " + tableIdentifier);
        editor.putInt(tableName + "tableIdentifier", tableIdentifier);
        editor.commit();


        if (menuName != null) {
            int pastCount = Integer.parseInt(String.valueOf(menuName.indexOf(menuName.length())));
            Log.d(TAG, "pastCount: " + pastCount);

        }
        menuName = getIntent().getStringExtra("menuName");

        if (afterPaymentList != null) {
            SaveOrderDeleteData orderSaveDeleteData = new SaveOrderDeleteData();
            //저장하고,

            try {
                boolean success = orderSaveDeleteData.orderSave(afterPaymentList);

                Log.d(TAG, "success: " + success);

                if (success == true) {
//                    deleteLocalData();
                    Log.d(TAG, "deleteData: ");

                    orderSaveDeleteData.deleteServerData(tableName); // 서버 데이터
                    Log.d(TAG, "deleteServerData: ");
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onResume() {
        super.onResume();


//        if (gender != null) {
//            Log.d(TAG, "gender not null: " + gender);
//            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
//            adminTableList.get(tableNameInt).setAdminTableGender(gender);
//            adminTableList.get(tableNameInt).setAdminTableGuestNumber(guestNumber);
//            // 이것도 쉐어드에 저장을 하고, 삭제하는 것으로..!
//
//        } else if (tableStatement != null) {
//            Log.d(TAG, "tableStatement not null: " + tableStatement);
//
//            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
//            Log.d(TAG, "tableNameInt: " + tableNameInt);
//            adminTableList.get(tableNameInt).setAdminTableStatement(tableStatement);
//            adminTableList.get(tableNameInt).setViewType(1);
//
//            editor.putInt(tableName + "viewType", 1);
//            editor.commit();
//
//            adminTableList.get(tableNameInt).setAdminTableIdentifier(tableIdentifier);
//
//
//        } else if (menuName != null) {
//            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
//            adminTableList.get(tableNameInt).setAdminTableMenu(menuName);
//            Log.d(TAG, "setAdminTableMenu: " + menuName);
//            adminTableList.get(tableNameInt).setAdminTablePrice(String.valueOf(totalPrice));
//            Log.d(TAG, "setAdminTablePrice: " + totalPrice);
//
//        }


        appbarAdminSales.setOnClickListener(v -> {
            //매출 액티비티가 나온다
            startActivityClass(AdminSales.class);
        });


        appbarAdminAddMenu.setOnClickListener(view -> {
            // 메뉴 이름, 가격, 이미지를 등록하면 서버로 들어가서 menuList Db에 등록된다
            startActivityClass(AdminModifyMenu.class);
        });

        appbarAdminModifyTable.setOnClickListener(view -> {
            startActivityClass(AdminModifyTableQuantity.class);
        });


        DialogManager dialogManager = new DialogManager();


        adapter.setOnItemClickListener((view, position) -> {

            Log.d(TAG, "onItemClick: " + position);

//            adminSidebarMenu.setOnClickListener(v -> {
//                if (adminTableList.get(position).getAdminTableMenu() != null ||
//                        adminTableList.get(position).getAdminTableStatement() != null) {
//                    showReceiptDialog(position);
//                } else {
//                    dialogManager.noButtonDialog(Admin.this, "빈 좌석이거나 아직 주문하지 않은 좌석입니다.");
//
//                }


//                        if (adminTableList.get(position).getAdminTableMenu() == null ) {
//
//                            dialogCustom.HandlerAlertDialog(Admin.this, "빈 좌석이거나 아직 주문하지 않은 좌석입니다.");
//
//                        } else if (adminTableList.get(position).getAdminTableStatement() == null) {
//                            dialogCustom.HandlerAlertDialog(Admin.this, "빈 좌석이거나 아직 주문하지 않은 좌석입니다.");
//                            Log.d(TAG, "선불: ");
//                        }
//                        else {
//                            showReceiptDialog(position);
//
//                        }
//            });

            adminSidebarInfo.setOnClickListener(v -> {
                Dialog dlg = new Dialog(Admin.this, R.style.RadiusDialogStyle);
                dlg.setContentView(R.layout.table_information_dialog);

                dlg.show();

                tableInfoImg = dlg.findViewById(R.id.table_info_img);
                tableInfoText = dlg.findViewById(R.id.table_info_text);
                tableInfoStatement = dlg.findViewById(R.id.table_info_statement);
                tableInfoGender = dlg.findViewById(R.id.table_info_gender);
                tableInfoMember = dlg.findViewById(R.id.table_info_member);
                tableInfoClose = dlg.findViewById(R.id.table_info_close);


                requestTableInfo(position + 1);


                tableInfoClose.setOnClickListener(v1 -> dlg.dismiss());
            });

            adminSidebarPay.setOnClickListener(v -> {

                // sqlite에서 정보 날리고, 서버로 데이터 보낼겨
                ArrayList<OrderList> orderLists = new ArrayList<>();
                orderLists = dbHelper.fetchMenuDetails(tableName, orderLists);

//                if (adminTableList.get(position).getAdminTablePrice() != null) {
//                    totalPrice = Integer.parseInt(adminTableList.get(position).getAdminTablePrice());
//                    // 여기에 카카오 페이를 붙이겠읍니다.....
//                    Intent intent = new Intent(Admin.this, KakaoPay.class);
//                    intent.putExtra("menuName", getOrderMenuName(orderLists));
//                    intent.putExtra("menuPrice", totalPrice);
//                    intent.putExtra("tableName", tableName);
//                    intent.putExtra("jsonOrderList", getJson(orderLists));
//                    intent.putExtra("paymentStyle", "after");
//                    intent.putExtra("get_id", id);
//                    startActivity(intent);
//                } else {
//                    dialogManager.noButtonDialog(Admin.this, "빈 좌석이거나 아직 주문하지 않은 좌석입니다.");
//                }


            });

        });

    }

//    public void showReceiptDialog(int position) {
//        Dialog dialog = new Dialog(Admin.this);
//        dialog.setContentView(R.layout.admin_receipt_dialog);
//
//        AdminPopUpAdapter adminReceiptAdapter = new AdminPopUpAdapter();
//
//        ArrayList<OrderList> adminOrderList = new ArrayList<>();
//
//        if (adminTableList.get(position).getViewType() == 0) {
//
//            adminOrderList = dbHelper.fetchMenuDetails(tableName, adminOrderList);
//
//        } else {
//            OrderData orderData = new OrderData();
//
//            String data = orderData.getOrderData(tableName, tableIdentifier);
//            Log.d(TAG, "showReceiptDialog data: " + data);
//
//            adminOrderList = orderData.setArrayList(tableName, data, adminOrderList);
//        }
//
//
//        TextView adminReceiptCancel = dialog.findViewById(R.id.admin_receipt_cancel);
//        TextView adminReceiptTotalPrice = dialog.findViewById(R.id.admin_receipt_totalPrice);
//        RecyclerView adminReceiptRecyclerView = dialog.findViewById(R.id.admin_receipt_recyclerView);
//
//        adminReceiptRecyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adminReceiptRecyclerView.setAdapter(adminReceiptAdapter);
//        adminReceiptAdapter.setAdapterItem(adminOrderList);
//
//        int getPrice = Integer.parseInt(adminTableList.get(position).getAdminTablePrice());
//
//        String totalPrice = addCommasToNumber(getPrice);
//
//        adminReceiptTotalPrice.setText(totalPrice);
//
//        dialog.show();
//
//        adminReceiptCancel.setOnClickListener(view -> {
//            dialog.dismiss();
//        });
//    }


    public void startActivityClass(Class activity) {
        Intent intent = new Intent(Admin.this, activity);
        intent.putExtra("adminData", adminData);
        startActivity(intent);
    }

    public String addCommasToNumber(int number) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(number) + "원";
    }

    public String getOrderMenuName(ArrayList<OrderList> orderLists) {

        int menuQuantity = orderLists.size();

        String menuName;

        if (menuQuantity == 1) {
            menuName = orderLists.get(0).getMenu();
        } else {
            menuName = orderLists.get(0).getMenu() + " 외" + Integer.toString(menuQuantity - 1);
        }
        Log.d(TAG, "menuName :" + menuName);

        return menuName;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getJson(ArrayList<OrderList> list) {
        JSONObject jsonObject = new JSONObject();
        JSONArray menujArray = new JSONArray();//배열이 필요할때

        try {
            for (int i = 0; i < list.size(); i++)//배열
            {
                JSONObject sObject = new JSONObject();//배열 내에 들어갈 json
                sObject.put("menu", list.get(i).getMenu());
                sObject.put("quantity", list.get(i).getQuantity());
                sObject.put("price", list.get(i).getPrice());
                menujArray.put(sObject);
            }

            jsonObject.put("table", tableName);
            jsonObject.put("item", menujArray);

            Log.d(TAG, "getJson: " + jsonObject.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


//    public void deleteLocalData() {
//
//        runOnUiThread(() -> {
//
//            //서버에서도 데이터를 이동하는 것이 좋겠다...! 채팅 데이터를 서버로 보내야함 여기서 + 얘기만(gender)
//
//            Log.d(TAG, "run: " + tableName);
//            dbHelper.deleteTableData(tableName, "adminTableList", "tableName"); //sqlite에서 지우고
//            editor.remove(tableName + "price"); //s.p에서도 지움
//            editor.remove(tableName + "menu");
//            editor.remove(tableName + "gender");
//            editor.remove(tableName + "guestName");
//            editor.commit();
//
//            int tableNameInt = Integer.parseInt(tableName.replace("table", "")) - 1;
//            Log.d(TAG, "tableNameInt: " + tableNameInt);
//            adminTableList.get(tableNameInt).setAdminTableMenu(null);
//            adminTableList.get(tableNameInt).setAdminTablePrice(null);
//            adminTableList.get(tableNameInt).setAdminTableGender(null);
//            adminTableList.get(tableNameInt).setAdminTableGuestNumber(null);
//            adapter.notifyItemChanged(tableNameInt);
//
//        });
//    }

    private void requestTableInfo(int clickTable) {

        Call<AdminTableDTO> call = service.requestTableInfo("table" + clickTable);
        call.enqueue(new Callback<AdminTableDTO>() {
            @Override
            public void onResponse(Call<AdminTableDTO> call, Response<AdminTableDTO> response) {
                Log.d(TAG, "onResponse tableInfoCheck: " + response.body().getResult());
                if (response.isSuccessful()) {
                    switch (response.body().getResult()) {
                        case "success":
                            tableInfoText.setVisibility(View.INVISIBLE);

                            String imageUrl = BuildConfig.SERVER_IP + "Profile/" + response.body().getImageUrl();
                            ImageLoadTask task = new ImageLoadTask(Admin.this, true, imageUrl, tableInfoImg);
                            task.execute();

                            tableInfoImg.setClickable(false);
                            tableInfoStatement.setText(response.body().getStatement());
                            tableInfoGender.setText(response.body().getGender());
                            tableInfoMember.setText(response.body().getGuestNumber() + "명");
                            break;

                        case "failed":
                            tableInfoText.setVisibility(View.INVISIBLE);
                            tableInfoStatement.setText("정보를 입력하지 않은 테이블입니다.");
                            tableInfoGender.setVisibility(View.GONE);
                            tableInfoMember.setVisibility(View.GONE);
                            break;

                        default:
                            Log.d(TAG, "onResponse tableInfoCheck default");
                    }
                } else {
                    Log.d(TAG, "onResponse tableInfoCheck is not successful");
                }
            }

            @Override
            public void onFailure(Call<AdminTableDTO> call, Throwable t) {
                Log.d(TAG, "onFailure tableInfoCheck :" + t.getMessage());
            }
        });

    }

    public ArrayList<AdminTableList> loadAdminTableList(){
        String table = sharedPreference.getString("adminTableList", null);
        Type type = new TypeToken<ArrayList<AdminTableList>>(){}.getType();
        return gson.fromJson(table, type);

    }

    public void updateTable(String request, int tableNumber){
        switch (request) {
            case "PayNow" :
                adminData.getAdminTableLists().get(tableNumber).setPaymentType(PaymentCategory.NOW.getValue());
                adminData.getAdminTableLists().get(tableNumber).setAdminTableStatement("선불 좌석 이용");
                adapter.setAdapterItem(adminData.getAdminTableLists());

                editor.putString("adminTableList", gson.toJson(adminData.getAdminTableLists()));
                editor.commit();

            case "End" :
                adminData.getAdminTableLists().get(tableNumber).setPaymentType(PaymentCategory.UNSELECTED.getValue());
                adminData.getAdminTableLists().get(tableNumber).setAdminTableStatement(null);
                adminData.getAdminTableLists().get(tableNumber).setAdminTableGender(null);
                adminData.getAdminTableLists().get(tableNumber).setAdminTableMenu(null);
                adminData.getAdminTableLists().get(tableNumber).setAdminTablePrice(null);
                adminData.getAdminTableLists().get(tableNumber).setAdminTableGuestNumber(null);
                adminData.getAdminTableLists().get(tableNumber).setAdminTableIdentifier(0);
                adapter.notifyItemChanged(tableNumber);

                editor.putString("adminTableList", gson.toJson(adminData.getAdminTableLists()));
                editor.commit();
                break;
        }
    }

}
