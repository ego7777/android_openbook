package com.example.openbook;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.openbook.retrofit.RetrofitService;
import com.example.openbook.retrofit.SuccessOrNot;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TableDataManager {

    String TAG = "TableDataManagerTAG";

    public void deleteProfile(String tableName, RetrofitService service, ResultCallback callback){
        Call<SuccessOrNot> call =service.deleteProfile(tableName);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                if(response.isSuccessful()){
                    callback.onResultReceived(response.body().getResult());
                }else{
                    Log.d(TAG, "deleteProfile is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure deleteProfile: " + t.getMessage());
            }
        });

    }

    public void saveChatMessages(String sender, String message,  RetrofitService service, ResultCallback callback){
        Call<SuccessOrNot> call = service.saveChatMessages(sender, message);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<SuccessOrNot> call, @NonNull Response<SuccessOrNot> response) {
                if(response.isSuccessful()){

                }else{
                    Log.d(TAG, "saveChatMessages is not successful");
                }
            }

            @Override
            public void onFailure(@NonNull Call<SuccessOrNot> call, @NonNull Throwable t) {
                Log.d(TAG, "onFailure saveChatMessages: " + t.getMessage());
            }
        });
    }
}
