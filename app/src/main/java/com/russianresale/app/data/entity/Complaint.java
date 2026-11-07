package com.russianresale.app.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.russianresale.app.data.model.ComplaintStatus;

/** Жалоба на объявление — её рассматривает администратор. */
@Entity(tableName = "complaints", indices = {@Index("listingId")})
public class Complaint {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long listingId;
    public long reporterId;
    public String reason;
    public String text;
    @NonNull
    public String status = ComplaintStatus.OPEN.name();
    public long createdAt;
}
