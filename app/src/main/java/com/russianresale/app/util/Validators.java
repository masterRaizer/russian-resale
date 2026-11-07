package com.russianresale.app.util;

import java.util.regex.Pattern;

/** Проверка вводимых данных. Без зависимостей от Android — покрыто unit-тестами. */
public final class Validators {
    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{10,15}$");

    public static final int MIN_PASSWORD = 6;
    public static final int MIN_PRICE = 100;
    public static final int MAX_PRICE = 5_000_000;

    private Validators() {
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= MIN_PASSWORD;
    }

    /** Телефон: 10–15 цифр, можно с + в начале; пробелы, скобки и дефисы игнорируются. */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        String digits = phone.replaceAll("[\\s()\\-]", "");
        return PHONE.matcher(digits).matches();
    }

    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    public static boolean isValidName(String name) {
        return name != null && name.trim().length() >= 2;
    }

    public static boolean isValidPrice(int price) {
        return price >= MIN_PRICE && price <= MAX_PRICE;
    }

    public static boolean isValidRating(int rating) {
        return rating >= 1 && rating <= 5;
    }

    /** Номер карты: 16 цифр и верная контрольная сумма (алгоритм Луна). */
    public static boolean isValidCardNumber(String number) {
        if (number == null) return false;
        String digits = number.replaceAll("[\\s-]", "");
        if (!digits.matches("\\d{16}")) return false;
        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            int d = digits.charAt(digits.length() - 1 - i) - '0';
            if (i % 2 == 1) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            sum += d;
        }
        return sum % 10 == 0;
    }

    /** Срок действия карты в формате ММ/ГГ, месяц 01–12. */
    public static boolean isValidCardExpiry(String value) {
        return value != null && value.trim().matches("(0[1-9]|1[0-2])/\\d{2}");
    }

    /** Безопасно разбирает целое число, null — если строка пустая или не число. */
    public static Integer parseIntOrNull(String value) {
        if (value == null) return null;
        String s = value.replaceAll("\\s", "");
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
