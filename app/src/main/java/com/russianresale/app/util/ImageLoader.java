package com.russianresale.app.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.russianresale.app.R;
import com.russianresale.app.data.model.Category;

import java.io.File;

/**
 * Загрузка картинок. Фото продавцов — файлы во внутренней памяти (через Glide),
 * демо-фото задаются строкой «demo:КАТЕГОРИЯ:#цвет» и рисуются {@link DemoPhotoDrawable}.
 */
public final class ImageLoader {
    public static final String DEMO_PREFIX = "demo:";

    private ImageLoader() {
    }

    public static String demo(Category category, String hexColor) {
        return DEMO_PREFIX + category.name() + ":" + hexColor;
    }

    /** Первое фото из списка, разделённого переводом строки. */
    public static String first(String photos) {
        if (photos == null || photos.isEmpty()) return null;
        int idx = photos.indexOf('\n');
        return idx < 0 ? photos : photos.substring(0, idx);
    }

    public static void load(ImageView view, String photo) {
        load(view, photo, 0.5f);
    }

    public static void load(ImageView view, String photo, float iconScale) {
        Context context = view.getContext();
        Glide.with(view).clear(view);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        if (photo == null || photo.isEmpty()) {
            view.setImageDrawable(demoDrawable(context, Category.ACCESSORIES, Color.GRAY, iconScale));
            return;
        }
        if (photo.startsWith(DEMO_PREFIX)) {
            String[] parts = photo.split(":");
            Category category = Category.of(parts.length > 1 ? parts[1] : "");
            int color;
            try {
                color = Color.parseColor(parts.length > 2 ? parts[2] : "#888888");
            } catch (IllegalArgumentException e) {
                color = Color.GRAY;
            }
            view.setImageDrawable(demoDrawable(context, category, color, iconScale));
            return;
        }
        Glide.with(view)
                .load(new File(photo))
                .placeholder(R.drawable.bg_placeholder)
                .error(R.drawable.bg_placeholder)
                .centerCrop()
                .into(view);
    }

    public static Drawable demoDrawable(Context context, Category category, int color, float iconScale) {
        Drawable icon = ContextCompat.getDrawable(context, category.icon);
        return new DemoPhotoDrawable(icon, color, iconScale);
    }

    public static void loadAvatar(ImageView view, String path, String name) {
        Context context = view.getContext();
        Glide.with(view).clear(view);
        if (path == null || path.isEmpty() || !new File(path).exists()) {
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            view.setImageDrawable(new LetterDrawable(name));
            return;
        }
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(view).load(new File(path)).circleCrop().into(view);
    }
}
