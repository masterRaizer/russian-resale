package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteQuery;

import com.russianresale.app.data.entity.Favorite;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.ListingItem;

import java.util.List;

@Dao
public interface ListingDao {
    @Insert
    long insert(Listing listing);

    @Update
    void update(Listing listing);

    @Query("DELETE FROM listings WHERE id = :id")
    void delete(long id);

    @Query("SELECT * FROM listings WHERE id = :id")
    Listing getById(long id);

    @Query("SELECT * FROM listings WHERE id = :id")
    LiveData<Listing> observe(long id);

    /** Каталог с фильтрами (SQL собирает {@link com.russianresale.app.data.model.ListingFilter}). */
    @RawQuery(observedEntities = {Listing.class, Favorite.class, User.class})
    LiveData<List<ListingItem>> search(SupportSQLiteQuery query);

    @Query("SELECT l.*, CASE WHEN u.shopName IS NOT NULL AND u.shopName <> '' THEN u.shopName ELSE u.fullName END AS sellerName, "
            + "1 AS favorite FROM favorites f JOIN listings l ON l.id = f.listingId JOIN users u ON u.id = l.sellerId "
            + "WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    LiveData<List<ListingItem>> observeFavorites(long userId);

    /** Активные объявления продавца — для его публичного профиля. */
    @Query("SELECT l.*, CASE WHEN u.shopName IS NOT NULL AND u.shopName <> '' THEN u.shopName ELSE u.fullName END AS sellerName, "
            + "EXISTS(SELECT 1 FROM favorites f WHERE f.listingId = l.id AND f.userId = :viewerId) AS favorite "
            + "FROM listings l JOIN users u ON u.id = l.sellerId "
            + "WHERE l.sellerId = :sellerId AND l.status = 'ACTIVE' ORDER BY l.createdAt DESC")
    LiveData<List<ListingItem>> observeSellerActive(long sellerId, long viewerId);

    /** Все объявления продавца (для экрана «Мои объявления»), включая проданные и архив. */
    @Query("SELECT * FROM listings WHERE sellerId = :sellerId ORDER BY CASE status WHEN 'ACTIVE' THEN 0 WHEN 'BLOCKED' THEN 1 WHEN 'SOLD' THEN 2 ELSE 3 END, createdAt DESC")
    LiveData<List<Listing>> observeBySeller(long sellerId);

    @Query("SELECT l.*, CASE WHEN u.shopName IS NOT NULL AND u.shopName <> '' THEN u.shopName ELSE u.fullName END AS sellerName, "
            + "0 AS favorite FROM listings l JOIN users u ON u.id = l.sellerId ORDER BY l.createdAt DESC")
    LiveData<List<ListingItem>> observeAllForAdmin();

    @Query("UPDATE listings SET views = views + 1 WHERE id = :id")
    void incrementViews(long id);

    @Query("UPDATE listings SET status = :status, updatedAt = :now WHERE id = :id")
    void setStatus(long id, String status, long now);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND listingId = :listingId)")
    LiveData<Boolean> observeIsFavorite(long userId, long listingId);

    @Query("SELECT COUNT(*) FROM favorites WHERE listingId = :listingId")
    LiveData<Integer> observeFavoritesCount(long listingId);

    @Query("SELECT COUNT(*) FROM listings WHERE sellerId = :sellerId AND status = 'ACTIVE'")
    int countActiveBySeller(long sellerId);

    @Query("SELECT COUNT(*) FROM listings WHERE status = 'ACTIVE'")
    int countActive();
}
