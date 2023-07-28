package com.example.openbook.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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

public class SignupTest extends AppCompatActivity {

    String TAG = "SignupTAG";
    String url = "http://3.36.255.141/signup.php";

    String overlapCk="0";
    OkHttpClient okHttpClient;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_test);


        TextView cancel = findViewById(R.id.signup_cancel_button);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        EditText idEditText = findViewById(R.id.signup_editText_id);
        EditText pwEditText = findViewById(R.id.signup_editText_password);
        EditText pwConfirmEditText = findViewById(R.id.signup_editText_check_password);
        EditText phoneEditText = findViewById(R.id.signup_editText_phone);
        EditText emailEditText = findViewById(R.id.signup_editText_email);

        Button checkDuplicateButton = findViewById(R.id.signup_duplicate_button);
        Button registerButton = findViewById(R.id.signup_register_button);

        TextView duplicateWarning = findViewById(R.id.signup_duplicate_warning);
        TextView passwordWarning = findViewById(R.id.signup_textview_password_warning);

        DialogCustom dlg = new DialogCustom();
        okHttpClient = new OkHttpClient();

        /**
         * 비밀번호 암호화 하세유...!!!!
         */



        /**
         * 아이디 중복 확인
         */


        checkDuplicateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkDuplicateButton.getText().toString().equals("수정")) {
                    Log.d(TAG, "수정: ");
                    idEditText.setEnabled(true);
                    idEditText.setText(""); // 아이디 입력 값 초기화
                    checkDuplicateButton.setText("중복확인");
                    duplicateWarning.setText(""); // 중복 경고 메시지 초기화
                } else {
                    Log.d(TAG, "중복확인: ");
                    RequestBody overlap_id = new FormBody.Builder()
                            .add("overlap_id", idEditText.getText().toString().trim())
                            .build();

                    Request request = new Request.Builder()
                            .url("http://3.36.255.141/overlap.php")
                            .post(overlap_id)
                            .build();
                    Log.d(TAG, "request :" + request);

                    okHttpClient.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.d(TAG, "onFailure: " + e);
                        }

                        @Override
                        public void onResponse(Call call, final Response response) {
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

                                            Log.d(TAG, "responseOverlap : " + responseOverlap);
                                            if (responseOverlap.equals("1")) {
                                                duplicateWarning.setText("사용할 수 없는 아이디 입니다.");
                                                duplicateWarning.setTextColor(Color.RED);
                                                idEditText.setTextColor(Color.RED);

                                            } else {
                                                Log.d(TAG, "사용할 수 있는 아이디 입니다." + responseOverlap);
                                                duplicateWarning.setText("사용 할 수 있는 아이디 입니다.");
                                                duplicateWarning.setTextColor(getColor(R.color.blue_purple));
                                                idEditText.setTextColor(Color.BLACK);
                                                idEditText.setEnabled(false);
                                                checkDuplicateButton.setText("수정");
                                                Log.d(TAG, "idEditText enable: " + idEditText.isEnabled());


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











        pwConfirmEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!pwEditText.getText().toString().equals(pwConfirmEditText.getText().toString())){
                    passwordWarning.setText("비밀번호가 일치하지 않습니다.");
                    passwordWarning.setTextColor(Color.RED);
                }else{
                    passwordWarning.setText("");
                    passwordWarning.setTextColor(Color.BLACK);
                }
            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean matcher_email;
                boolean matcher_phone;

                /**
                 * 이메일 형식
                 */
                String emailCk = emailEditText.getText().toString().trim();
                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
                matcher_email = pattern.matcher(emailCk).matches();

                /**
                 * 핸드폰 형식
                 */
                String phoneCk = phoneEditText.getText().toString().trim();
                Log.d(TAG, "phoneCk1 :" + phoneCk);

                Pattern pattern_phone = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");
                Log.d(TAG, "pattern_phone1 :" + pattern_phone);

                matcher_phone = pattern_phone.matcher(phoneCk).matches();
                Log.d(TAG, "matcher_phone1 : " + matcher_phone);

                if (idEditText.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignupTest.this, "아이디를 입력해주세요.");

                }else if (pwEditText.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignupTest.this, "비밀번호를 입력해주세요.");

                }else if (phoneEditText.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignupTest.this, "핸드폰 번호를 입력해주세요.");

                }else if (emailEditText.getText().toString().isEmpty()) {
                    dlg.showAlertDialog(SignupTest.this,"이메일을 입력해주세요.");

                }else if (!pwEditText.getText().toString().equals(pwConfirmEditText.getText().toString())) {
                    dlg.showAlertDialog(SignupTest.this,"비밀번호가 일치하지 않습니다.");

                }else if(overlapCk.equals("1") ) {
                    dlg.showAlertDialog(SignupTest.this,"사용할 수 없는 아이디 입니다.");

                }else if(overlapCk.equals("0")) {
                    dlg.showAlertDialog(SignupTest.this,"아이디 중복확인을 해주세요.");

                }else if(!matcher_phone){
                    dlg.showAlertDialog(SignupTest.this,"핸드폰 번호를 형식에 맞게 입력해주세요.");


                    Log.d(TAG, "phoneCk2 : " + phoneCk);
                    Log.d(TAG, "matcher_phone2 :" + matcher_phone);

                }else if(!matcher_email){
                    dlg.showAlertDialog(SignupTest.this,"이메일을 형식에 맞게 입력해주세요.");

                    Log.d(TAG, "email.getText : " + emailCk);
                    Log.d(TAG, "matcher_email :" + matcher_email);
                }else{
                    // POST 파라미터 추가
                    RequestBody formBody = new FormBody.Builder()
                            .add("id", idEditText.getText().toString().trim())
                            .add("pw", pwEditText.getText().toString().trim())
                            .add("phone",phoneEditText.getText().toString().trim())
                            .add("email",emailEditText.getText().toString().trim())
                            .build();
                    Log.d(TAG, "formBody : " + formBody);

                    // 요청 만들기
                    Request request = new Request.Builder()
                            .url(url)
                            .post(formBody)
                            .build();
                    Log.d(TAG, "request :" +request);

                    // 응답 콜백
                    okHttpClient.newCall(request).enqueue(new Callback() {
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

