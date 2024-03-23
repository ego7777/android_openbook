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

import com.example.openbook.R;
import com.example.openbook.StartActivityEvent;
import com.example.openbook.databinding.FragmentSignupBinding;

public class SignUpFragment extends Fragment {
    String TAG = "SignUpFragmentTAG";
    Context context;
    StartActivityEvent event;
    FragmentSignupBinding binding;
    StartActivityViewModel startActivityViewModel;

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    public void init(){
        startActivityViewModel = new ViewModelProvider(this).get(StartActivityViewModel.class);
        binding.setViewModel(startActivityViewModel);
        binding.setLifecycleOwner(this);

        startActivityViewModel.getSignUpIdWarning().observe(getViewLifecycleOwner(), it ->{
            if(it != null){
                binding.signupIdOverlapCk.setVisibility(View.VISIBLE);
            }else{
                binding.signupIdOverlapCk.setVisibility(View.INVISIBLE);
            }
        });


        binding.signupOverlap.setOnClickListener(view ->{
            String id = binding.signupId.getText().toString().trim();
            Log.d(TAG, "id: " + id);

            startActivityViewModel.checkIdDuplication(id);
        });
    }
}
