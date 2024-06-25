package com.example.openbook;

import com.example.openbook.retrofit.RetrofitManager;
import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.TableListDTO;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class TableQuantity {
    String TAG = "TableQuantityTAG";

    int tableQuantity;

    public void getTableQuantity(Callback<TableListDTO> callback)  {
        RetrofitManager retrofitManager = new RetrofitManager();
        Retrofit retrofit = retrofitManager.getRetrofit(BuildConfig.SERVER_IP);
        RetrofitService service = retrofit.create(RetrofitService.class);

        Call<TableListDTO> call = service.getTableList(BuildConfig.ADMIN_KEY);
        call.enqueue(callback);
    }


    public void setTableQuantity(int tableQuantity) {

    }
}
