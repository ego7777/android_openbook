package com.example.openbook.startActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.openbook.R;
import com.example.openbook.StartActivityEvent;
import com.example.openbook.databinding.FragmentIntroBinding;

public class IntroFragment extends Fragment {
    String TAG = "IntroFragmentTAG";
    Context context;

    FragmentIntroBinding binding;
    StartActivityEvent event;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_intro, container, false);
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

    private void init(){

        Animation[] animations = new Animation[] {
                AnimationUtils.loadAnimation(requireContext(), R.anim.beat1),
                AnimationUtils.loadAnimation(requireContext(), R.anim.beat2),
                AnimationUtils.loadAnimation(requireContext(), R.anim.heart),
                AnimationUtils.loadAnimation(requireContext(), R.anim.beat3),
                AnimationUtils.loadAnimation(requireContext(), R.anim.textshow)
        };

        View[] views = new View[] {
                binding.rate1,
                binding.rate2,
                binding.heart,
                binding.rate3,
                binding.text
        };

        for (int i = 0; i < animations.length; i++) {
            views[i].startAnimation(animations[i]);
        }

        new Handler().postDelayed(() -> event.moveToLogin(), 2500);


    }
}
