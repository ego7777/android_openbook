package com.example.openbook;

import com.google.gson.annotations.SerializedName;

public class KakaoPayReadyResponseDTO {
    String tid;
    @SerializedName("next_redirect_app_url")
    String nextRedirectAppUrl;

    @SerializedName("next_redirect_mobile_url")
    String nextRedirectMobileUrl;
    @SerializedName("next_redirect_pc_url")
    String nextRedirectPcUrl;
    @SerializedName("android_app_scheme")
    String androidAppScheme;
    @SerializedName("ios_app_scheme")
    String iosAppScheme;
    @SerializedName("created_at")
    String createdAt;

    public String getTid() {
        return tid;
    }

    public String getNextRedirectAppUrl() {
        return nextRedirectAppUrl;
    }

    public String getNextRedirectMobileUrl() {
        return nextRedirectMobileUrl;
    }

    public String getNextRedirectPcUrl() {
        return nextRedirectPcUrl;
    }

    public String getAndroidAppScheme() {
        return androidAppScheme;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
