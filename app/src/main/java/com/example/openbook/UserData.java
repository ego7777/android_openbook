package com.example.openbook;

import com.google.android.gms.tasks.Task;

public class UserData {

    public String userID;
    public String fcmToken;
//    public int chattingRequest;


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

//    public int getChattingRequest() {
//        return chattingRequest;
//    }
//
//    public void setChattingRequest(int chattingRequest) {
//        this.chattingRequest = chattingRequest;
//    }
}
