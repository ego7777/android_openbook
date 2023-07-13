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

import com.example.openbook.Activity.Admin;
import com.example.openbook.Activity.Menu;
import com.example.openbook.Data.MyData;

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

public class KakaoPay extends AppCompatActivity {

    String TAG = "KakaoPay_TAG";

    WebView webView;
    MyWebViewClient myWebViewClient;
    OkHttpClient okHttpClient;

    String cid = "TC0ONETIME"; //가맹점 테스트 코드
    String adminKey = "31a4ed25936fb8af2b4cf65b41ce17ec";

    String tidPin;
    String pgToken;

    String menuName, tableName;
    int menuPrice;
    String jsonOrderList;


    String tempUrl = null;

    MyData myData;
    String get_id;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kakaopay_activity);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        Log.d(TAG, "myData id: " + myData.getId());
//        Log.d(TAG, "myData paymentStyle: " + myData.getPaymentStyle());

        get_id = getIntent().getStringExtra("get_id");
        Log.d(TAG, "get_id: " + get_id);

        if(get_id == null){
            get_id = myData.getId();
            Log.d(TAG, "get_id null: " + get_id);
        }

        menuName = getIntent().getStringExtra("menuName");
        menuPrice = getIntent().getIntExtra("menuPrice", 0);
        Log.d(TAG, "menuPrice: " + menuPrice);
        jsonOrderList = getIntent().getStringExtra("jsonOrderList");

        // 웹 뷰 설정
        webView = findViewById(R.id.kakaopay_webview);
        myWebViewClient = new MyWebViewClient();


        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(myWebViewClient);

        sendPaymentRequest();


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

                url = url.replace("?pg_token="+pg_token, "");
                Log.d(TAG, "pg_token 뺀 url: " + url);

                if(tempUrl == null){
                    sendApprovalRequest();
                }

                tempUrl = url;

            }

            if(url.contains("cancel")){
                finish();
            }else if(url.contains("fail")){
               finish();
            }else if(url.contains("success")){
                finish();
            }

            view.loadUrl(url);


            return false;

        } // shouldOverrideUrlLoading

    } // MyWebView


    public void sendPaymentRequest() {

        String url = "https://kapi.kakao.com/v1/payment/ready";

        okHttpClient = new OkHttpClient();

        try{
            JSONObject jsonObject = new JSONObject(jsonOrderList);
            tableName = jsonObject.getString("table");

        }catch (JSONException e){
            e.printStackTrace();
        }


        RequestBody requestBodyReady = new FormBody.Builder()
                .add("cid", "TC0ONETIME")
                .add("partner_order_id", "partner_order_id")
                .add("partner_user_id", tableName)
                .add("item_name", menuName) //상품명(결제 할 때 띄워짐)
                .add("quantity", "1") //상품 수량
                .add("total_amount", String.valueOf(menuPrice)) //결제금액
                .add("tax_free_amount", "0") // 면세금액
                .add("approval_url", "http://3.36.255.141/kakaopay/success") // 성공 시 redirect url
                .add("cancel_url", "http://3.36.255.141/kakopay/cancel") // 취소 시 redirect url
                .add("fail_url", "http://3.36.255.141/kakaopay/fail") // 실패 시 redirect url
                .build();

        Request request = new Request.Builder()
                .url(url)
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


    public void sendApprovalRequest() {

        RequestBody requestBody = new FormBody.Builder()
                .add("cid", "TC0ONETIME")
                .add("tid", tidPin)
                .add("partner_order_id", "partner_order_id")
                .add("partner_user_id", tableName)
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
                    tableName = jsonObject.getString("partner_user_id");
                    Log.d(TAG, "created_at :" + time);


                    JSONObject orderList = new JSONObject(jsonOrderList);
                    orderList.put("orderTime", time);

                    if(get_id.equals("admin")){
                        Intent intent = new Intent(KakaoPay.this, Admin.class);
                        intent.putExtra("get_id", "admin");
                        intent.putExtra("tableName", tableName);
                        intent.putExtra("orderList", orderList.toString());
                        // 돌아가면 데이터 지우는 것으로.....?!

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(intent);
                                overridePendingTransition(0, 0);
                            }
                        });

                    }else{
                        Intent intent = new Intent(KakaoPay.this, Menu.class);
                        intent.putExtra("orderList", orderList.toString());
                        intent.putExtra("myData", myData);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(intent);
                            }
                        });
                    }




                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        });
    }

}
