package com.russianresale.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "favorites", primaryKeys = {"userId", "listingId"},
        foreignKeys = {
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "userId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Listing.class, parentColumns = "id", childColumns = "listingId",
                        onDelete = ForeignKey.CASCADE)},
        indices = {@Index("listingId")})
public class Favorite {
    public long userId;
    public long listingId;
    public long createdAt;

    public Favorite() {
    }

    @androidx.room.Ignore
    public Favorite(long userId, long listingId) {
        this.userId = userId;
        this.listingId = listingId;
        this.createdAt = System.currentTimeMillis();
    }
}
