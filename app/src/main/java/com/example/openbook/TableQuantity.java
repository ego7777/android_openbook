package com.example.openbook;

import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;

import java.io.IOException;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TableQuantity {
    String TAG = "TableQuantityTAG";


    int tableQuantity;

    public int getTableQuantity()  {
        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        RetrofitService service = retrofit.create(RetrofitService.class);

        Call<TableListDTO> call = service.getTableList(BuildConfig.ADMIN_KEY);
        call.enqueue(new Callback<TableListDTO>() {
            @Override
            public void onResponse(Call<TableListDTO> call, Response<TableListDTO> response) {
                Log.d(TAG, "onResponse table: " + response.body());
                if(response.isSuccessful()){
                    switch (response.body().getResult()){
                        case "success" :
                            tableQuantity = response.body().getTableCount();
                            break;
                        case "failed" :
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<TableListDTO> call, Throwable t) {

            }
        });

        return tableQuantity;
    }


    public void setTableQuantity(int tableQuantity) {



    }
}
