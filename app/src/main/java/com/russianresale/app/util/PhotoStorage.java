package com.russianresale.app.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * Копирует выбранные в галерее фото во внутреннюю память приложения
 * (с уменьшением до 1600 px), чтобы они не пропали, если пользователь удалит оригинал.
 * Вызывать в фоновом потоке.
 */
public final class PhotoStorage {
    private static final int MAX_SIDE = 1600;

    private PhotoStorage() {
    }

    public static String save(Context context, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        int sample = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / sample > MAX_SIDE * 2) sample *= 2;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        Bitmap bitmap;
        try (InputStream in = context.getContentResolver().openInputStream(uri)) {
            bitmap = BitmapFactory.decodeStream(in, null, options);
        }
        if (bitmap == null) throw new IOException("Не удалось прочитать изображение");
        int max = Math.max(bitmap.getWidth(), bitmap.getHeight());
        if (max > MAX_SIDE) {
            float k = MAX_SIDE / (float) max;
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap,
                    Math.round(bitmap.getWidth() * k), Math.round(bitmap.getHeight() * k), true);
            if (scaled != bitmap) bitmap.recycle();
            bitmap = scaled;
        }
        File dir = new File(context.getFilesDir(), "photos");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Нет доступа к памяти");
        File file = new File(dir, UUID.randomUUID() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        }
        bitmap.recycle();
        return file.getAbsolutePath();
    }
}
