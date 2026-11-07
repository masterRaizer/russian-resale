package com.russianresale.app.util;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * «Фото» для демо-объявлений: мягкий градиент цвета вещи и силуэт категории.
 * Размер не задан, поэтому ImageView растягивает рисунок на всю свою площадь.
 */
public class DemoPhotoDrawable extends Drawable {
    private final Drawable icon;
    private final int color;
    private final Paint background = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint circle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float iconScale;

    public DemoPhotoDrawable(Drawable icon, int color, float iconScale) {
        this.icon = icon.mutate();
        this.color = color;
        this.iconScale = iconScale;
        this.icon.setTintList(null);
        this.icon.setTint(color);
        circle.setColor(Color.argb(150, 255, 255, 255));
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        int light = blend(color, Color.WHITE, 0.86f);
        int lighter = blend(color, Color.WHITE, 0.95f);
        background.setShader(new LinearGradient(bounds.left, bounds.top, bounds.right, bounds.bottom,
                lighter, light, Shader.TileMode.CLAMP));
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect b = getBounds();
        canvas.drawRect(b, background);
        int size = (int) (Math.min(b.width(), b.height()) * iconScale);
        int cx = b.centerX();
        int cy = b.centerY();
        canvas.drawCircle(cx, cy, size * 0.78f, circle);
        icon.setBounds(cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2);
        icon.draw(canvas);
    }

    private static int blend(int from, int to, float ratio) {
        float inv = 1f - ratio;
        int r = (int) (Color.red(from) * inv + Color.red(to) * ratio);
        int g = (int) (Color.green(from) * inv + Color.green(to) * ratio);
        int bl = (int) (Color.blue(from) * inv + Color.blue(to) * ratio);
        return Color.rgb(r, g, bl);
    }

    @Override
    public void setAlpha(int alpha) {
        background.setAlpha(alpha);
        icon.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        icon.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
