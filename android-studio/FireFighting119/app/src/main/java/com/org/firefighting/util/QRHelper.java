package com.org.firefighting.util;

import android.graphics.Bitmap;

import androidx.annotation.Nullable;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Hashtable;

public class QRHelper {

    @Nullable
    public static Result scan(Bitmap bitmap) {
        Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
        Bitmap scanBitmap = Bitmap.createBitmap(bitmap);

        int[] px = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(px, 0, scanBitmap.getWidth(), 0, 0,
                scanBitmap.getWidth(), scanBitmap.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(
                scanBitmap.getWidth(), scanBitmap.getHeight(), px);
        BinaryBitmap tempBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(tempBitmap, hints);
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
        return null;
    }
}
