package com.example.openbook;

import com.example.openbook.startActivity.LoginResponseModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface RetrofitService {
    @POST("Login.php")
    @FormUrlEncoded
    Call<LoginResponseModel> requestLogin(@Field("identifier") int identifier,
                      @Field("password") int password);
    @POST("CheckIdDuplication.php")
    @FormUrlEncoded
    Call<SuccessOrNot> requestIdDuplication(@Field("identifier") int identifier);

    @POST("SignUp.php")
    @FormUrlEncoded
    Call<SuccessOrNot> requestSignUp(@Field("id") String id,
                               @Field("identifier") int identifier,
                               @Field("password") int password,
                               @Field("phone") String phone,
                               @Field("email") String email);
    @GET("GetMenuList.php")
    Call<MenuListDTO> getMenuList();

}
