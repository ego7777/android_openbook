package com.example.openbook.startActivity;


import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.BuildConfig;
import com.example.openbook.DialogCustom;
import com.example.openbook.R;
import com.example.openbook.RetrofitManager;
import com.example.openbook.RetrofitService;
import com.example.openbook.SuccessOrNot;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignUp extends AppCompatActivity {
    String TAG = "SignupTAG";
    boolean isIdDuplicate, isPasswordMatch, isPhoneMatch, isEmailMatch = false;
    String id, password;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /**
         * 뒤로가기
         */
        TextView cancelButton = findViewById(R.id.signup_cancel_button);
        cancelButton.setOnClickListener(view -> finish());


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

        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        final RetrofitService service = retrofit.create(RetrofitService.class);



        /**
         * 아이디 중복 확인
         */
        checkDuplicateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkDuplicateButton.getText().toString().equals("수정")) {
                    Log.d(TAG, "수정: ");
                    idEditText.setEnabled(true);
                    // 아이디 입력 값 초기화
                    id = null;
                    idEditText.setText(null);
                    checkDuplicateButton.setText("중복확인");
                    duplicateWarning.setVisibility(View.GONE);
                    isIdDuplicate = false;

                } else {
                    id = idEditText.getText().toString();
                    Log.d(TAG, "onClick id hashcode: " + id.hashCode());

                    if (id.isEmpty()) {
                        Toast.makeText(SignUp.this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    } else {
                        service.requestIdDuplication(id.hashCode())
                                .enqueue(new Callback<SuccessOrNot>() {
                                    @Override
                                    public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                                        Log.d(TAG, "onResponse SignUp: " + response.body());
                                        if (response.isSuccessful()) {
                                            switch (response.body().getResult()) {
                                                case "success":
                                                    dlg.showAlertDialog(SignUp.this, "사용 할 수 있는 아이디 입니다.");
                                                    duplicateWarning.setVisibility(View.GONE);
                                                    idEditText.setEnabled(false);
                                                    checkDuplicateButton.setText("수정");
                                                    isIdDuplicate = true;
                                                    break;

                                                case "failed":
                                                    duplicateWarning.setVisibility(View.VISIBLE);
                                                    isIdDuplicate = false;
                                                    break;
                                            }

                                        } else {
                                            Toast.makeText(SignUp.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<SuccessOrNot> call, Throwable t) {
                                        Log.d(TAG, "onFailure SignUp: " + t.getMessage());
                                    }
                                });
                    }

                }
            }
        });

        /**
         * 비밀번호 확인
         */

        pwConfirmEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!pwEditText.getText().toString().equals(pwConfirmEditText.getText().toString())) {
                    passwordWarning.setVisibility(View.VISIBLE);
                } else {
                    isPasswordMatch = true;
                    passwordWarning.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /**
                 * 이메일 형식
                 */
                String emailCk = emailEditText.getText().toString().trim();
                Pattern pattern = android.util.Patterns.EMAIL_ADDRESS;
                isEmailMatch = pattern.matcher(emailCk).matches();


                /**
                 * 핸드폰 형식
                 */
                String phoneCk = phoneEditText.getText().toString().trim();
                Pattern pattern_phone = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");
                isPhoneMatch = pattern_phone.matcher(phoneCk).matches();
                Log.d(TAG, "isPhoneMatch: " + isPhoneMatch);


                if (id.isEmpty() || id == null) {

                    dlg.showAlertDialog(SignUp.this, "아이디를 입력해주세요.");

                } else if (pwEditText.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignUp.this, "비밀번호를 입력해주세요.");

                } else if (phoneEditText.getText().toString().isEmpty()) {

                    dlg.showAlertDialog(SignUp.this, "핸드폰 번호를 입력해주세요.");

                } else if (emailEditText.getText().toString().isEmpty()) {
                    dlg.showAlertDialog(SignUp.this, "이메일을 입력해주세요.");

                } else if (!isPasswordMatch) {
                    dlg.showAlertDialog(SignUp.this, "비밀번호가 일치하지 않습니다.");

                } else if (!isIdDuplicate) {
                    dlg.showAlertDialog(SignUp.this, "아이디를 중복 확인을 해주세요.");

                } else if (!isPhoneMatch) {
                    dlg.showAlertDialog(SignUp.this, "핸드폰 번호를 형식에 맞게 입력해주세요.");
                    Log.d(TAG, "phoneMatch : " + phoneCk);

                } else if (!isEmailMatch) {
                    dlg.showAlertDialog(SignUp.this, "이메일을 형식에 맞게 입력해주세요.");
                    Log.d(TAG, "email.getText : " + emailCk);
                } else {

                    Call<SuccessOrNot> call = service.requestSignUp(idEditText.getText().toString().trim(),
                            idEditText.getText().toString().trim().hashCode(),
                            pwEditText.getText().toString().trim().hashCode(),
                            phoneEditText.getText().toString().trim(),
                            emailEditText.getText().toString().trim());

                    call.enqueue(new Callback<SuccessOrNot>() {
                        @Override
                        public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                            Log.d(TAG, "onResponse Signup: " + response.body());
                            if (response.isSuccessful()) {
                                switch (response.body().getResult()) {
                                    case "success":
                                        Toast.makeText(SignUp.this, "회원가입에 성공했습니다.", Toast.LENGTH_LONG).show();
                                        finish();
                                        break;
                                    case "failed":
                                        Toast.makeText(SignUp.this, "회원가입에 실패했습니다.", Toast.LENGTH_LONG).show();
                                        break;

                                }
                            } else {
                                Toast.makeText(SignUp.this, getResources().getString(R.string.networkError), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<SuccessOrNot> call, Throwable t) {
                            Log.d(TAG, "onFailure Signup: " + t.getMessage());
                        }
                    });
                }
            }
        });
    }


}
