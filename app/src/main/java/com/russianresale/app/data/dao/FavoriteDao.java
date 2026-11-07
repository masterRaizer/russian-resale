package com.russianresale.app.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.russianresale.app.data.entity.Favorite;

@Dao
public interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Favorite favorite);

    @Query("DELETE FROM favorites WHERE userId = :userId AND listingId = :listingId")
    void delete(long userId, long listingId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND listingId = :listingId)")
    boolean exists(long userId, long listingId);
}
