package com.russianresale.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

@Entity(tableName = "cart_items", primaryKeys = {"userId", "listingId"},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Listing.class, parentColumns = "id", childColumns = "listingId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index("listingId")})
public class CartItem {
    public long userId;
    public long listingId;
    public int quantity = 1;
    public long addedAt;

    public CartItem() {
    }

    @Ignore
    public CartItem(long userId, long listingId, int quantity) {
        this.userId = userId;
        this.listingId = listingId;
        this.quantity = quantity;
        this.addedAt = System.currentTimeMillis();
    }
}
