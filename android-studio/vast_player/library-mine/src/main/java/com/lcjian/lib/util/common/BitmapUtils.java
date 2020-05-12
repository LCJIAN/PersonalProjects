package com.lcjian.lib.util.common;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ImageUtils
 *
 * @author LCJIAN
 */
public class BitmapUtils {

    /**
     * 从view 得到图片
     *
     * @param view
     * @return
     */
    public static Bitmap getBitmapFromView(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        return view.getDrawingCache(true);
    }

    /**
     * convert Bitmap to byte array
     *
     * @param b
     * @return
     */
    public static byte[] bitmapToByte(Bitmap b) {
        if (b == null) {
            return null;
        }
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 100, o);
        byte[] result = null;
        result = o.toByteArray();
        try {
            o.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * scale image
     *
     * @param org
     * @param newWidth
     * @param newHeight
     * @return
     */
    public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
        return scaleImage(org, (float) newWidth / org.getWidth(), (float) newHeight / org.getHeight());
    }

    /**
     * scale image
     *
     * @param org
     * @param scaleWidth  sacle of width
     * @param scaleHeight scale of height
     * @return
     */
    public static Bitmap scaleImage(Bitmap org, float scaleWidth, float scaleHeight) {
        if (org == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(org, 0, 0, org.getWidth(), org.getHeight(), matrix, true);
    }

    /**
     * Round the corners of a {@link Bitmap}
     *
     * @param source
     * @param radius
     * @return rounded corner bitmap
     */
//    public static Bitmap roundCorners(final Bitmap source, float radius) {
//        int width = source.getWidth();
//        int height = source.getHeight();
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setColor(Color.WHITE);
//        Bitmap clipped = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//        Canvas canvas = new Canvas(clipped);
//        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
//        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.DST_IN));
//        Bitmap rounded = Bitmap.createBitmap(width, height, Config.ARGB_8888);
//        canvas = new Canvas(rounded);
//        canvas.drawBitmap(source, 0, 0, null);
//        canvas.drawBitmap(clipped, 0, 0, paint);
//        source.recycle();
//        clipped.recycle();
//        return rounded;
//    }

    /**
     * Round the corners of a {@link Bitmap}
     *
     * @param source
     * @param radius
     * @return rounded corner bitmap
     */
    public static Bitmap roundCorners(Bitmap source, float radius) {
        int width = source.getWidth();
        int height = source.getHeight();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        Bitmap clipped = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(clipped);
        canvas.drawRoundRect(new RectF(0, 0, width, height), radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, 0, 0, paint);
        source.recycle();
        return clipped;
    }

    public static Bitmap mergeBitmap(Bitmap background, Bitmap foreground) {
        int w = background.getWidth() > foreground.getWidth() ? background.getWidth() : foreground.getWidth();
        int h = background.getHeight() > foreground.getHeight() ? background.getHeight() : foreground.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(background, 0, 0, null);
        canvas.drawBitmap(foreground, 0, 0, null);
        background.recycle();
        foreground.recycle();
        return bitmap;
    }

    public static Bitmap mergeBitmap(int firstResId, int secondResId, Resources res) {
        Bitmap firstBitmap = BitmapFactory.decodeResource(res, firstResId);
        Bitmap secondBitmap = BitmapFactory.decodeResource(res, secondResId);
        Bitmap result = mergeBitmap(firstBitmap, secondBitmap);
        firstBitmap.recycle();
        secondBitmap.recycle();
        return result;
    }

    /**
     * Decode and sample down a bitmap from a file to the requested width and height.
     *
     * @param filename  The full path of the file to decode
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return A bitmap sampled down from the original with the same aspect ratio and dimensions
     * that are equal to or greater than the requested width and height
     */
    public static Bitmap decodeSampledBitmapFromFile(String filename,
                                                     int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filename, options);
    }

    /**
     * Calculate an inSampleSize for use in a {@link BitmapFactory.Options} object when decoding
     * bitmaps using the decode* methods from {@link BitmapFactory}. This implementation calculates
     * the closest inSampleSize that is a power of 2 and will result in the final decoded bitmap
     * having a width and height equal to or larger than the requested width and height.
     *
     * @param options   An options object with out* params already populated (run through a decode*
     *                  method with inJustDecodeBounds==true
     * @param reqWidth  The requested width of the resulting bitmap
     * @param reqHeight The requested height of the resulting bitmap
     * @return The value to be used for inSampleSize
     */
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            // This offers some additional logic in case the image has a strange
            // aspect ratio. For example, a panorama may have a much larger
            // width than height. In these cases the total pixels might still
            // end up being too large to fit comfortably in memory, so we should
            // be more aggressive with sample down the image (=larger inSampleSize).

            long totalPixels = width * height / inSampleSize;

            // Anything more than 2x the requested pixels we'll sample down further
            final long totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels > totalReqPixelsCap) {
                inSampleSize *= 2;
                totalPixels /= 2;
            }
        }
        return inSampleSize;
    }
}