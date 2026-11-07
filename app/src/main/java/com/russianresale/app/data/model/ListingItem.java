package com.russianresale.app.data.model;

import androidx.room.Embedded;

import com.russianresale.app.data.entity.Listing;

/** Объявление для карточки в каталоге: + имя продавца и признак «в избранном». */
public class ListingItem {
    @Embedded
    public Listing listing;
    public String sellerName;
    public boolean favorite;
}
