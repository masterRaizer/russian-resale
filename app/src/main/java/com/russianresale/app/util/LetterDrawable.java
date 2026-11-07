package com.russianresale.app.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** Аватар-заглушка: первая буква имени на цветном круге. */
public class LetterDrawable extends Drawable {
    private static final int[] COLORS = {
            0xFF7A2E3B, 0xFF8A6A4D, 0xFF3F6B5C, 0xFF3559A8, 0xFF8E4585, 0xFFB26A00, 0xFF4E6E81
    };
    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final String letter;

    public LetterDrawable(String name) {
        String n = name == null ? "" : name.replaceAll("[«»\"']", "").trim();
        letter = n.isEmpty() ? "?" : n.substring(0, 1).toUpperCase();
        bg.setColor(COLORS[Math.floorMod(n.hashCode(), COLORS.length)]);
        text.setColor(Color.WHITE);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        float r = Math.min(b.width(), b.height()) / 2f;
        canvas.drawCircle(b.exactCenterX(), b.exactCenterY(), r, bg);
        text.setTextSize(r);
        float y = b.exactCenterY() - (text.descent() + text.ascent()) / 2f;
        canvas.drawText(letter, b.exactCenterX(), y, text);
    }

    @Override
    public void setAlpha(int alpha) {
        bg.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        bg.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
