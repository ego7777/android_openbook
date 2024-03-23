package com.example.openbook.startActivity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.R;
import com.example.openbook.StartActivityEvent;

public class StartActivity extends AppCompatActivity implements StartActivityEvent {

    String TAG = "StartActivity";
    IntroFragment introFragment = new IntroFragment();
    LoginFragment loginFragment = new LoginFragment();
    SignUpFragment signUpFragment = new SignUpFragment();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_activity);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.start_activity_frame_layout, introFragment)
                .commit();
    }

    @Override
    public void moveToLogin() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.start_activity_frame_layout, loginFragment)
                .commit();
    }

    @Override
    public void moveToSignUp() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.start_activity_frame_layout, signUpFragment)
                .commit();
    }
}
