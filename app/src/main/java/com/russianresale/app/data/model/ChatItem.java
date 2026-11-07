package com.russianresale.app.data.model;

import androidx.room.Embedded;

import com.russianresale.app.data.entity.Chat;

public class ChatItem {
    @Embedded
    public Chat chat;
    public String buyerName;
    public String sellerName;
    public String listingTitle;
    public String listingPhoto;
}
