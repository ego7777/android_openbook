package com.example.openbook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.net.URL;
import java.util.HashMap;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private String urlStr;
    private ImageView imageView;
    Context context;
    boolean ticket;

    String TAG = "ImageLoadTask";

    private static HashMap<String, Bitmap> bitmapHash = new HashMap<String, Bitmap>();

    public ImageLoadTask(Context context, boolean ticket, String urlStr, ImageView imageView) {
        this.urlStr = urlStr;
        this.imageView = imageView;
        this.context = context;
        this.ticket = ticket;
        Log.d(TAG, "ImageLoadTask ticket :" + ticket);
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        Bitmap bitmap = null;
        try {
            if (bitmapHash.containsKey(urlStr)) {

                Bitmap oldbitmap = bitmapHash.remove(urlStr);

                if (oldbitmap != null) {
//                   oldbitmap.recycle();
//                   oldbitmap = null;
                }
            }

            URL url = new URL(urlStr);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            bitmapHash.put(urlStr, bitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        BlurImage blurImage = new BlurImage();

        Log.d(TAG, "onPostExecute: " + ticket);

        if (ticket == false) {
            imageView.setImageBitmap(blurImage.blur(context, bitmap));
        } else {
            imageView.setImageBitmap(bitmap);
        }

        imageView.invalidate();
    }
}
