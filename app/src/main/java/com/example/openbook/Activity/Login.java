package com.example.openbook.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.openbook.Data.AdminTableList;
import com.example.openbook.Data.MyData;
import com.example.openbook.DialogCustom;
import com.example.openbook.R;
import com.example.openbook.TableQuantity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Login extends AppCompatActivity {

    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    String server_client_id = "520510988286-vq6c4kmjph9iatf225qkkjkpj5g1gjgp.apps.googleusercontent.com";

    String personId;
    String personName;

    String local_id;
    String TAG = "LoginTAG";

    ArrayList<AdminTableList> adminTableList;
    int tableFromDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        final OkHttpClient client = new OkHttpClient();






        /**
         * 아이디, 비밀번호 입력
         */
        EditText id = findViewById(R.id.login_editText_id);
        EditText pw = findViewById(R.id.login_pw);
        Button login = findViewById(R.id.login_button);

        DialogCustom dialogCustom = new DialogCustom();


        /**
         * 클릭하면 회원정보 확인해서 로그
         */
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (id.getText().toString().length() > 0 ||
                        pw.getText().toString().trim().length() > 0) {
                    local_id = id.getText().toString().trim();

                    Log.d(TAG, "local_id : " + local_id);
                    Log.d(TAG, "local_pw : " + pw.getText().toString());

                    //Post 파라미터 추가
                    RequestBody formBody = new FormBody.Builder()
                            .add("id", local_id)
                            .add("pw", pw.getText().toString().trim())
                            .build();


                    //요청 만들기

                    Request request = new Request.Builder()
                            .url("http://3.36.255.141/login.php")
                            .post(formBody)
                            .build();

                    //응답 콜백
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
                                            String responseData = response.body().string();
                                            Log.d(TAG, "responseData : " + responseData);

                                            if (responseData.equals("1")) {

                                                dialogCustom.showAlertDialog(Login.this, "아이디가 존재하지 않습니다");

                                            } else if (responseData.equals("2")) {

                                                dialogCustom.showAlertDialog(Login.this, "비밀번호가 일치하지 않습니다.");

                                            } else if (responseData.equals("성공")) {

                                                startActivityString(PaymentSelect.class,"myData", local_id);

                                            } else if (responseData.equals("admin")){

                                                startActivityTableList(Admin.class, adminTableList, "adminTableList",local_id);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    dialogCustom.showAlertDialog(Login.this, "입력 안 된 칸이 있습니다.");
                }
            }
        });


        /**
         * 회원가입
         */
        Button signup = findViewById(R.id.signup_button);
        signup.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(Login.this, SignupTest.class);
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });


        /**
         * 구글 로그인
         */
//         사용자 데이터를 요청을 위한 로그인 옵션 설정
//         DEFAULT_SIGN_IN parameter: 유저 ID, 기본 프로필 정보 요청 시 사용
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // email addresses도 요청함
                .requestIdToken(server_client_id)
                .build();
        Log.d(TAG, "gso");

//         gso로 객체 생성
        gsc = GoogleSignIn.getClient(Login.this, gso);
        Log.d(TAG, "gsc: " + gsc);

        SignInButton google_login = findViewById(R.id.google_login);

        TextView google_login_textView = (TextView) google_login.getChildAt(0);
        google_login_textView.setText("Continue With Google");
        google_login_textView.setTextSize(20);

        google_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = gsc.getSignInIntent();
                Log.d(TAG, "signInIntent: ");
                startActivityForResult(signInIntent, 1000);
                Log.d(TAG, "signIn: ");
            }
        });


    }


//    private void signIn() {
//        Intent signInIntent = gsc.getSignInIntent();
//        Log.d(TAG, "signInIntent: ");
//        startActivityForResult(signInIntent, 1000);
//        Log.d(TAG, "signIn: ");
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            Log.d(TAG, "task : " + task);

//            try{
//                task.getResult(ApiException.class);
//                navigateToMain();
//            }catch (ApiException e){
//                Log.d(TAG, "onActivityResult: " + e);
//            }
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Account " + acct);

            if (acct != null) {
                personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                personId = acct.getId();


                Log.d(TAG, "handleSignInResult:personName " + personName);
                Log.d(TAG, "handleSignInResult:personEmail " + personEmail);
                Log.d(TAG, "handleSignInResult:personId " + personId);


                RequestBody formBody = new FormBody.Builder()
                        .add("id", personId)
                        .add("email", personEmail)
                        .build();

                OkHttpClient google_client = new OkHttpClient();

                Request request = new Request.Builder()
                        .url("http://3.36.255.141/google_signup.php")
                        .post(formBody)
                        .build();


                google_client.newCall(request).enqueue(new Callback() {
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
                                        Log.d(TAG, "응답실패 : " + response);
                                        Toast.makeText(getApplicationContext(), "네트워크 문제 발생", Toast.LENGTH_SHORT).show();

                                    } else {
                                        // 응답 성공
                                        final String responseData = response.body().string();
                                        if (responseData.equals("성공")) {
                                            Toast.makeText(Login.this, "로그인에 성공했습니다.", Toast.LENGTH_LONG).show();
                                            startActivityString(PaymentSelect.class, "myData", "구글로그인");

                                        } else {
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

            } else {
                Log.d(TAG, "아ㅏ 실패야 임마!!");
            }
        } catch (ApiException e) {
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }


    // 문자열 인텐트 전달 함수
    public void startActivityString(Class c, String name, String sendString) {
        Intent intent = new Intent(getApplicationContext(), c);
        MyData myData = new MyData(sendString, tableFromDB, null, false, false, 0);
        intent.putExtra(name, myData);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // 인텐트 화면전환 하는 함수
    // FLAG_ACTIVITY_CLEAR_TOP = 불러올 액티비티 위에 쌓인 액티비티 지운다.
    public void startActivityTableList(Class c, ArrayList tableList, String tableListName,String id) {
        Intent intent = new Intent(getApplicationContext(), c);
        intent.putExtra(tableListName, tableList);
        intent.putExtra("get_id", id);
        startActivity(intent);
        Log.d(TAG, "startActivityTableList: " + tableList.size());
        // 화면전환 애니메이션 없애기
        overridePendingTransition(0, 0);
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(adminTableList == null || adminTableList.isEmpty()){
            adminTableList = new ArrayList<>();

            TableQuantity tableQuantity = new TableQuantity();

            tableFromDB = tableQuantity.getTableQuantity();
            Log.d(TAG, "onStart table : " + tableFromDB);

            for(int i=1; i<tableFromDB+1; i++){
                adminTableList.add(new AdminTableList("table"+i,
                        null, null, null, null, 0, 0));

                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("adminTableMenu", null);
                    jsonObject.put("adminTablePrice", null);
                    jsonObject.put("adminTableGender", null);
                    jsonObject.put("adminTableGuestNumber", null);

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            Log.d(TAG, "onStart Table Size: " + adminTableList.size());


        }




    }
}