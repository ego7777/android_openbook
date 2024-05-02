package com.example.openbook.Activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.openbook.Data.AdminData;
import com.example.openbook.BuildConfig;
import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.MyData;
import com.example.openbook.DialogManager;
import com.example.openbook.PaymentCategory;
import com.example.openbook.R;
import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;
import com.example.openbook.retrofit.TableListDTO;
import com.example.openbook.TableQuantity;
import com.example.openbook.startActivity.LoginResponseModel;
import com.example.openbook.startActivity.SignUp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;

import com.google.android.gms.tasks.Task;
import com.google.gson.Gson;


import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class Login extends AppCompatActivity {

    GoogleSignInOptions googleSignInOptions;
    GoogleSignInClient googleSignInClient;
    ActivityResultLauncher<Intent> resultLauncher;

    String googleId, googleIdentifier, googleEmail;
    String TAG = "LoginTAG";

    ArrayList<AdminTableList> adminTableList;
    int tableFromDB;

    RetrofitService service;
    Dialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        /**
         * 아이디, 비밀번호
         */
        EditText idEditText = findViewById(R.id.login_editText_id);
        EditText pwEditText = findViewById(R.id.login_editText_pw);
        Button loginButton = findViewById(R.id.login_button);
        SignInButton googleLoginButton = findViewById(R.id.google_login);

        DialogManager dialogManager = new DialogManager();


        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        service = retrofit.create(RetrofitService.class);


        /**
         * 로그인
         */
        loginButton.setOnClickListener(view -> {
            String id = idEditText.getText().toString().trim();
            String password = pwEditText.getText().toString().trim();

            if (!id.isEmpty() && !password.isEmpty()) {

                progress = dialogManager.progressDialog(Login.this);
                progress.show();

                Call<LoginResponseModel> call = service.requestLogin(id.hashCode(), password.hashCode());
                call.enqueue(new Callback<LoginResponseModel>() {
                    @Override
                    public void onResponse(Call<LoginResponseModel> call, Response<LoginResponseModel> response) {
                        if (response.isSuccessful()) {
                            progress.dismiss();

                            switch (response.body().getResult()) {
                                case "success":
                                    switch (response.body().getId()) {
                                        case "admin":
                                            startActivityAdmin(Admin.class, id);
                                            break;

                                        default:
                                            startActivityCustomer(PaymentSelect.class, "myData", id);
                                            break;
                                    }
                                    break;

                                case "failed":
                                    dialogManager.positiveBtnDialog(Login.this, "아이디나 비밀번호가 일치하지 않습니다.").show();
                                    break;

                            }
                        } else {
                            Toast.makeText(Login.this, R.string.networkError, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponseModel> call, Throwable t) {
                        Log.d(TAG, "onFailure Login: " + t.getMessage());
                    }
                });

            } else {
                dialogManager.positiveBtnDialog(Login.this, "아이디와 패스워드를 모두 입력해주세요.").show();

            }
        });


        /**
         * 회원가입
         */
        Button signup = findViewById(R.id.signup_button);
        signup.setOnClickListener(view -> {
            Intent intent = new Intent(Login.this, SignUp.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });


        /**
         * 구글 로그인
         */

        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(BuildConfig.GOOGLE_API_KEY)
                .build();


        googleLoginButton.setOnClickListener(view -> {
            googleSignInClient = GoogleSignIn.getClient(Login.this, googleSignInOptions);
            Intent signInIntent = googleSignInClient.getSignInIntent();
            resultLauncher.launch(signInIntent);
        });

        resultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleSignInResult(task);
            } else {
                Log.d(TAG, "google login intent's result not okay");
            }
        });
    }


    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount googleSignInAccount = completedTask.getResult(ApiException.class);

            if (googleSignInAccount != null) {
                googleId = googleSignInAccount.getDisplayName();
                googleEmail = googleSignInAccount.getEmail();
                googleIdentifier = googleSignInAccount.getId();

                Call<SuccessOrNot> checkIdCall = service.requestIdDuplication(googleIdentifier.hashCode());
                checkIdCall.enqueue(new Callback<SuccessOrNot>() {
                    @Override
                    public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "onResponse google login: "  + response.body().getResult());
                            switch (response.body().getResult()) {
                                case "success":
                                    googleSignIn();
                                    break;
                                case "failed":
                                    startActivityCustomer(PaymentSelect.class, "myData", googleId);
                                    break;
                            }
                        } else {
                            Toast.makeText(Login.this, R.string.networkError, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SuccessOrNot> call, Throwable t) {
                        Log.d(TAG, "onFailure google checkID: " + t.getMessage());
                    }
                });


            } else {
                Log.d(TAG, "googleSignInAccount Null");
            }
        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    public void googleSignIn() {
        Call<SuccessOrNot> call = service.requestSignUp(googleId,
                googleIdentifier.hashCode(),
                0000, "000-0000-0000", googleEmail);
        call.enqueue(new Callback<SuccessOrNot>() {
            @Override
            public void onResponse(Call<SuccessOrNot> call, Response<SuccessOrNot> response) {
                Log.d(TAG, "onResponse google: " + response.body());
                if (response.isSuccessful()) {
                    switch (response.body().getResult()) {
                        case "success":
                            startActivityCustomer(PaymentSelect.class, "myData", googleId);
                            break;

                        case "failed":
                            Toast.makeText(getApplicationContext(), "회원가입에 실패했습니다.", Toast.LENGTH_LONG).show();
                            break;

                        default:
                            Log.d(TAG, "onResponse google default");
                    }
                } else {
                    Toast.makeText(Login.this, "네트워크 오류가 발생하였습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SuccessOrNot> call, Throwable t) {
                Log.d(TAG, "onFailure google: " + t.getMessage());
            }
        });
    }


    // 문자열 인텐트 전달 함수
    public void startActivityCustomer(Class c, String name, String sendString) {
        Intent intent = new Intent(getApplicationContext(), c);
        MyData myData = new MyData(sendString,
                tableFromDB,
                PaymentCategory.UNSELECTED,
                false,
                false,
                0,
                false);
        intent.putExtra(name, myData);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void startActivityAdmin(Class c, String id) {
        Intent intent = new Intent(getApplicationContext(), c);
        AdminData adminData = new AdminData(id, adminTableList, false);

        SharedPreferences sharedPreferences = getSharedPreferences("AdminInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();

        editor.putString("id", id);
        editor.putBoolean("isFcmExist", false);
        editor.putString("adminTableList", gson.toJson(adminTableList));
        Log.d(TAG, "startActivityAdmin: " + gson.toJson(adminTableList));
        editor.commit();

        intent.putExtra("adminTableList", adminTableList);
        intent.putExtra("adminData", adminData);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }


    @Override
    protected void onStart() {
        super.onStart();


        if(adminTableList == null || adminTableList.isEmpty()){
            adminTableList = new ArrayList<>();

            TableQuantity tableQuantity = new TableQuantity();
            tableQuantity.getTableQuantity(new Callback<TableListDTO>() {
                @Override
                public void onResponse(Call<TableListDTO> call, Response<TableListDTO> response) {

                    if(response.isSuccessful()){
                        switch (response.body().getResult()){
                            case "success" :
                                tableFromDB = response.body().getTableCount();

                                for(int i=1; i<tableFromDB+1; i++){
                                    adminTableList.add(new AdminTableList("table"+i,
                                            null,
                                            null,
                                            null,
                                            null,
                                            PaymentCategory.UNSELECTED.getValue(),
                                            0));
                                }

                                Log.d(TAG, "onStart Table Size: " + adminTableList.size());

                                break;
                            case "failed" :
                                Log.d(TAG, "onResponse table failed: ");
                        }
                    }else{
                        Log.d(TAG, "onResponse table isNotSuccessful");
                    }
                }
                @Override
                public void onFailure(Call<TableListDTO> call, Throwable t) {
                    Log.d(TAG, "onFailure table: " + t.getMessage());
                }
            });


        }
    }


}

