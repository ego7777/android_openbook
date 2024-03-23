package com.example.openbook.startActivity;

import android.util.Log;

import com.example.openbook.RetrofitService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.function.Function;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class StartActivityModel {

    String TAG = "StartActivityModel";
    Gson gson = new GsonBuilder().setLenient().create();
    Retrofit retrofit = new Retrofit.Builder().baseUrl("http://3.137.36.159/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    RetrofitService service = retrofit.create(RetrofitService.class);
    public void requestLogin(String id, String password, Function<LoginResponseModel, Void> callback){
       service.requestLogin(id, password).enqueue(new Callback<LoginResponseModel>() {
           @Override
           public void onResponse(Call<LoginResponseModel> call, Response<LoginResponseModel> response) {
               Log.d(TAG, "Login onResponse: " + response.body());
               if(response.isSuccessful()){
                    callback.apply(response.body());
               }
           }

           @Override
           public void onFailure(Call<LoginResponseModel> call, Throwable t) {
               Log.d(TAG, "Login onFailure: " + t.getMessage());
               callback.apply(new LoginResponseModel("failed", "0"));
           }
       });

    }

    public void requestIdDuplication(String id, Function<String, Void> callback){
        Log.d(TAG, "requestIdDuplication Model: " + id);
        service.requestIdDuplication(id).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                Log.d(TAG, "Duplication onResponse: " + response.body());
                if(response.isSuccessful()){
                    callback.apply(response.body());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "Duplication onFailure: " + t.getMessage());
                callback.apply("failed");
            }
        });
    }
}
