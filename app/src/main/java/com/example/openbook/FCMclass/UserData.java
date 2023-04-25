package com.example.openbook.FCMclass;

import com.google.android.gms.tasks.Task;

public class UserData {

    public String userID;
    public String fcmToken;


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

}
