package com.example.openbook.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Data.TableList;
import com.example.openbook.R;

import java.util.ArrayList;

public class PaymentSelect extends AppCompatActivity {

    String get_id;
    int tableFromDB;

    Button payment_select_after;
    Button payment_select_before;

    String TAG = "paymentSelect_TAG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_select_activity);

        get_id = getIntent().getStringExtra("get_id");
        tableFromDB = getIntent().getIntExtra("tableFromDB", 20);
        Log.d(TAG, "tableFromDB: " + tableFromDB);

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
        intent.putExtra("tableFromDB", tableFromDB);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
