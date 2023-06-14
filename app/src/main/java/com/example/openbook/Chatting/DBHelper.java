package com.example.openbook.Chatting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import java.net.URL;

public class DBHelper extends SQLiteOpenHelper {

    static String DBName = "OpenbookLocal.db";


    public DBHelper(@Nullable Context context, int version) {
        super(context, DBName, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String queryChatting="CREATE TABLE chattingTable" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "content VARCHAR(2000) not null," +
                "time VARCHAR(8) not null," +
                "sender VARCHAR(10) not null," +
                "receiver VARCAHR(10) not null," +
                "read VARCHAR(4))";

        db.execSQL(queryChatting);


        //menu.class
        String queryMenu = "CREATE TABLE menuListTable"+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "menuName VARCHAR(20) not null," +
                "menuPrice INT not null," +
                "menuImage INT not null, " +
                "menuType INT not null)";

        db.execSQL(queryMenu);

        String queryAdmin = "CREATE TABLE adminTableList" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tableName VARCHAR(20) not null," +
                "menuName VARCHAR(20) not null," +
                "menuQuantity INT not null, " +
                "menuPRice INT not null)";

        db.execSQL(queryAdmin);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String queryChatting = "DROP TABLE chattingTable";
        db.execSQL(queryChatting);

        String queryMenu = "DROP TABLE menuListTable";
        db.execSQL(queryMenu);

        String queryAdmin = "DROP TABLE adminTableList";
        db.execSQL(queryAdmin);

        onCreate(db);
    }

    public boolean insertChattingData(String content, String time, String sender, String receiver, String read){
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

    public boolean insertMenuData(String menuName, int menuPrice, String menuImage, int menuType){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("menuName", menuName);
        contentValues.put("menuPrice", menuPrice);
        contentValues.put("menuImage", menuImage);
        contentValues.put("menuType", menuType);

        long result = db.insert("menuListTable", null, contentValues);
        if(result == -1){
            return false;
        }
        return true;
    }

    public boolean insetAdminData(String tableName, String menuName, int menuQuantity, int menuPrice){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tableName", tableName);
        contentValues.put("menuName", menuName);
        contentValues.put("menuQuantity",menuQuantity);
        contentValues.put("menuPrice", menuPrice);

        long result = db.insert("adminTableList", null, contentValues);
        if(result == -1){
            return false;
        }
        return true;
    }


    public Cursor getTableData(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+tableName, null);
        return res;
    }



}
