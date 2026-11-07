package com.russianresale.app.data.model;

import androidx.annotation.DrawableRes;

import com.russianresale.app.R;

/** Категории товаров. На площадке продаются только одежда, обувь и аксессуары. */
public enum Category {
    OUTERWEAR("Верхняя одежда", R.drawable.ic_cat_outerwear),
    DRESSES("Платья и юбки", R.drawable.ic_cat_dress),
    TOPS("Футболки и топы", R.drawable.ic_cat_tshirt),
    TROUSERS("Брюки и джинсы", R.drawable.ic_cat_trousers),
    SUITS("Костюмы", R.drawable.ic_cat_suit),
    KNITWEAR("Трикотаж", R.drawable.ic_cat_knit),
    SHOES("Обувь", R.drawable.ic_cat_shoes),
    BAGS("Сумки", R.drawable.ic_cat_bag),
    HATS("Головные уборы", R.drawable.ic_cat_hat),
    JEWELRY("Украшения", R.drawable.ic_cat_jewelry),
    ACCESSORIES("Аксессуары", R.drawable.ic_cat_accessory);

    public final String title;
    @DrawableRes
    public final int icon;

    Category(String title, int icon) {
        this.title = title;
        this.icon = icon;
    }

    public static Category of(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return ACCESSORIES;
        }
    }

    public static String[] titles() {
        Category[] values = values();
        String[] result = new String[values.length];
        for (int i = 0; i < values.length; i++) result[i] = values[i].title;
        return result;
    }
}
