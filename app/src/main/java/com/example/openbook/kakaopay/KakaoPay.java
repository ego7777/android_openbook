package com.example.openbook.kakaopay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.zxing.common.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class KakaoPay extends AppCompatActivity {

    String TAG = "KakaoPayTAG";

    WebView webView;
    MyWebViewClient myWebViewClient;
    String tid, pgToken;

    String orderItemName;
    int totalPrice;
    MyData myData;
    String tableName, partnerUserId;
    RetrofitService kakaoPayService;
    Gson gson;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kakaopay_activity);

        myData = (MyData) getIntent().getSerializableExtra("myData");

        tableName = getIntent().getStringExtra("tableName");

        if(myData != null){
            partnerUserId = myData.getId();
        }else{
            partnerUserId = tableName;
        }


        gson = new Gson();

        orderItemName = getIntent().getStringExtra("orderItemName");
        totalPrice = getIntent().getIntExtra("totalPrice", 0);

        // 웹 뷰 설정
        webView = findViewById(R.id.kakaopay_webview);
        myWebViewClient = new MyWebViewClient();


        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(myWebViewClient);

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit("https://kapi.kakao.com/");
        kakaoPayService = retrofit.create(RetrofitService.class);

        sendPaymentRequest();

    }


    public class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            String url = request.getUrl().toString();

            Log.d(TAG, "shouldOverrideUrlLoading: url\n" + url);
            if (url != null && url.contains("pg_token=")) {

                String pg_token = url.substring(url.indexOf("pg_token=") + 9);
                pgToken = pg_token;
                Log.d(TAG, "pgtoken" + pgToken);

                url = url.replace("?pg_token=" + pg_token, "");
                Log.d(TAG, "pg_token 뺀 url: " + url);

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
        paymentRequest.put("partner_user_id", partnerUserId);
        paymentRequest.put("item_name", orderItemName);
        paymentRequest.put("quantity", "1");
        paymentRequest.put("total_amount", String.valueOf(totalPrice));
        paymentRequest.put("tax_free_amount", "0");
        paymentRequest.put("approval_url", BuildConfig.SERVER_IP + "kakaopay/success");
        paymentRequest.put("cancel_url", BuildConfig.SERVER_IP + "kakaopay/cancel");
        paymentRequest.put("fail_url", BuildConfig.SERVER_IP + "kakaopay/fail");

        Call<KakaoPayReadyResponseDTO> call = kakaoPayService.createPaymentRequest(BuildConfig.KAKAOPAY_ADMIN_KEY, paymentRequest);
        call.enqueue(new Callback<KakaoPayReadyResponseDTO>() {
            @Override
            public void onResponse(Call<KakaoPayReadyResponseDTO> call, Response<KakaoPayReadyResponseDTO> response) {
                Log.d(TAG, "onResponse create kakaopay : " + response);
                if (response.isSuccessful()) {
                    tid = response.body().getTid();
                    String url = response.body().getNextRedirectPcUrl();
                    webView.loadUrl(url);
                } else {
                    Log.d(TAG, "onResponse isNotSuccessful");
                }
            }

            @Override
            public void onFailure(Call<KakaoPayReadyResponseDTO> call, Throwable t) {
                Log.d(TAG, "onFailure create kakaopay : " + t.getMessage());
            }
        });

    }


    public void sendApprovalRequest() {

        HashMap<String, String> approvedRequest = new HashMap<>();
        approvedRequest.put("cid", "TC0ONETIME");
        approvedRequest.put("tid", tid);
        approvedRequest.put("partner_order_id", "openbook");
        approvedRequest.put("partner_user_id", partnerUserId);
        approvedRequest.put("pg_token", pgToken);

        Call<KakaoPayApproveResponseDTO> call = kakaoPayService.requestApprovedPayment(BuildConfig.KAKAOPAY_ADMIN_KEY, approvedRequest);
        call.enqueue(new Callback<KakaoPayApproveResponseDTO>() {
            @Override
            public void onResponse(Call<KakaoPayApproveResponseDTO> call, Response<KakaoPayApproveResponseDTO> response) {
                Log.d(TAG, "onResponse kakao approved: " + response.body());
                if (response.isSuccessful()) {
                    String approvedAt = response.body().getApprovedAt();

                    if (partnerUserId.equals("admin")) {
                        Intent intent = new Intent(KakaoPay.this, Admin.class);
                        intent.putExtra("tableName", tableName);
                        intent.putExtra("approvedAt", approvedAt);
                        startActivity(intent);

                    } else {
                        Intent intent = new Intent(KakaoPay.this, Menu.class);
                        intent.putExtra("myData", myData);
                        intent.putExtra("approvedAt", approvedAt);
                        startActivity(intent);
                    }


                }
            }

            @Override
            public void onFailure(Call<KakaoPayApproveResponseDTO> call, Throwable t) {
                Log.d(TAG, "onFailure kakao approved: " + t.getMessage());
            }
        });


    }

}
