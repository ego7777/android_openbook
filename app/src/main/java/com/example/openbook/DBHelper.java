package com.example.openbook;

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
import java.util.HashMap;
import java.util.Map;

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
                    "message VARCHAR(2000) not null," +
                    "time VARCHAR(8) not null," +
                    "sender VARCHAR(10) not null," +
                    "receiver VARCAHR(10) not null," +
                    "isRead VARCHAR(4))";

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
        contentValues.put("message", content);
        contentValues.put("time", time);
        contentValues.put("sender", sender);
        contentValues.put("receiver", receiver);
        contentValues.put("isRead", read);
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

    public ArrayList getTableData(ArrayList list) {
        SQLiteDatabase db = this.getWritableDatabase();

        String[] columns = {"menuName", "menuPrice", "menuImage", "menuType"};
        String[] menuNames = {"소주", "병맥주", "카니미소", "후토마끼"};
        String selection = "menuName IN (?, ?, ?, ?)";
        Cursor cursor = db.query("menuListTable", columns, selection, menuNames, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                int price = cursor.getInt(1);
                String image = cursor.getString(2);
                int type = cursor.getInt(3);
                list.add(new MenuList(image, name, price, type));
            }
            cursor.close();
        }

        return list;
    }

    public String getChatting(String sender) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {"message", "time", "receiver"};
        String selection = "sender = ?";
        String[] selectionArgs = {sender};

        Cursor cursor = db.query("chattingTable", columns, selection, selectionArgs, null, null, null);

        // Map to hold receivers and their messages
        Map<String, JSONArray> messagesByReceiver = new HashMap<>();

        if(cursor.getCount() == 0){
            cursor.close();
            db.close();
            return null;
        }else{
            while (cursor.moveToNext()) {
                try {
                    String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                    String receiver = cursor.getString(cursor.getColumnIndexOrThrow("receiver"));

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", message);
                    jsonObject.put("time", time);

                    if (!messagesByReceiver.containsKey(receiver)) {
                        messagesByReceiver.put(receiver, new JSONArray());
                    }
                    messagesByReceiver.get(receiver).put(jsonObject);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            cursor.close();
            db.close();

            // Convert the map to a JSON object
            JSONObject resultJson = new JSONObject();
            for (Map.Entry<String, JSONArray> entry : messagesByReceiver.entrySet()) {
                try {
                    resultJson.put(entry.getKey(), entry.getValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return resultJson.toString();
        }
    }

    public void upDateIsRead(String myTable, String otherTable) {
        SQLiteDatabase db = this.getWritableDatabase();
        String updateQuery = "UPDATE chattingTable SET isRead = '' WHERE receiver = '" + otherTable + "' AND sender = '" + myTable + "' AND isRead = '1'";
        Log.d(TAG, "upDateIsRead: ");
        db.execSQL(updateQuery);
        Log.d(TAG, "upDateIsRead: done? ");
    }


    public void deleteAllChatMessages() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM chattingTable");
        db.close();
    }

    public void deleteCompletedTableChatMessage(String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        String sql = "DELETE FROM chattingTable WHERE sender = ?";
        db.execSQL(sql, new String[]{tableName});
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
