package com.example.openbook.Chatting;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.openbook.Data.OrderList;

import java.net.URL;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    static String DBName = "OpenbookLocal.db";




    public DBHelper(@Nullable Context context, int version) {
        super(context, DBName, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String queryChatting = "CREATE TABLE chattingTable" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "content VARCHAR(2000) not null," +
                "time VARCHAR(8) not null," +
                "sender VARCHAR(10) not null," +
                "receiver VARCAHR(10) not null," +
                "read VARCHAR(4))";

        db.execSQL(queryChatting);


        //menu.class
        String queryMenu = "CREATE TABLE menuListTable" +
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
                "menuPrice INT not null)";

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

    public boolean insertChattingData(String content, String time, String sender, String receiver, String read) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("content", content);
        contentValues.put("time", time);
        contentValues.put("sender", sender);
        contentValues.put("receiver", receiver);
        contentValues.put("read", read);
        long result = db.insert("chattingTable", null, contentValues);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public boolean insertMenuData(String menuName, int menuPrice, String menuImage, int menuType) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("menuName", menuName);
        contentValues.put("menuPrice", menuPrice);
        contentValues.put("menuImage", menuImage);
        contentValues.put("menuType", menuType);

        long result = db.insert("menuListTable", null, contentValues);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public boolean insertAdminData(String tableName, String menuName, int menuQuantity, int menuPrice) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tableName", tableName);
        contentValues.put("menuName", menuName);
        contentValues.put("menuQuantity", menuQuantity);
        contentValues.put("menuPrice", menuPrice);


        long result = db.insert("adminTableList", null, contentValues);
        if (result == -1) {
            return false;
        }
        return true;
    }

    public void addMenu(String tableNumber, String menu, int quantity, int price) {
        SQLiteDatabase db = this.getWritableDatabase();

        String checkMenu = "SELECT * FROM adminTableList " +
                "WHERE tableName = '" + tableNumber + "' AND menuName = '" + menu + "'";

        Cursor cursor = db.rawQuery(checkMenu, null);

        if(cursor.moveToFirst()){
            // 메뉴 이름이 존재하는 경우
            int existingMenuCount = cursor.getInt(3);
            int newMenuCount = existingMenuCount + quantity;

            String query = "UPDATE adminTableList " +
                    "SET menuQuantity = '" + newMenuCount +
                    "' WHERE tableName = '" + tableNumber +"' AND menuName = '" + menu + "'";

            db.execSQL(query);


        }else{
            // 메뉴 이름이 존재하지 않는 경우
            insertAdminData(tableNumber, menu, quantity, price);
        }

    }

    public ArrayList fetchMenuDetails(String tableNumber, ArrayList list){
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM adminTableList " +
                "WHERE tableName = '" + tableNumber + "'";

        Cursor cursor = db.rawQuery(query, null);

        while(cursor.moveToNext()){
            String menuName = cursor.getString(2);
            int menuQuantity = cursor.getInt(3);
            int menuPrice = cursor.getInt(4);

            list.add(new OrderList(1, tableNumber, menuName, menuQuantity, menuPrice));

        }

        return list;

    }




    public void deleteTableData(String tableValue) {
        SQLiteDatabase db = getWritableDatabase();
        String deleteQuery = "DELETE FROM adminTableList"  + " WHERE tableName = '" + tableValue + "'";
        db.execSQL(deleteQuery);
        db.close();
    }





    public Cursor getTableData(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + tableName, null);
        return res;
    }


}
