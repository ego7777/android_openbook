package com.example.openbook.startActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.openbook.DialogCustom;
import com.example.openbook.R;
import com.example.openbook.StartActivityEvent;

public class LoginFragment extends Fragment {

    String TAG = "LoginFragmentTAG";
    Context context;

    StartActivityEvent event;
    FragmentLoginBinding binding;
    StartActivityViewModel startActivityViewModel;
    DialogCustom dialogCustom;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof StartActivityEvent) {
            event = (StartActivityEvent) context;
            Log.d(TAG, "onAttach: 호출");
        } else {
            throw new RuntimeException(context.toString() + " must implement MainActivityEvent");
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void init() {
        startActivityViewModel = new ViewModelProvider(this).get(StartActivityViewModel.class);
        binding.setStartActivityViewModel(startActivityViewModel);
        binding.setLifecycleOwner(this);


        startActivityViewModel.getCheckLoginBlank().observe(getViewLifecycleOwner(), it -> {
            if (!it) {
                dialogCustom = new DialogCustom();
                dialogCustom.showAlertDialog(context, "아이디와 비밀번호를 모두 입력해주세요.");
            }
        });

        binding.loginButton.setOnClickListener(view -> {
            String id = binding.loginEditTextId.getText().toString().trim();
            String password = binding.loginEditTextPw.getText().toString().trim();
            Log.d(TAG, "id: " + id + "\n pw: " + password);

            startActivityViewModel.checkLoginBlank(id, password);

        });

        binding.signupButton.setOnClickListener(view -> {
            event.moveToSignUp();
        });

        /**
         * 구글 로그인
         */
        binding.googleLogin.setOnClickListener(view -> {

        });
    }


}
