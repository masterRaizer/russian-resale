package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.russianresale.app.data.entity.CartItem;
import com.russianresale.app.data.model.CartLine;

import java.util.List;

@Dao
public interface CartDao {
    String LINES = "SELECT c.listingId AS listingId, c.quantity AS quantity, l.title AS title, l.price AS price, "
            + "l.photos AS photos, l.size AS size, l.category AS category, l.quantity AS stock, l.status AS status, "
            + "l.sellerId AS sellerId, l.madeToOrder AS madeToOrder, "
            + "CASE WHEN u.shopName IS NOT NULL AND u.shopName <> '' THEN u.shopName ELSE u.fullName END AS sellerName "
            + "FROM cart_items c JOIN listings l ON l.id = c.listingId JOIN users u ON u.id = l.sellerId "
            + "WHERE c.userId = :userId ORDER BY l.sellerId, c.addedAt";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(CartItem item);

    @Query("SELECT * FROM cart_items WHERE userId = :userId AND listingId = :listingId")
    CartItem find(long userId, long listingId);

    @Query("UPDATE cart_items SET quantity = :quantity WHERE userId = :userId AND listingId = :listingId")
    void setQuantity(long userId, long listingId, int quantity);

    @Query("DELETE FROM cart_items WHERE userId = :userId AND listingId = :listingId")
    void delete(long userId, long listingId);

    @Query("DELETE FROM cart_items WHERE userId = :userId")
    void clear(long userId);

    @Query(LINES)
    LiveData<List<CartLine>> observeLines(long userId);

    @Query(LINES)
    List<CartLine> getLines(long userId);

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE userId = :userId")
    LiveData<Integer> observeCount(long userId);

    @Query("SELECT EXISTS(SELECT 1 FROM cart_items WHERE userId = :userId AND listingId = :listingId)")
    LiveData<Boolean> observeInCart(long userId, long listingId);
}
