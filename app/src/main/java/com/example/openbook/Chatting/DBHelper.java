package com.example.openbook.Chatting;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.openbook.Data.MenuList;
import com.example.openbook.Data.OrderList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {

    static String DBName = "OpenbookLocal.db";
    static int DB_VERSION = 2;
    String TAG = "dbHelperTAG";

    public DBHelper(@Nullable Context context) {
        super(context, DBName, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.d(TAG, "dbHelper onCreate");

        try{
            Log.d(TAG, "dbHelper onCreate in try");

            String queryChatting = "CREATE TABLE IF NOT EXISTS chattingTable" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "content VARCHAR(2000) not null," +
                    "time VARCHAR(8) not null," +
                    "sender VARCHAR(10) not null," +
                    "receiver VARCAHR(10) not null," +
                    "read VARCHAR(4))";

            db.execSQL(queryChatting);

            Log.d(TAG, "creating chattingTable");


            //menu.class
            String queryMenu = "CREATE TABLE IF NOT EXISTS menuListTable" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "menuName VARCHAR(20) not null," +
                    "menuPrice INT not null," +
                    "menuImage String not null, " +
                    "menuType INT not null)";

            db.execSQL(queryMenu);

            Log.d(TAG, "creating menuListTable");

            String queryAdmin = "CREATE TABLE IF NOT EXISTS adminTableList" +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "tableName VARCHAR(20) not null," +
                    "menuName VARCHAR(20) not null," +
                    "menuQuantity INT not null, " +
                    "menuPrice INT not null, " +
                    "identifier INT not null)";

            db.execSQL(queryAdmin);

            Log.d(TAG, "creating adminTableList");

        } catch (Exception e){
            Log.d(TAG, "Error creating tables :" + e.getMessage());
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String queryChatting = "DROP TABLE IF EXISTS chattingTable";
        db.execSQL(queryChatting);

        String queryMenu = "DROP TABLE IF EXISTS menuListTable";
        db.execSQL(queryMenu);

        String queryAdmin = "DROP TABLE IF EXISTS adminTableList";
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
        return result != -1;
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
            Log.d(TAG, "insertMenuData insert false");
            return false;
        }
        Log.d(TAG, "insertMenuData insert true");
        return true;
    }

    public boolean insertAdminData(String tableName, String menuName, int menuQuantity, int menuPrice, int identifier) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tableName", tableName);
        contentValues.put("menuName", menuName);
        contentValues.put("menuQuantity", menuQuantity);
        contentValues.put("menuPrice", menuPrice);
        contentValues.put("identifier", identifier);


        long result = db.insert("adminTableList", null, contentValues);
        return result != -1;
    }

    public void addMenu(String tableNumber, String menu, int quantity, int price, int identifier) {
        SQLiteDatabase db = this.getWritableDatabase();

        String checkMenu = "SELECT * FROM adminTableList " +
                "WHERE tableName = '" + tableNumber + "' AND menuName = '" + menu + "'";

        Cursor cursor = db.rawQuery(checkMenu, null);

        if (cursor.moveToFirst()) {
            // 메뉴 이름이 존재하는 경우
            int existingMenuCount = cursor.getInt(3);
            int newMenuCount = existingMenuCount + quantity;

            String query = "UPDATE adminTableList " +
                    "SET menuQuantity = '" + newMenuCount +
                    "' WHERE tableName = '" + tableNumber + "' AND menuName = '" + menu + "'";

            db.execSQL(query);
        } else {
            // 메뉴 이름이 존재하지 않는 경우
            insertAdminData(tableNumber, menu, quantity, price, identifier);
        }

        cursor.close();

    }

    public ArrayList fetchMenuDetails(String tableNumber, ArrayList list) {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "SELECT * FROM adminTableList " +
                "WHERE tableName = '" + tableNumber + "'";

        Cursor cursor = db.rawQuery(query, null);

        while (cursor.moveToNext()) {
            String menuName = cursor.getString(2);
            int menuQuantity = cursor.getInt(3);
            int menuPrice = cursor.getInt(4);
            int identifier = cursor.getInt(5);

            list.add(new OrderList(1, tableNumber, menuName, menuQuantity, menuPrice));

        }

        cursor.close();

        return list;

    }

    public ArrayList getTableData(ArrayList list) {
        SQLiteDatabase db = this.getWritableDatabase();

        // SQL 쿼리 실행하여 원하는 데이터 가져오기
        String[] columns = {"menuName", "menuPrice", "menuImage", "menuType"};
        String[] menuNames = {"소주", "병맥주", "초코에몽", "Idh"};
        String selection = "menuName IN (?, ?, ?, ?)";
        Cursor cursor = db.query("menuListTable", columns, selection, menuNames, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {

                String name = cursor.getString(0);
                Log.d(TAG, "getTableData name : " + name);
                int price = cursor.getInt(1);
                Log.d(TAG, "getTableData price : " + price);
                String image = cursor.getString(2);
                Log.d(TAG, "getTableData image: " + image);
                int type = cursor.getInt(3);
                Log.d(TAG, "getTableData type: " + type);
                list.add(new MenuList(image, name, price, type, 1));
            }
            cursor.close();
        }

        return list;
    }

    public String chattingJson(String tableValue) {
        // SQLite 데이터베이스에서 데이터 가져오기
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM chattingTable WHERE sender = ?";
        Cursor cursor = db.rawQuery(selectQuery, new String[]{tableValue});

        JSONArray jsonArray = new JSONArray();
        if (cursor.moveToFirst()) {
            do {
                JSONObject jsonObject = new JSONObject();
                try {
                    String message = cursor.getString(1);
                    String time = cursor.getString(2);
                    String receiver = cursor.getString(4);

                    jsonObject.put("message", message);
                    jsonObject.put("time", time);
                    jsonObject.put("receiver", receiver);

                    jsonArray.put(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();

        // JSON 형식으로 변환된 데이터 출력 또는 전송 등의 작업 수행
        String jsonData = jsonArray.toString();
        Log.d("ChatData", jsonData);

        return jsonData;

    }

    public void upDateIsRead(String myTable, String otherTable) {
        SQLiteDatabase db = this.getWritableDatabase();
        String updateQuery = "UPDATE chattingTable SET read = '' WHERE receiver = '" + otherTable + "' AND sender = '" + myTable + "' AND read = '1'";
        Log.d(TAG, "upDateIsRead: ");
        db.execSQL(updateQuery);
        Log.d(TAG, "upDateIsRead: done? ");
    }


    public void deleteTableData(String tableValue, String tableListName, String column) {
        SQLiteDatabase db = getWritableDatabase();
        String deleteQuery = "DELETE FROM " + tableListName + " WHERE " + column + " = '" + tableValue + "'";
        db.execSQL(deleteQuery);
        db.close();
    }


    public Cursor getTableData(String tableName) {
        if(isExistTable(tableName)){
            SQLiteDatabase db = this.getWritableDatabase();
            return db.rawQuery("SELECT * FROM " + tableName, null);
        }else{
            return null;
        }
    }

    public boolean isExistTable(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", new String[]{tableName});
        boolean exists = false;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                exists = (cursor.getInt(0) > 0);
            }
            cursor.close();
        }
        return exists;
    }


    public void dropTable(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }


}
