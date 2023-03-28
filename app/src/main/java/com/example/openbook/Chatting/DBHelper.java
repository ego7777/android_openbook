package com.example.openbook.Chatting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    static String DBName = "chattingMessages.db";
    String content = "content";
    String time = "time";
    String sender = "sender";
    String receiver = "receiver";


    public DBHelper(@Nullable Context context, int version) {
        super(context, DBName, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String query="CREATE TABLE chattingTable" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "content VARCHAR(2000) not null," +
                "time VARCHAR(8) not null," +
                "sender VARCHAR(10) not null," +
                "receiver VARCAHR(10) not null," +
                "read VARCHAR(4))";

        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE chattingTable";
        db.execSQL(query);
        onCreate(db);
    }

    public boolean insertData(String content, String time, String sender, String receiver, String read){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("content", content);
        contentValues.put("time", time);
        contentValues.put("sender", sender);
        contentValues.put("receiver", receiver);
        contentValues.put("read", read);
        long result = db.insert("chattingTable", null, contentValues);
        if(result == -1){
            return false;
        }
        return true;
    }


    public Cursor getChattingData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM chattingTable", null);
        return res;
    }



}
