package com.example.openbook.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.R;

public class Intro extends AppCompatActivity {

    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_intro);

        ImageView rate1 = findViewById(R.id.rate1);
        ImageView rate2 = findViewById(R.id.rate2);
        ImageView heart = findViewById(R.id.heart);
        ImageView rate3 = findViewById(R.id.rate3);
        TextView text = findViewById(R.id.text);



        Animation anim1 = AnimationUtils.loadAnimation(Intro.this, R.anim.beat1);
        Animation anim2 = AnimationUtils.loadAnimation(Intro.this, R.anim.beat2);
        Animation anim3 = AnimationUtils.loadAnimation(Intro.this, R.anim.heart);
        Animation anim4 = AnimationUtils.loadAnimation(Intro.this, R.anim.beat3);
        Animation text_anim = AnimationUtils.loadAnimation(Intro.this, R.anim.textshow);

        rate1.startAnimation(anim1);
        rate2.startAnimation(anim2);
        heart.startAnimation(anim3);
        rate3.startAnimation(anim4);
        text.startAnimation(text_anim);



        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Intro.this, Login.class);
                startActivity(intent);
                finish();
            }
        }, 2500);


    }
}
