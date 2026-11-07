package com.russianresale.app.data.repo;

import androidx.lifecycle.LiveData;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.Chat;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.Message;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.ChatItem;

import java.util.List;

/** Сообщения между покупателем и продавцом. Начать переписку может только покупатель. */
public class ChatRepository extends BaseRepository {

    public ChatRepository(AppDatabase db) {
        super(db);
    }

    public LiveData<List<ChatItem>> observeChats(long userId) {
        return db.chats().observeForUser(userId);
    }

    public LiveData<ChatItem> observeChat(long chatId) {
        return db.chats().observeItem(chatId);
    }

    public LiveData<List<Message>> observeMessages(long chatId) {
        return db.chats().observeMessages(chatId);
    }

    public LiveData<Integer> observeUnread(long userId) {
        return db.chats().observeUnread(userId);
    }

    /** Находит или создаёт чат покупателя с продавцом по объявлению. */
    public void openChat(long buyerId, long listingId, Callback<Long> callback) {
        exec(() -> {
            requireBuyer(buyerId);
            Listing l = db.listings().getById(listingId);
            if (l == null) throw new AppException("Объявление не найдено");
            Chat chat = db.chats().find(buyerId, l.sellerId, listingId);
            if (chat != null) return chat.id;
            chat = new Chat();
            chat.buyerId = buyerId;
            chat.sellerId = l.sellerId;
            chat.listingId = listingId;
            chat.updatedAt = System.currentTimeMillis();
            return db.chats().insert(chat);
        }, callback);
    }

    public void send(long userId, long chatId, String text, Callback<Boolean> callback) {
        exec(() -> {
            User user = requireUser(userId);
            Chat chat = db.chats().getById(chatId);
            if (chat == null || (chat.buyerId != userId && chat.sellerId != userId)) {
                throw new AppException("Чат не найден");
            }
            if (text == null || text.trim().isEmpty()) throw new AppException("Введите сообщение");
            if (text.length() > 1000) throw new AppException("Слишком длинное сообщение");
            Message m = new Message();
            m.chatId = chatId;
            m.senderId = user.id;
            m.text = text.trim();
            m.createdAt = System.currentTimeMillis();
            db.chats().insertMessage(m);
            chat.lastMessage = m.text;
            chat.updatedAt = m.createdAt;
            if (userId == chat.buyerId) chat.unreadSeller++;
            else chat.unreadBuyer++;
            db.chats().update(chat);
            return true;
        }, callback);
    }

    public void markRead(long userId, long chatId) {
        exec(() -> {
            Chat chat = db.chats().getById(chatId);
            if (chat == null) return false;
            if (userId == chat.buyerId) chat.unreadBuyer = 0;
            else if (userId == chat.sellerId) chat.unreadSeller = 0;
            db.chats().update(chat);
            return true;
        }, null);
    }
}
