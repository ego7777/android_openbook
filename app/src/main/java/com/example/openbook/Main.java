package com.example.openbook;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Activity.Login;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Main extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    String personId ="";

    String TAG = "main_log";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        String get_id = getIntent().getStringExtra("id");


        TextView member_id =  findViewById(R.id.member_id);
        Log.d(TAG, "get_id : " +get_id);


        if(get_id.length()>0){
            member_id.setText(get_id + "님 반갑습니다 :)");
        }

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        gsc = GoogleSignIn.getClient(Main.this, gso);

        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
        if(acct != null){
            String personName = acct.getGivenName();
            String personEmail = acct.getEmail();
            personId = acct.getId();

            member_id.setText(personId + "님 반갑습니다 :)");
        }





        Button logout = findViewById(R.id.main_logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (!personId.isEmpty()) {
                    gsc.signOut().addOnCompleteListener(Main.this, new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                            startActivity(new Intent(Main.this, Login.class));
                            Toast.makeText(getApplicationContext(), "로그아웃 되었습니다.", Toast.LENGTH_LONG).show();
                        }
                    });
                }else{

                    //요청 만들기
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://3.36.255.141/logout.php")
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(@NonNull Call call, @NonNull IOException e) {
                            Log.d(TAG, "onFailure: " + e);
                        }

                        @Override
                        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (!response.isSuccessful()) {
                                            Log.d(TAG, "응답 실패" + response);
                                        } else {
                                            Log.d(TAG, "응답 성공");
                                            final String responseData = response.body().string();
                                            Log.d(TAG, "responseData : " + responseData);

                                            if (responseData.equals("실패")) {
                                                Log.d(TAG, "로그아웃 실패");
                                            } else if (responseData.equals("성공")) {
                                                Log.d(TAG, "로그아웃 성공");
                                                startActivityflag(Login.class);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

                }

            }
        });

    }

    public void startActivityflag(Class c){
        Intent intent = new Intent(getApplicationContext(), c);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);

        // 화면전환 애니메이션 없애기
        overridePendingTransition(0, 0);

    }
}

