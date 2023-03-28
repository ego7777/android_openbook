package com.example.openbook.QRcode;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class MakeQR {
    
    public Bitmap myQR(String get_id){
        String text = "http://3.36.255.141/tableInfo.php?table="+get_id;

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();

        BitMatrix bitMatrix = null;

        {
            try {
                bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,200,200);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }

        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
        return bitmap;
    }
    

}



