package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.russianresale.app.data.entity.Review;
import com.russianresale.app.data.model.RatingSummary;
import com.russianresale.app.data.model.ReviewItem;

import java.util.List;

@Dao
public interface ReviewDao {
    String ITEM = "SELECT r.*, "
            + "(SELECT fullName FROM users WHERE id = r.buyerId) AS buyerName, "
            + "(SELECT CASE WHEN shopName IS NOT NULL AND shopName <> '' THEN shopName ELSE fullName END FROM users WHERE id = r.sellerId) AS sellerName, "
            + "(SELECT title FROM order_items WHERE id = r.orderItemId) AS listingTitle, "
            + "(SELECT photo FROM order_items WHERE id = r.orderItemId) AS listingPhoto "
            + "FROM reviews r ";

    @Insert
    long insert(Review review);

    @Update
    void update(Review review);

    @Query("SELECT * FROM reviews WHERE id = :id")
    Review getById(long id);

    @Query("SELECT * FROM reviews WHERE orderItemId = :orderItemId")
    Review findByOrderItem(long orderItemId);

    @Query(ITEM + "WHERE r.sellerId = :sellerId ORDER BY r.createdAt DESC")
    LiveData<List<ReviewItem>> observeBySeller(long sellerId);

    @Query(ITEM + "WHERE r.listingId = :listingId ORDER BY r.createdAt DESC")
    LiveData<List<ReviewItem>> observeByListing(long listingId);

    @Query(ITEM + "WHERE r.buyerId = :buyerId ORDER BY r.createdAt DESC")
    LiveData<List<ReviewItem>> observeByBuyer(long buyerId);

    @Query(ITEM + "WHERE r.orderId = :orderId ORDER BY r.createdAt DESC")
    LiveData<List<ReviewItem>> observeByOrder(long orderId);

    @Query("SELECT COALESCE(AVG(rating), 0) AS average, COUNT(*) AS count FROM reviews WHERE sellerId = :sellerId")
    LiveData<RatingSummary> observeSellerRating(long sellerId);

    @Query("SELECT COALESCE(AVG(rating), 0) AS average, COUNT(*) AS count FROM reviews WHERE sellerId = :sellerId")
    RatingSummary sellerRating(long sellerId);
}
