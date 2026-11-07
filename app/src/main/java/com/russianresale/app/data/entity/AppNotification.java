package com.russianresale.app.data.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Уведомление внутри приложения (новый заказ, смена статуса, отзыв, сообщение). */
@Entity(tableName = "notifications", indices = {@Index("userId")})
public class AppNotification {
    public static final String TYPE_ORDER = "ORDER";
    public static final String TYPE_REVIEW = "REVIEW";
    public static final String TYPE_CHAT = "CHAT";
    public static final String TYPE_LISTING = "LISTING";
    public static final String TYPE_SYSTEM = "SYSTEM";

    @PrimaryKey(autoGenerate = true)
    public long id;
    public long userId;
    public String title;
    public String text;
    public String type;
    /** Id связанного объекта: заказа, чата или объявления. */
    public long refId;
    public boolean read;
    public long createdAt;

    public AppNotification() {
    }

    @Ignore
    public AppNotification(long userId, String type, long refId, String title, String text) {
        this.userId = userId;
        this.type = type;
        this.refId = refId;
        this.title = title;
        this.text = text;
        this.createdAt = System.currentTimeMillis();
    }
}
