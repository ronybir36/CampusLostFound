package com.example.lostfound.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Converts photos to and from Base64 text so they can be stored in the
 * Realtime Database. Photos are shrunk first to keep each record small
 * (Firebase Storage needs a paid plan, so this project avoids it).
 */
public final class ImageUtils {

    private static final int MAX_SIDE_PX = 480;   // stored size
    private static final int JPEG_QUALITY = 60;
    private static final int DECODE_MAX_PX = 1024; // avoids OutOfMemory on big gallery photos

    private ImageUtils() { }

    /** Shrinks the bitmap and returns it as a Base64 JPEG string. */
    public static String toBase64(Bitmap bitmap) {
        Bitmap scaled = scaleDown(bitmap);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out);
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    /** Returns the bitmap for a stored Base64 string, or null if it is empty or broken. */
    public static Bitmap fromBase64(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = Base64.decode(data, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Loads a gallery image at a reduced size. Throws IOException if it cannot be read. */
    public static Bitmap fromUri(Context context, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Cannot open the selected image");
            }
            BitmapFactory.decodeStream(in, null, bounds);
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = calculateSampleSize(bounds.outWidth, bounds.outHeight);
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            if (in == null) {
                throw new IOException("Cannot open the selected image");
            }
            return BitmapFactory.decodeStream(in, null, options);
        }
    }

    private static int calculateSampleSize(int width, int height) {
        int sample = 1;
        while (Math.max(width, height) / (sample * 2) >= DECODE_MAX_PX) {
            sample *= 2;
        }
        return sample;
    }

    private static Bitmap scaleDown(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int largest = Math.max(width, height);
        if (largest <= MAX_SIDE_PX) {
            return source;
        }
        float ratio = (float) MAX_SIDE_PX / largest;
        return Bitmap.createScaledBitmap(source,
                Math.round(width * ratio),
                Math.round(height * ratio),
                true);
    }
}
