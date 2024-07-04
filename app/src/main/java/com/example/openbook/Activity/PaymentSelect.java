package com.example.openbook.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.openbook.Data.MyData;
import com.example.openbook.Category.PaymentCategory;
import com.example.openbook.R;
import com.example.openbook.TableDataManager;
import com.example.openbook.retrofit.TableListDTO;
import com.example.openbook.TableQuantity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentSelect extends AppCompatActivity {

    MyData myData;
    Button paymentSelectLater, paymentSelectNow;
    String TAG = "paymentSelectTAG";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment_select_activity);

        myData = (MyData) getIntent().getSerializableExtra("myData");

        SharedPreferences pref = getSharedPreferences("CustomerData", MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        TableDataManager tableDataManager = new TableDataManager();
        tableDataManager.hideSystemUI(this);

        editor.remove("orderedItems");
        editor.remove("cartItems");
        editor.remove("profileTicket");
        editor.remove("activeTableList");
        editor.commit();


        if(myData.getTableFromDB() == 0){
            Log.d(TAG, "tableFromDB ZERO: ");
            getTableFromDB();
            Log.d(TAG, "tableFromDB: " + myData.getTableFromDB());
        }

        paymentSelectLater = findViewById(R.id.payment_select_later);
        paymentSelectNow = findViewById(R.id.payment_select_now);

    }

    public void getTableFromDB(){
        TableQuantity tableQuantity = new TableQuantity();
        tableQuantity.getTableQuantity(new Callback<>() {
            @Override
            public void onResponse(Call<TableListDTO> call, Response<TableListDTO> response) {
                if (response.isSuccessful()) {
                    switch (response.body().getResult()) {
                        case "success":
                            int tableFromDB = response.body().getTableCount();
                            myData.setTableFromDB(tableFromDB);
                            break;
                        case "failed":
                            Log.d(TAG, "onResponse table failed: ");
                    }
                } else {
                    Log.d(TAG, "onResponse table isNotSuccessful");
                }
            }

            @Override
            public void onFailure(Call<TableListDTO> call, Throwable t) {
                Log.d(TAG, "onFailure table: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        paymentSelectLater.setOnClickListener(view ->{
            startActivityString(PaymentCategory.LATER, myData.getId().hashCode());
        });

        paymentSelectNow.setOnClickListener(view ->{
            int identifier = hashCode();
            Log.d(TAG, "identifier: " + identifier);

            startActivityString(PaymentCategory.NOW, identifier);
        });
    }

    public void startActivityString(PaymentCategory paymentStyle, int identifier){
        Intent intent = new Intent(PaymentSelect.this, Menu.class);
        myData.setPaymentCategory(paymentStyle);
        myData.setIdentifier(identifier);
        intent.putExtra("myData", myData);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}
