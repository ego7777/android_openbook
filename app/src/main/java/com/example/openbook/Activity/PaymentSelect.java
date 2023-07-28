package com.example.openbook.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Data.MyData;
import com.example.openbook.Data.TableList;
import com.example.openbook.R;
import com.example.openbook.TableQuantity;

import java.util.ArrayList;
import java.util.Random;

public class PaymentSelect extends AppCompatActivity {

    MyData myData;

    Button payment_select_after;
    Button payment_select_before;

    String TAG = "paymentSelectTAG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_select_activity);

        myData = (MyData) getIntent().getSerializableExtra("myData");
        Log.d(TAG, "myData id: " + myData.getId());

        SharedPreferences pref = getSharedPreferences("cart_list", MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        editor.remove("order_list");

        if(myData.getTableFromDB() == 0){
            Log.d(TAG, "tableFromDB ZERO: ");
            TableQuantity tableQuantity = new TableQuantity();
            int tableFromDB = tableQuantity.getTableQuantity();
            myData.setTableFromDB(tableFromDB);
            Log.d(TAG, "tableFromDB: " + myData.getTableFromDB());
        }

        payment_select_after = findViewById(R.id.payment_select_after);
        payment_select_before = findViewById(R.id.payment_select_before);



    }

    @Override
    protected void onResume() {
        super.onResume();


        payment_select_after.setOnClickListener(view ->{
            startActivityString("after", 0);
        });

        payment_select_before.setOnClickListener(view ->{
            int identifier = hashCode();
            Log.d(TAG, "identifier: " + identifier);

            startActivityString("before", identifier);
        });
    }

    public void startActivityString(String paymentStyle, int identifier){
        Intent intent = new Intent(PaymentSelect.this, Menu.class);
        myData.setPaymentStyle(paymentStyle);
        myData.setIdentifier(identifier);
        intent.putExtra("myData", myData);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
