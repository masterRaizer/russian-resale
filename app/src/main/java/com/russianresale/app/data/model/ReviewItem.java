package com.russianresale.app.data.model;

import androidx.room.Embedded;

import com.russianresale.app.data.entity.Review;

public class ReviewItem {
    @Embedded
    public Review review;
    public String buyerName;
    public String sellerName;
    public String listingTitle;
    public String listingPhoto;
}
