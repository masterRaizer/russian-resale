package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.russianresale.app.data.entity.Complaint;
import com.russianresale.app.data.model.ComplaintItem;

import java.util.List;

@Dao
public interface ComplaintDao {
    @Insert
    long insert(Complaint complaint);

    @Update
    void update(Complaint complaint);

    @Query("SELECT * FROM complaints WHERE id = :id")
    Complaint getById(long id);

    @Query("SELECT EXISTS(SELECT 1 FROM complaints WHERE listingId = :listingId AND reporterId = :reporterId AND status = 'OPEN')")
    boolean hasOpen(long listingId, long reporterId);

    @Query("SELECT c.*, l.title AS listingTitle, l.photos AS listingPhoto, l.status AS listingStatus, "
            + "(SELECT fullName FROM users WHERE id = c.reporterId) AS reporterName, "
            + "(SELECT CASE WHEN shopName IS NOT NULL AND shopName <> '' THEN shopName ELSE fullName END FROM users WHERE id = l.sellerId) AS sellerName "
            + "FROM complaints c JOIN listings l ON l.id = c.listingId "
            + "ORDER BY CASE c.status WHEN 'OPEN' THEN 0 ELSE 1 END, c.createdAt DESC")
    LiveData<List<ComplaintItem>> observeAll();

    @Query("SELECT COUNT(*) FROM complaints WHERE status = 'OPEN'")
    int countOpen();

    @Query("UPDATE complaints SET status = :status WHERE listingId = :listingId AND status = 'OPEN'")
    void closeAllForListing(long listingId, String status);
}
