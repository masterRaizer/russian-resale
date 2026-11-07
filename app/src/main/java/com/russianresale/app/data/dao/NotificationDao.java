package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.russianresale.app.data.entity.AppNotification;

import java.util.List;

@Dao
public interface NotificationDao {
    @Insert
    long insert(AppNotification notification);

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC, id DESC")
    LiveData<List<AppNotification>> observe(long userId);

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND read = 0")
    LiveData<Integer> observeUnread(long userId);

    @Query("UPDATE notifications SET read = 1 WHERE userId = :userId")
    void markAllRead(long userId);

    @Query("DELETE FROM notifications WHERE userId = :userId")
    void clear(long userId);
}
