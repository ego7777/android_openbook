package com.example.openbook;

import com.example.openbook.startActivity.LoginResponseModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface RetrofitService {
    @POST("Login.php")
    @FormUrlEncoded
    Call<LoginResponseModel> requestLogin(@Field("id") String id,
                      @Field("password") String password);
    @POST("CheckIdDuplication.php")
    @FormUrlEncoded
    Call<String> requestIdDuplication(@Field("id") String id);

}
