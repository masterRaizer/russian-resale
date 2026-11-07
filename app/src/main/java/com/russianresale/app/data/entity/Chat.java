package com.russianresale.app.data.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Переписка покупателя и продавца по конкретному объявлению. */
@Entity(tableName = "chats",
        indices = {@Index(value = {"buyerId", "sellerId", "listingId"}, unique = true), @Index("sellerId")})
public class Chat {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long buyerId;
    public long sellerId;
    public long listingId;
    public String lastMessage;
    public long updatedAt;
    public int unreadBuyer;
    public int unreadSeller;
}
