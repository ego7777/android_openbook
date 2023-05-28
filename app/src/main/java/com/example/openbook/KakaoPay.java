package com.example.openbook;

import android.content.Context;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KakaoPay extends AppCompatActivity {

    String TAG = "KakaoPay_TAG";

    WebView webView;
    //    MyWebViewClient myWebViewClient;
    OkHttpClient okHttpClient;

    String cid = "TC0ONETIME"; //가맹점 테스트 코드
    String adminKey = "31a4ed25936fb8af2b4cf65b41ce17ec";
    String url;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kakaopay_activity);


        String orderList = getIntent().getStringExtra("orderList");

        // 웹 뷰 설정
        webView = findViewById(R.id.kakaopay_webview);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());


        Map<String, String> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");
        params.put("partner_order_id", "가맹점 주문 번호");
        params.put("partner_user_id", "가맹점 회원 ID");
        params.put("item_name", "상품명");
        params.put("quantity", "주문수량");
        params.put("total_amount", "총 금액");
        params.put("tax_free_amount", "상품 비과세 금액");
        params.put("approval_url", "http://192.168.219.171:8080/payment/success"); // 성공 시 redirect url
        params.put("cancel_url", "http://localhost:8080/payment/cancel"); // 취소 시 redirect url
        params.put("fail_url", "http://localhost:8080/payment/fail"); // 실패 시 redirect url


        okHttpClient = new OkHttpClient();

        RequestBody requestBodyReady = new FormBody.Builder()
                .add("cid", "TC0ONETIME")
                .add("partner_order_id", "가맹점 주문 번호")
                .add("partner_user_id", "가맹점 회원 ID")
                .add("item_name", "상품명")
                .add("quantity", "1") //상품 수량
                .add("total_amount", "2000") //총 금액
                .add("tax_free_amount", "0") // 상품 비과세
                .add("approval_url", "http://www.naver.com/success") // 성공 시 redirect url
                .add("cancel_url", "http://www.naver.com/cancel") // 취소 시 redirect url
                .add("fail_url", "http://www.naver.com/fail") // 실패 시 redirect url
                .build();

        Request request = new Request.Builder()
                .url("https://kapi.kakao.com/v1/payment/ready")
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


                Log.d(TAG, "onResponse: " + responseData);

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    url = jsonObject.getString("msg");
                    url = url.substring(url.indexOf("(")+1);
                    url = url.substring(0, url.indexOf(")"));

                    Log.d(TAG, "onResponse: " + url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl(url);
                    }
                });

            }
        });

//        RequestBody requestBodyApprove = new FormBody.Builder()
//                .add("cid", "TC0ONETIME")
//                .add("tid", )
//                .add("partner_order_id", "가맹점 주문 번호")
//                .add("partner_user_id", "가맹점 회원 ID")

    }

    public class MyWebViewClient extends WebViewClient {

    }
}
