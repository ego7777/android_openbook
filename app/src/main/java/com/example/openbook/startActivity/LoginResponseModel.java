package com.example.openbook.startActivity;

public class LoginResponseModel {

    String result;
    String id;


    public LoginResponseModel(String result, String id){
        this.result = result;
        this.id= id;
    }

    public String getResult() {
        return result;
    }

    public String getId() {
        return id;
    }
}
