package com.example.openbook.retrofit;

import com.example.openbook.kakaopay.KakaoPayApproveResponseDTO;
import com.example.openbook.kakaopay.KakaoPayReadyResponseDTO;

import java.util.HashMap;


import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @POST("GetTableList.php")
    @FormUrlEncoded
    Call<TableListDTO> getTableList(@Field("admin") String admin);

    @POST("SetTableList.php")
    @FormUrlEncoded
    Call<TableListDTO> setTableList(@Field("table_count") int tableCount);

    @POST("TableInfoCheck.php")
    @FormUrlEncoded
    Call<TableInformationDTO> getTableImage(@Field("tableName") String table);

    Call<SuccessOrNot> sendGiftOtherTable(@Field("to") String to,
                                          @Field("from") String from,
                                          @Field("menuName") String menuName,
                                          @Field("menuQuantity") int menuQuantity,
                                          @Field("menuPrice") int menuPrice);

    @POST("v1/payment/ready")
    @FormUrlEncoded
    Call<KakaoPayReadyResponseDTO> createPaymentRequest(@Header("Authorization") String apiKey,
                                                        @FieldMap HashMap<String, String> request);

    @POST("v1/payment/approve")
    @FormUrlEncoded
    Call<KakaoPayApproveResponseDTO> requestApprovedPayment(@Header("Authorization") String apiKey,
                                                            @FieldMap HashMap<String, String> request);
    @POST("SavePayment.php")
    @FormUrlEncoded
    Call<SuccessOrNot> savePayment(@Field("tid") String tid,
                                   @Field("transaction_date") String approvedAt,
                                   @Field("total_amount") int totalAmount,
                                   @Field("identifier") int identifier,
                                   @Field("payment_method_type") int paymentMethodType,
                                   @Field("order_items") String orderList);

    @POST("RegisterFcmToken.php")
    @FormUrlEncoded
    Call<SuccessOrNot> saveFcmToken(@Field("identifier") int identifier,
                                    @Field("token") String token);
    @POST("TableInfoCheck.php")
    @FormUrlEncoded
    Call<AdminTableDTO> requestTableInfo(@Field("tableName") String tableName);

    @POST("SendFcmRequest.php")
    @FormUrlEncoded
    Call<SuccessOrNot> sendRequestFcm(@Field("userId") String userId,
                                      @Field("data") String data);
    @POST("Sales.php")
    @FormUrlEncoded
    Call<SalesDTO> requestSalesData(@Field("duration") String duration,
                                    @Field("current_date") String currentDate);
    @POST("SalesItemAnalyzer.php")
    @FormUrlEncoded
    Call<SalesItemDTO> requestSalesItems(@Field("duration") String duration,
                                            @Field("current_date") String currentDate,
                                            @Field("standard") String standard);
}
