package com.russianresale.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "messages",
        foreignKeys = @ForeignKey(entity = Chat.class, parentColumns = "id", childColumns = "chatId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("chatId")})
public class Message {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long chatId;
    public long senderId;
    public String text;
    public long createdAt;
}
