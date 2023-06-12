package com.example.openbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Activity.Menu;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

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
    MyWebViewClient myWebViewClient;
    OkHttpClient okHttpClient;

    String cid = "TC0ONETIME"; //가맹점 테스트 코드
    String adminKey = "31a4ed25936fb8af2b4cf65b41ce17ec";

    String tidPin;
    String pgToken;

    String get_id;
    String menuName;
    int menuPrice;
    String jsonOrderList;
    String paymentStyle;

    String tempUrl = null;


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
        myWebViewClient = new MyWebViewClient();


        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(myWebViewClient);

        kakaoPayReady();


    }


    public class MyWebViewClient extends WebViewClient {


        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

            String url = request.getUrl().toString();

            Log.d(TAG, "shouldOverrideUrlLoading: url\n" + url);
            Log.d(TAG, "tempUrl :" + tempUrl);

            if (url.equals(tempUrl)) {
                Log.d(TAG, "같은 url 들어옴");

            } else if (url != null && url.contains("pg_token=")) {
                String pg_token = url.substring(url.indexOf("pg_token=") + 9);
                pgToken = pg_token;
                Log.d(TAG, "should Token\n" + pgToken);

                if(tempUrl == null){
                    kakaoPayRequest();
                }

                tempUrl = url;

            }
//            else if (url != null && url.startsWith("intent://")) {
//                try {
//                    Log.d(TAG, "intent로 들어옴");
//                    Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
//                    Intent existPackage = getPackageManager().getLaunchIntentForPackage(intent.getPackage());
//
//                    if (existPackage != null) {
//                        Log.d(TAG, "package not null ");
//                        startActivity(intent);
//
//                    }
//
//                    return true;
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            view.loadUrl(url);


            return false;

        } // shouldOverrideUrlLoading

    } // MyWebView


    public void kakaoPayReady() {

        okHttpClient = new OkHttpClient();

        RequestBody requestBodyReady = new FormBody.Builder()
                .add("cid", "TC0ONETIME")
                .add("partner_order_id", "partner_order_id")
                .add("partner_user_id", "partner_user_id")
                .add("item_name", menuName)
                .add("quantity", "1") //상품 수량
                .add("total_amount", String.valueOf(menuPrice)) //총 금액
                .add("tax_free_amount", "0") // 상품 비과세
                .add("approval_url", "http://3.36.255.141//success") // 성공 시 redirect url
                .add("cancel_url", "http://3.36.255.141/kakaopayCancel.html") // 취소 시 redirect url
                .add("fail_url", "http://3.36.255.141//fail") // 실패 시 redirect url
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


                Log.d(TAG, "ready response: \n" + responseData);

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String tid = jsonObject.getString("tid");
                    String url = jsonObject.getString("next_redirect_pc_url");


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView.loadUrl(url);
                        }
                    });

                    tidPin = tid;

                    Log.d(TAG, "onResponse: " + url);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }


    public void kakaoPayRequest() {

        Log.d(TAG, "kakaoPayRequest: ");

        RequestBody requestBody = new FormBody.Builder()
                .add("cid", "TC0ONETIME")
                .add("tid", tidPin)
                .add("partner_order_id", "partner_order_id")
                .add("partner_user_id", "partner_user_id")
                .add("pg_token", pgToken)
                .build();

        Request request = new Request.Builder()
                .url("https://kapi.kakao.com/v1/payment/approve")
                .header("Authorization", "KakaoAK " + adminKey)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d(TAG, "onFailure: " + e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();

                Log.d(TAG, "request Response: " + responseData);

                try {
                    JSONObject jsonObject = new JSONObject(responseData);
                    String time = jsonObject.getString("created_at");
                    Log.d(TAG, "created_at :" + time);


                    JSONObject orderList = new JSONObject(jsonOrderList);
                    orderList.put("orderTime", time);

                    Intent intent = new Intent(KakaoPay.this, Menu.class);
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
        });
    }

}
