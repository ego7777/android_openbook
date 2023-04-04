package com.example.openbook.Activity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.DialogCustom;
import com.example.openbook.R;

import java.io.IOException;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignUp extends AppCompatActivity {
    String TAG = "signup_log";
    String url = "http://3.36.255.141/signup.php";

    String overlapCk="0";

    Dialog dlg;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        /**
         * 뒤로가기
         */

        TextView back = findViewById(R.id.signup_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        EditText id = findViewById(R.id.signup_id);
        EditText pw = findViewById(R.id.signup_pw);
        EditText pw_confirm = findViewById(R.id.signup_pw_confirm);
        EditText phone = findViewById(R.id.signup_phone);
        EditText email = findViewById(R.id.signup_email);

        Button overlap = findViewById(R.id.overlap);
        Button register = findViewById(R.id.register);

        TextView id_overlap_ck = findViewById(R.id.id_overlap_ck);
        TextView pw_ck = findViewById(R.id.pw_ck);

        DialogCustom dlg = new DialogCustom();

        /**
         * 비밀번호 암호화 하세유...!!!!
         */



        /**
         * 아이디 중복 확인
         */
        overlap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RequestBody overlap_id = new FormBody.Builder()
                        .add("overlap_id", id.getText().toString().trim())
                        .build();

                OkHttpClient overlap_client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url("http://3.36.255.141/overlap.php")
                        .post(overlap_id)
                        .build();
                Log.d(TAG, "request :" + request);

                overlap_client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onFailure: " + e);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {

                        // 서브 스레드 Ui 변경 할 경우 에러
                        // 메인스레드 Ui 설정
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {

                                    if (!response.isSuccessful()) {
                                        // 응답 실패
                                        Log.d(TAG, "응답실패" + response);
                                        Toast.makeText(getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();

                                    } else {
                                        // 응답 성공
                                        Log.d(TAG, "응답 성공");
                                        final String responseOverlap = response.body().string().trim();
                                        //회원 가입할 때 중복확인 안하거나, 중복되면 가입 못하게 하려고 변수 저장
                                        overlapCk = responseOverlap;

                                        id.addTextChangedListener(new TextWatcher() {
                                            @Override
                                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                                            }

                                            @Override
                                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                                                overlapCk = "0";
                                            }

                                            @Override
                                            public void afterTextChanged(Editable editable) {

                                            }
                                        });

                                        Log.d(TAG, "responseOverlap : " + responseOverlap);
                                        if (responseOverlap.equals("1")) {
                                            id_overlap_ck.setText("사용할 수 없는 아이디 입니다.");
                                            id_overlap_ck.setTextColor(Color.RED);
                                            id.setTextColor(Color.RED);

                                        } else {
                                            Log.d(TAG, "사용할 수 있는 아이디 입니다." + responseOverlap);
                                            id_overlap_ck.setText("사용 할 수 있는 아이디 입니다.");
                                            id_overlap_ck.setTextColor(Color.BLACK);
                                            id.setTextColor(Color.BLACK);

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
        });

        pw_confirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!pw.getText().toString().equals(pw_confirm.getText().toString())){
                    pw_ck.setText("비밀번호가 일치하지 않습니다.");
                    pw_ck.setTextColor(Color.RED);
                }else{
                    pw_ck.setText("");
                    pw_ck.setTextColor(Color.BLACK);
                }
            }
        });



        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean matcher_email;
                boolean matcher_phone;

                /**
                 * 이메일 형식
                 */
                String emailCk = email.getText().toString().trim();
                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
                matcher_email = pattern.matcher(emailCk).matches();

                /**
                 * 핸드폰 형식
                 */
                String phoneCk = phone.getText().toString().trim();
                Log.d(TAG, "phoneCk1 :" + phoneCk);

                Pattern pattern_phone = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");
                Log.d(TAG, "pattern_phone1 :" + pattern_phone);

                matcher_phone = pattern_phone.matcher(phoneCk).matches();
                Log.d(TAG, "matcher_phone1 : " + matcher_phone);

                if (id.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignUp.this, "아이디를 입력해주세요.");

                }else if (pw.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignUp.this, "비밀번호를 입력해주세요.");

                }else if (phone.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignUp.this, "핸드폰 번호를 입력해주세요.");

                }else if (email.getText().toString().isEmpty()) {
                    dlg.showAlertDialog(SignUp.this,"이메일을 입력해주세요.");

                }else if (!pw.getText().toString().equals(pw_confirm.getText().toString())) {
                    dlg.showAlertDialog(SignUp.this,"비밀번호가 일치하지 않습니다.");

                }else if(overlapCk.equals("1") ) {
                    dlg.showAlertDialog(SignUp.this,"사용할 수 없는 아이디 입니다.");

                }else if(overlapCk.equals("0")) {
                    dlg.showAlertDialog(SignUp.this,"아이디 중복확인을 해주세요.");

                }else if(!matcher_phone){
                    dlg.showAlertDialog(SignUp.this,"핸드폰 번호를 형식에 맞게 입력해주세요.");


                    Log.d(TAG, "phoneCk2 : " + phoneCk);
                    Log.d(TAG, "matcher_phone2 :" + matcher_phone);

                }else if(!matcher_email){
                    dlg.showAlertDialog(SignUp.this,"이메일을 형식에 맞게 입력해주세요.");

                    Log.d(TAG, "email.getText : " + emailCk);
                    Log.d(TAG, "matcher_email :" + matcher_email);
                }else{
                    // POST 파라미터 추가
                    RequestBody formBody = new FormBody.Builder()
                            .add("id", id.getText().toString().trim())
                            .add("pw", pw.getText().toString().trim())
                            .add("phone",phone.getText().toString().trim())
                            .add("email",email.getText().toString().trim())
                            .build();
                    Log.d(TAG, "formBody : " + formBody);

                    // 요청 만들기
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();
                    Log.d(TAG, "request :" +request);

                    // 응답 콜백
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {

                            e.printStackTrace();
                            Log.d(TAG, "onFailure: " + e);
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {

                            // 서브 스레드 Ui 변경 할 경우 에러
                            // 메인스레드 Ui 설정
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (!response.isSuccessful()) {
                                            Log.d(TAG, "응답실패");
                                            Toast.makeText(getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();

                                        } else {
                                            Log.d(TAG, "응답 성공");
                                            final String responseData = response.body().string();
                                            if(responseData.equals("성공")) {
                                                Toast.makeText(getApplicationContext(), "회원가입에 성공했습니다.", Toast.LENGTH_LONG).show();
                                                startActivityflag(Login.class);
                                                Log.d(TAG, "Login.class");
                                            }else {
                                                Log.d(TAG, "회원가입에 실패 했습니다." + responseData);
                                                Toast.makeText(getApplicationContext(), "회원가입에 실패했습니다.", Toast.LENGTH_LONG).show();
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
            }});
    }







    // 인텐트 화면전환
    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
    public void startActivityflag(Class c) {
        Intent intent = new Intent(getApplicationContext(), c);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // 화면전환 애니메이션 없애기
        overridePendingTransition(0, 0);
    }
}
