package com.example.openbook.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.R;

public class PaymentSelect extends AppCompatActivity {

    String get_id;

    Button payment_select_after;
    Button payment_select_before;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_select_activity);

        get_id = getIntent().getStringExtra("get_id");

        payment_select_after = findViewById(R.id.payment_select_after);
        payment_select_before = findViewById(R.id.payment_select_before);

    }

    @Override
    protected void onResume() {
        super.onResume();


        payment_select_after.setOnClickListener(view ->{
            startActivityString("after");
        });

        payment_select_before.setOnClickListener(view ->{
            startActivityString("before");
        });
    }

    public void startActivityString(String paymentStyle){
        Intent intent = new Intent(PaymentSelect.this, Menu.class);
        intent.putExtra("get_id", get_id);
        intent.putExtra("paymentStyle", paymentStyle);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
