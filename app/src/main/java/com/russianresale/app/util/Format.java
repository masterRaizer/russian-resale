package com.russianresale.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Форматирование цен, дат и рейтинга. */
public final class Format {
    private static final Locale RU = Locale.forLanguageTag("ru-RU");

    private Format() {
    }

    /** 12500 → «12 500 ₽». */
    public static String price(long rub) {
        String digits = String.valueOf(Math.abs(rub));
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = digits.length() - 1; i >= 0; i--) {
            sb.append(digits.charAt(i));
            if (++count % 3 == 0 && i > 0) sb.append(' ');
        }
        if (rub < 0) sb.append('-');
        return sb.reverse() + " ₽";
    }

    public static String date(long millis) {
        return new SimpleDateFormat("d MMMM yyyy", RU).format(new Date(millis));
    }

    public static String dateTime(long millis) {
        return new SimpleDateFormat("d MMM yyyy, HH:mm", RU).format(new Date(millis));
    }

    public static String time(long millis) {
        return new SimpleDateFormat("HH:mm", RU).format(new Date(millis));
    }

    /** «сегодня, 14:05», «вчера» или дата. */
    public static String relative(long millis) {
        long now = System.currentTimeMillis();
        long dayMs = 24L * 60 * 60 * 1000;
        String today = new SimpleDateFormat("yyyyMMdd", RU).format(new Date(now));
        String that = new SimpleDateFormat("yyyyMMdd", RU).format(new Date(millis));
        if (today.equals(that)) return "сегодня, " + time(millis);
        String yesterday = new SimpleDateFormat("yyyyMMdd", RU).format(new Date(now - dayMs));
        if (yesterday.equals(that)) return "вчера, " + time(millis);
        return new SimpleDateFormat("d MMM", RU).format(new Date(millis));
    }

    public static String rating(double value) {
        return String.format(RU, "%.1f", value);
    }

    /** 1 отзыв, 2 отзыва, 5 отзывов. */
    public static String plural(int n, String one, String few, String many) {
        int mod100 = n % 100;
        int mod10 = n % 10;
        String word;
        if (mod100 >= 11 && mod100 <= 14) word = many;
        else if (mod10 == 1) word = one;
        else if (mod10 >= 2 && mod10 <= 4) word = few;
        else word = many;
        return n + " " + word;
    }

    public static String orderNumber(long id) {
        return "№ " + (100000 + id);
    }
}
