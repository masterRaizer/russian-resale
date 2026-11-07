package com.russianresale.app;

import android.app.Application;
import android.content.Context;

import com.russianresale.app.data.DemoSeeder;
import com.russianresale.app.data.SessionManager;
import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.repo.AdminRepository;
import com.russianresale.app.data.repo.AuthRepository;
import com.russianresale.app.data.repo.ChatRepository;
import com.russianresale.app.data.repo.ListingRepository;
import com.russianresale.app.data.repo.NotificationRepository;
import com.russianresale.app.data.repo.OrderRepository;
import com.russianresale.app.util.ThemeSettings;

/** Точка входа приложения и простой контейнер зависимостей (Service Locator). */
public class App extends Application {
    private SessionManager session;
    private AuthRepository auth;
    private ListingRepository listings;
    private OrderRepository orders;
    private ChatRepository chats;
    private NotificationRepository notifications;
    private AdminRepository admin;

    @Override
    public void onCreate() {
        super.onCreate();
        ThemeSettings.apply(this);
        AppDatabase db = AppDatabase.get(this);
        session = new SessionManager(this);
        auth = new AuthRepository(db);
        listings = new ListingRepository(db);
        orders = new OrderRepository(db);
        chats = new ChatRepository(db);
        notifications = new NotificationRepository(db);
        admin = new AdminRepository(db);
        // при первом запуске база заполняется демо-данными
        new DemoSeeder(db).seedIfEmpty();
    }

    public static App get(Context context) {
        return (App) context.getApplicationContext();
    }

    public SessionManager session() {
        return session;
    }

    public AuthRepository auth() {
        return auth;
    }

    public ListingRepository listings() {
        return listings;
    }

    public OrderRepository orders() {
        return orders;
    }

    public ChatRepository chats() {
        return chats;
    }

    public NotificationRepository notifications() {
        return notifications;
    }

    public AdminRepository admin() {
        return admin;
    }
}
