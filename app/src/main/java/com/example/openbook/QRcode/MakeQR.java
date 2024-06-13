package com.example.openbook.QRcode;

import android.graphics.Bitmap;

import com.example.openbook.BuildConfig;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MakeQR {
    
    public Bitmap clientQR(String get_id){

        String text = BuildConfig.SERVER_IP + "WriteTableInfo.php?table="+get_id;

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        BitMatrix bitMatrix = null;

        {
            try {
                bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,500,500);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return bitmap;
    }

    public Bitmap adminQr(){

        String url = BuildConfig.SERVER_IP + "RegisterMenu.php";

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        BitMatrix bitMatrix = null;

        {
            try {
                bitMatrix = multiFormatWriter.encode(url, BarcodeFormat.QR_CODE,200,200);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return bitmap;
    }
    

}



