package com.russianresale.app.util;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

/** Тема оформления: как в системе, светлая или тёмная. */
public final class ThemeSettings {
    private static final String PREFS = "settings";
    private static final String KEY = "night_mode";
    public static final String[] TITLES = {"Как в системе", "Светлая", "Тёмная"};
    private static final int[] MODES = {
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES};

    private ThemeSettings() {
    }

    public static int index(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getInt(KEY, 0);
    }

    public static void apply(Context context) {
        int i = index(context);
        AppCompatDelegate.setDefaultNightMode(MODES[Math.max(0, Math.min(i, MODES.length - 1))]);
    }

    public static void set(Context context, int index) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putInt(KEY, index).apply();
        apply(context);
    }
}
