package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.russianresale.app.data.entity.Chat;
import com.russianresale.app.data.entity.Message;
import com.russianresale.app.data.model.ChatItem;

import java.util.List;

@Dao
public interface ChatDao {
    String ITEM = "SELECT c.*, "
            + "(SELECT fullName FROM users WHERE id = c.buyerId) AS buyerName, "
            + "(SELECT CASE WHEN shopName IS NOT NULL AND shopName <> '' THEN shopName ELSE fullName END FROM users WHERE id = c.sellerId) AS sellerName, "
            + "(SELECT title FROM listings WHERE id = c.listingId) AS listingTitle, "
            + "(SELECT photos FROM listings WHERE id = c.listingId) AS listingPhoto "
            + "FROM chats c ";

    @Insert
    long insert(Chat chat);

    @Update
    void update(Chat chat);

    @Query("SELECT * FROM chats WHERE id = :id")
    Chat getById(long id);

    @Query("SELECT * FROM chats WHERE buyerId = :buyerId AND sellerId = :sellerId AND listingId = :listingId LIMIT 1")
    Chat find(long buyerId, long sellerId, long listingId);

    @Query(ITEM + "WHERE c.id = :id")
    LiveData<ChatItem> observeItem(long id);

    @Query(ITEM + "WHERE (c.buyerId = :userId OR c.sellerId = :userId) AND c.lastMessage IS NOT NULL ORDER BY c.updatedAt DESC")
    LiveData<List<ChatItem>> observeForUser(long userId);

    @Insert
    long insertMessage(Message message);

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt, id")
    LiveData<List<Message>> observeMessages(long chatId);

    @Query("SELECT COALESCE(SUM(CASE WHEN buyerId = :userId THEN unreadBuyer ELSE unreadSeller END), 0) FROM chats "
            + "WHERE buyerId = :userId OR sellerId = :userId")
    LiveData<Integer> observeUnread(long userId);
}
