package com.example.openbook.Chatting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

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

        String queryMenu = "CREATE TABLE menuListTable"+
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "menuName VARCHAR(20) not null," +
                "menuPrice INT not null," +
                "menuImage INT not null)";

        db.execSQL(queryMenu);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String queryChatting = "DROP TABLE chattingTable";
        db.execSQL(queryChatting);

        String queryMenu = "DROP TABLE menuListTable";
        db.execSQL(queryMenu);

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

    public boolean insertMenuData(String menuName, int menuPrice, int menuImage){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("menuName", menuName);
        contentValues.put("menuPrice", menuPrice);
        contentValues.put("menuImage", menuImage);

        long result = db.insert("menuListTable", null, contentValues);
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
