package com.example.openbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Activity.Menu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Kakao extends AppCompatActivity {

    String TAG = "Kakao_TAG";
    WebView webView;
    OkHttpClient okHttpClient;

    String adminKey = "31a4ed25936fb8af2b4cf65b41ce17ec";

    String get_id;
    String menuName;
    int menuPrice;
    String jsonOrderList;
    String paymentStyle;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kakaopay_activity);

        get_id = getIntent().getStringExtra("get_id");
        menuName = getIntent().getStringExtra("menuName");
        menuPrice = getIntent().getIntExtra("menuPrice", 0);
        jsonOrderList = getIntent().getStringExtra("jsonOrderList");
        paymentStyle = getIntent().getStringExtra("paymentStyle");

        // 웹 뷰 설정
        webView = findViewById(R.id.kakaopay_webview);
        webView.getSettings().setJavaScriptEnabled(true);

        sendPaymentRequest();
    }

    public void sendPaymentRequest() {

        String paymentUrl = "https://kapi.kakao.com/v1/payment/ready";

        okHttpClient = new OkHttpClient();

        RequestBody requestBodyReady = new FormBody.Builder()
                .add("cid", "TC0ONETIME")
                .add("partner_order_id", "주문번호")
                .add("partner_user_id", "사용자 식별값")
                .add("item_name", menuName)
                .add("quantity", "1") //상품 수량
                .add("total_amount", String.valueOf(menuPrice)) //결제금액
                .add("tax_free_amount", "0") // 면세금액
                .add("approval_url", "http://3.36.255.141/success") // 성공 시 redirect url
                .add("cancel_url", "http://3.36.255.141/cancel") // 취소 시 redirect url
                .add("fail_url", "http://3.36.255.141/fail") // 실패 시 redirect url
                .build();

        Request request = new Request.Builder()
                .url(paymentUrl)
                .header("Authorization", "KakaoAK " + adminKey)
                .post(requestBodyReady)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                String responseData = response.body().string();
                Log.d(TAG, "ready response: \n" + responseData);

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String redirectPcUrl = jsonObject.getString("next_redirect_pc_url");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(redirectPcUrl);
                            Log.d(TAG, "redirectPcUrl 실행: ");
                        }
                    });
                } catch (JSONException e) {

                }




                runOnUiThread(() -> {
                    webView.setWebViewClient(new WebViewClient() {
                        @Override
                        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                            // 결제 성공 URL 체크
                            Log.d(TAG, "shouldOverrideUrlLoading: 진입");
                            String url = request.getUrl().toString();

                            Log.d(TAG, "shouldOverrideUrlLoading url: " + url);

                            if (url.startsWith("http://3.36.255.141/success")) {
                                String pg_token = url.substring(url.indexOf("pg_token=") + 9);
                                sendApprovalRequest(responseData, pg_token);
                                return true;
                            }

                            return super.shouldOverrideUrlLoading(view, request);

                        }
                    });
                });

            }
        });
    }


    private void sendApprovalRequest(String responseData, String pgToken) {
        Log.d(TAG, "sendApprovalRequest: 진입");
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            String tid = jsonObject.getString("tid");


            RequestBody requestBody = new FormBody.Builder()
                    .add("cid", "TC0ONETIME")
                    .add("tid", tid)
                    .add("partner_order_id", "partner_order_id")
                    .add("partner_user_id", "partner_user_id")
                    .add("pg_token", pgToken)
                    .build();

            String approvalUrl = "https://kapi.kakao.com/v1/payment/approve";

            Request request = new Request.Builder()
                    .url(approvalUrl)
                    .post(requestBody)
                    .build();

            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d(TAG, "onFailure: " + e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        Log.d(TAG, "결제 승인 응답: " + responseData);
                        //결제 승인 요청 응답 처리
                        successPayment(responseData);

                    } else {
                        //결제 승인 요청 실패 처리
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void successPayment(String responseData) {
        try {
            JSONObject jsonObject = new JSONObject(responseData);
            String time = jsonObject.getString("created_at");
            Log.d(TAG, "created_at :" + time);


            JSONObject orderList = new JSONObject(jsonOrderList);
            orderList.put("orderTime", time);

            Intent intent = new Intent(Kakao.this, Menu.class);
            intent.putExtra("orderList", orderList.toString());
            intent.putExtra("paymentStyle", paymentStyle);
            intent.putExtra("get_id", get_id);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
