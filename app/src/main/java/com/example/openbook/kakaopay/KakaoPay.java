package com.example.openbook.kakaopay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Activity.Admin;
import com.example.openbook.Activity.Menu;
import com.example.openbook.BuildConfig;
import com.example.openbook.Data.AdminData;
import com.example.openbook.Data.MyData;
import com.example.openbook.R;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.google.gson.Gson;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class KakaoPay extends AppCompatActivity {

    String TAG = "KakaoPayTAG";

    WebView webView;
    MyWebViewClient myWebViewClient;
    String tid, pgToken, orderItemName, tableName;
    int totalPrice, partnerUserId;
    MyData myData;
    AdminData adminData;
    RetrofitService retrofitService;
    RetrofitManager retrofitManager;
    Gson gson;
    String orderItems;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakaopay);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        adminData = getIntent().getParcelableExtra("adminData");

        tableName = getIntent().getStringExtra("tableName");
        orderItems = getIntent().getStringExtra("orderItems");
        Log.d(TAG, "orderItems: " + orderItems);

        if (myData != null) {
            partnerUserId = myData.getIdentifier();
        } else {
            partnerUserId = tableName.hashCode();
        }

        gson = new Gson();

        orderItemName = getIntent().getStringExtra("orderItemName");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);

        // 웹 뷰 설정
        webView = findViewById(R.id.kakaopay_webview);
        myWebViewClient = new MyWebViewClient();

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(myWebViewClient);

        retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit("https://kapi.kakao.com/");
        retrofitService = retrofit.create(RetrofitService.class);

        sendPaymentRequest();
    }


    public class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            String url = request.getUrl().toString();

            if (url.contains("pg_token=")) {

                String pg_token = url.substring(url.indexOf("pg_token=") + 9);
                pgToken = pg_token;

                url = url.replace("?pg_token=" + pg_token, "");

                sendApprovalRequest();
            }

            if (url.contains("cancel")) {
                finish();
            } else if (url.contains("fail")) {
                finish();
            } else if (url.contains("success")) {
                finish();
            }

            view.loadUrl(url);

            return false;

        }

    }

    public void sendPaymentRequest() {

        HashMap<String, String> paymentRequest = new HashMap<>();
        paymentRequest.put("cid", "TC0ONETIME");
        paymentRequest.put("partner_order_id", "openbook");
        paymentRequest.put("partner_user_id", String.valueOf(partnerUserId));
        paymentRequest.put("item_name", orderItemName);
        paymentRequest.put("quantity", "1");
        paymentRequest.put("total_amount", String.valueOf(totalPrice));
        paymentRequest.put("tax_free_amount", "0");
        paymentRequest.put("approval_url", BuildConfig.SERVER_IP + "kakaopay/success");
        paymentRequest.put("cancel_url", BuildConfig.SERVER_IP + "kakaopay/cancel");
        paymentRequest.put("fail_url", BuildConfig.SERVER_IP + "kakaopay/fail");

        Call<KakaoPayReadyResponseDTO> call = retrofitService.createPaymentRequest(BuildConfig.KAKAOPAY_ADMIN_KEY, paymentRequest);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<KakaoPayReadyResponseDTO> call, @NonNull Response<KakaoPayReadyResponseDTO> response) {
                Log.d(TAG, "onResponse create kakaopay : " + response);
                if (response.isSuccessful()) {
                    tid = response.body().getTid();
                    Log.d(TAG, "onResponse tid: " + tid);
                    String url = response.body().getNextRedirectPcUrl();
                    webView.loadUrl(url);
                } else {
                    Log.d(TAG, "onResponse isNotSuccessful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<KakaoPayReadyResponseDTO> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure create kakaopay : " + t.getMessage());
            }
        });

    }


    public void sendApprovalRequest() {

        HashMap<String, String> approvedRequest = new HashMap<>();
        approvedRequest.put("cid", "TC0ONETIME");
        approvedRequest.put("tid", tid);
        approvedRequest.put("partner_order_id", "openbook");
        approvedRequest.put("partner_user_id", String.valueOf(partnerUserId));
        approvedRequest.put("pg_token", pgToken);

        Call<KakaoPayApproveResponseDTO> call = retrofitService.requestApprovedPayment(BuildConfig.KAKAOPAY_ADMIN_KEY, approvedRequest);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<KakaoPayApproveResponseDTO> call, @NonNull Response<KakaoPayApproveResponseDTO> response) {
                Log.d(TAG, "onResponse kakao approved: " + response);
                if (response.isSuccessful()) {

                    String approvedAt = response.body().getApprovedAt();
                    String paymentMethodType = response.body().getPaymentMethodType();
                    int identifier = Integer.parseInt(response.body().getPartnerUserId());

                    int payment;

                    if (paymentMethodType.equals("MONEY")) {
                        payment = 0;
                    } else {
                        payment = 1;
                    }

                    if (payment == 0 || payment == 1) {
                        saveOrderDetailsOnServer(approvedAt, payment, identifier);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<KakaoPayApproveResponseDTO> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure kakao approved: " + t.getMessage());
            }
        });
    }

    public void saveOrderDetailsOnServer(String approvedAt,
                                         int paymentMethodType,
                                         int identifier) {

        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        retrofitService = retrofit.create(RetrofitService.class);

        Call<SuccessOrNot> call = retrofitService.savePayment(tid,
                approvedAt, totalPrice, identifier, paymentMethodType, orderItems);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse saveOrderDetailsOnServer: " + response.body());
                if (response.isSuccessful()) {
                    switch (response.body().getResult()) {
                        case "success":
                            if (myData == null) {
                                Intent intent = new Intent(KakaoPay.this, Admin.class);
                                //가서 해당 tableName sharedPreference 지우기, 테이블 초기화 하기
                                intent.putExtra("paidTable", tableName);
                                intent.putExtra("tid", tid);
                                startActivity(intent);

                            } else {
                                Intent intent = new Intent(KakaoPay.this, Menu.class);
                                intent.putExtra("myData", myData);
                                intent.putExtra("isPayment", true);
                                startActivity(intent);
                            }
                            break;

                        case "failed":
                            Log.d(TAG, "onResponse saveOrderDetails is failed");
                            if (myData == null) {
                                Intent intent = new Intent(KakaoPay.this, Admin.class);
                                //가서 해당 tableName sharedPreference 지우기, 테이블 초기화 하기
                                intent.putExtra("tableName", tableName);
                                setResult(RESULT_CANCELED, intent);
                                finish();

                            } else {
                                Intent intent = new Intent(KakaoPay.this, Menu.class);
                                intent.putExtra("myData", myData);
                                setResult(RESULT_CANCELED, intent);
                                finish();
                            }
                            break;
                    }
                } else {
                    Log.d(TAG, "onResponse saveOrderDetailsOnServer is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure saveOrderDetailsOnServer: " + t.getMessage());
            }
        });
    }


}
