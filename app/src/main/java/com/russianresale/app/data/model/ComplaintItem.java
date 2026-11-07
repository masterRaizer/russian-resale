package com.russianresale.app.data.model;

import androidx.room.Embedded;

import com.russianresale.app.data.entity.Complaint;

public class ComplaintItem {
    @Embedded
    public Complaint complaint;
    public String listingTitle;
    public String listingPhoto;
    public String listingStatus;
    public String reporterName;
    public String sellerName;
}
