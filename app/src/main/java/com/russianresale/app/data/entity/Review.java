package com.russianresale.app.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Отзыв покупателя о купленной вещи и продавце. Один отзыв на одну позицию полученного заказа. */
@Entity(tableName = "reviews",
        indices = {@Index(value = "orderItemId", unique = true), @Index("sellerId"), @Index("listingId")})
public class Review {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long orderItemId;
    public long orderId;
    public long listingId;
    public long sellerId;
    public long buyerId;
    /** Оценка от 1 до 5. */
    public int rating;
    public String text;
    /** Публичный ответ продавца. */
    public String sellerReply;
    public long createdAt;
    public long replyAt;
}
