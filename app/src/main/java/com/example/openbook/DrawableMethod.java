package com.example.openbook;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayOutputStream;


public class DrawableMethod {

    String TAG = "DrawableToBitmap";

    public byte[] makeBitmap(Drawable drawable){
        Bitmap bitmap = null;

        try{
            if(drawable instanceof BitmapDrawable){
                Log.d(TAG, "makeBitmap: first if");
                BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;

                if(bitmapDrawable.getBitmap() != null){
                    Log.d(TAG, "makeBitmap: inner first if ");
                    return bitmapToByteArray(bitmapDrawable.getBitmap());
                }
            }

            if(drawable.getIntrinsicWidth()<=0 || drawable.getIntrinsicHeight() <= 0){
                Log.d(TAG, "makeBitmap: second if");
                bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);

            }else{
                Log.d(TAG, "makeBitmap: second else");
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            }

            Canvas canvas = new Canvas(bitmap);
            Log.d(TAG, "makeBitmap: canvas");
            drawable.setBounds(0, 0, canvas.getWidth(),canvas.getHeight());
            drawable.draw(canvas);

        }catch (Exception e){
            e.printStackTrace();
        }
        Log.d(TAG, "return bitmap : " + bitmap);

        return bitmapToByteArray(bitmap);
    }

    public byte[] bitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        return byteArray;

    }

    public Bitmap byteArrayToBitmap(byte[] byteArray){
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        return bitmap;
    }
}
