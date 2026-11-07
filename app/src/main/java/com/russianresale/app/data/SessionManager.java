package com.russianresale.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.Role;

/** Текущая сессия: кто вошёл в приложение. Хранится в SharedPreferences. */
public class SessionManager {
    private static final String PREFS = "session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_NAME = "name";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void save(User user) {
        prefs.edit()
                .putLong(KEY_USER_ID, user.id)
                .putString(KEY_ROLE, user.role)
                .putString(KEY_NAME, user.displayName())
                .apply();
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, 0);
    }

    public Role getRole() {
        return Role.of(prefs.getString(KEY_ROLE, Role.BUYER.name()));
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public boolean isBuyer() {
        return isLoggedIn() && getRole() == Role.BUYER;
    }

    public boolean isSeller() {
        return isLoggedIn() && getRole() == Role.SELLER;
    }

    public boolean isAdmin() {
        return isLoggedIn() && getRole() == Role.ADMIN;
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
