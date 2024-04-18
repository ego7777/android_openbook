package com.example.openbook.retrofit;

public class TokenDTO {
    String result;
    String token;

    public TokenDTO(String result, String token){
        this.result = result;
        this.token = token;
    }

    public String getResult() {
        return result;
    }

    public String getToken() {
        return token;
    }
}
