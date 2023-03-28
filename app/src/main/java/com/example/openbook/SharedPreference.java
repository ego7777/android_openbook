package com.example.openbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SharedPreference  {

    public static final String SprefsName = "Menu";

    private static final String DEFAULT_VALUE_STRING = "";
    private static final boolean DEFAULT_VALUE_BOOLEAN = false;
    private static final int DEFAULT_VALUE_INT = -1;
    private static final long DEFAULT_VALUE_LONG = -1L;
    private static final float DEFAULT_VALUE_FLOAT = -1F;

    /**
     * 쉐어드 프리퍼런스 파일을 생성해주자.
     * getSharedPreferences 소괄호 안에는 파일 이름과 모드를 지정한다.
     * */
    private static SharedPreferences getSprefs(Context context) {
        return context.getSharedPreferences(SprefsName, Context.MODE_PRIVATE);
    }

    /**
     * String 값 저장
     * 에디터를 사용해야 쉐어드 프리퍼런스 파일에 값을 넣어 줄 수 있다.
     * 커밋을 해주어야 저장이 된다.
     * 그래서 작성이 끝나고나면 꼭 커밋을 해주어야한다.
     */

    public static void setString(Context context, String key, String value) {

        SharedPreferences sprefs = getSprefs(context);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putString(key, value);
        editor.commit();

    }



    /**
     * boolean 값 저장
     */

    public static void setBoolean(Context context, String key, boolean value) {

        SharedPreferences sprefs = getSprefs(context);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putBoolean(key, value);
        editor.commit();

    }



    /**
     * int 값 저장
     */

    public static void setInt(Context context, String key, int value) {

        SharedPreferences sprefs = getSprefs(context);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putInt(key, value);
        editor.commit();

    }



    /**
     * long 값 저장
     */

    public static void setLong(Context context, String key, long value) {

        SharedPreferences sprefs = getSprefs(context);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putLong(key, value);
        editor.commit();

    }



    /**
     * float 값 저장
     */

    public static void setFloat(Context context, String key, float value) {

        SharedPreferences sprefs = getSprefs(context);
        SharedPreferences.Editor editor = sprefs.edit();
        editor.putFloat(key, value);
        editor.commit();

    }



    /**
     * String 값 로드
     */

    public static String getString(Context context, String key) {

        SharedPreferences sprefs = getSprefs(context);
        String value = sprefs.getString(key, DEFAULT_VALUE_STRING);
        return value;

    }



    /**
     * boolean 값 로드
     */

    public static boolean getBoolean(Context context, String key) {

        SharedPreferences sprefs = getSprefs(context);
        boolean value = sprefs.getBoolean(key, DEFAULT_VALUE_BOOLEAN);
        return value;

    }



    /**
     * int 값 로드
     */

    public static int getInt(Context context, String key) {

        SharedPreferences sprefs = getSprefs(context);
        int value = sprefs.getInt(key, DEFAULT_VALUE_INT);
        return value;

    }



    /**
     * long 값 로드
     */

    public static long getLong(Context context, String key) {

        SharedPreferences sprefs = getSprefs(context);

        long value = sprefs.getLong(key, DEFAULT_VALUE_LONG);

        return value;

    }



    /**
     * float 값 로드
     */

    public static float getFloat(Context context, String key) {

        SharedPreferences sprefs = getSprefs(context);
        float value = sprefs.getFloat(key, DEFAULT_VALUE_FLOAT);
        return value;

    }



    /**
     * 키 값 삭제
     */

    public static void removeKey(Context context, String key) {

        SharedPreferences sprefs = getSprefs(context);
        SharedPreferences.Editor edit = sprefs.edit();
        edit.remove(key);
        edit.commit();

    }



    /**
     * 모든 저장 데이터 삭제
     */

    public static void clear(Context context) {

        SharedPreferences sprefs = getSprefs(context);
        SharedPreferences.Editor edit = sprefs.edit();
        edit.clear();
        edit.commit();

    }

}







