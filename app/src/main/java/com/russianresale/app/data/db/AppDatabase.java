package com.russianresale.app.data.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.russianresale.app.data.dao.CartDao;
import com.russianresale.app.data.dao.ChatDao;
import com.russianresale.app.data.dao.ComplaintDao;
import com.russianresale.app.data.dao.FavoriteDao;
import com.russianresale.app.data.dao.ListingDao;
import com.russianresale.app.data.dao.NotificationDao;
import com.russianresale.app.data.dao.OrderDao;
import com.russianresale.app.data.dao.ReviewDao;
import com.russianresale.app.data.dao.UserDao;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.data.entity.CartItem;
import com.russianresale.app.data.entity.Chat;
import com.russianresale.app.data.entity.Complaint;
import com.russianresale.app.data.entity.Favorite;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.Message;
import com.russianresale.app.data.entity.Order;
import com.russianresale.app.data.entity.OrderItem;
import com.russianresale.app.data.entity.Review;
import com.russianresale.app.data.entity.User;

/** Локальная база данных SQLite (Room) — 11 таблиц. */
@Database(entities = {
        User.class, Listing.class, Favorite.class, CartItem.class, Order.class, OrderItem.class,
        Review.class, Chat.class, Message.class, AppNotification.class, Complaint.class
}, version = 1, exportSchema = true)
public abstract class AppDatabase extends RoomDatabase {
    private static final String NAME = "russian_resale.db";
    private static volatile AppDatabase instance;

    public abstract UserDao users();

    public abstract ListingDao listings();

    public abstract FavoriteDao favorites();

    public abstract CartDao cart();

    public abstract OrderDao orders();

    public abstract ReviewDao reviews();

    public abstract ChatDao chats();

    public abstract NotificationDao notifications();

    public abstract ComplaintDao complaints();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, NAME)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
