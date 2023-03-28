package com.example.openbook;

import android.os.AsyncTask;

import okhttp3.MediaType;

public class SendNotification {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static void sendNotification(String token, String title, String message){
        new AsyncTask<Void, Void, Void>(){

            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }
        };
    }
}
