package com.russianresale.app.data.repo;

import androidx.lifecycle.LiveData;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.AppNotification;

import java.util.List;

public class NotificationRepository extends BaseRepository {

    public NotificationRepository(AppDatabase db) {
        super(db);
    }

    public LiveData<List<AppNotification>> observe(long userId) {
        return db.notifications().observe(userId);
    }

    public LiveData<Integer> observeUnread(long userId) {
        return db.notifications().observeUnread(userId);
    }

    public void markAllRead(long userId) {
        exec(() -> {
            db.notifications().markAllRead(userId);
            return true;
        }, null);
    }

    public void clear(long userId) {
        exec(() -> {
            db.notifications().clear(userId);
            return true;
        }, null);
    }
}
