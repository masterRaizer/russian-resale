package com.russianresale.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.russianresale.app.data.entity.Order;
import com.russianresale.app.data.entity.OrderItem;
import com.russianresale.app.data.model.OrderCard;

import java.util.List;

@Dao
public interface OrderDao {
    String CARD = "SELECT o.*, "
            + "(SELECT fullName FROM users WHERE id = o.buyerId) AS buyerName, "
            + "(SELECT CASE WHEN shopName IS NOT NULL AND shopName <> '' THEN shopName ELSE fullName END FROM users WHERE id = o.sellerId) AS sellerName, "
            + "(SELECT title FROM order_items WHERE orderId = o.id ORDER BY id LIMIT 1) AS firstTitle, "
            + "(SELECT photo FROM order_items WHERE orderId = o.id ORDER BY id LIMIT 1) AS photo, "
            + "(SELECT COALESCE(SUM(quantity), 0) FROM order_items WHERE orderId = o.id) AS itemsCount "
            + "FROM orders o ";

    @Insert
    long insert(Order order);

    @Insert
    void insertItems(List<OrderItem> items);

    @Update
    void update(Order order);

    @Update
    void updateItem(OrderItem item);

    @Query("SELECT * FROM orders WHERE id = :id")
    Order getById(long id);

    @Query("SELECT * FROM orders WHERE id = :id")
    LiveData<Order> observe(long id);

    @Query(CARD + "WHERE o.id = :id")
    LiveData<OrderCard> observeCard(long id);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id")
    LiveData<List<OrderItem>> observeItems(long orderId);

    @Query("SELECT * FROM order_items WHERE orderId = :orderId ORDER BY id")
    List<OrderItem> getItems(long orderId);

    @Query("SELECT * FROM order_items WHERE id = :id")
    OrderItem getItem(long id);

    @Query(CARD + "WHERE o.buyerId = :buyerId ORDER BY o.createdAt DESC")
    LiveData<List<OrderCard>> observeByBuyer(long buyerId);

    @Query(CARD + "WHERE o.sellerId = :sellerId ORDER BY CASE o.status WHEN 'NEW' THEN 0 WHEN 'ACCEPTED' THEN 1 WHEN 'SHIPPED' THEN 2 ELSE 3 END, o.createdAt DESC")
    LiveData<List<OrderCard>> observeBySeller(long sellerId);

    @Query("SELECT COUNT(*) FROM orders WHERE sellerId = :sellerId AND status = 'NEW'")
    LiveData<Integer> observeNewCountForSeller(long sellerId);

    @Query("SELECT COUNT(*) FROM orders WHERE sellerId = :sellerId AND status = 'NEW'")
    int countNewForSeller(long sellerId);

    @Query("SELECT COUNT(*) FROM orders WHERE sellerId = :sellerId AND status IN ('ACCEPTED', 'SHIPPED')")
    int countInProgressForSeller(long sellerId);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0) FROM order_items oi JOIN orders o ON o.id = oi.orderId "
            + "WHERE o.sellerId = :sellerId AND o.status = 'DELIVERED'")
    int countSoldItems(long sellerId);

    @Query("SELECT COALESCE(SUM(itemsTotal), 0) FROM orders WHERE sellerId = :sellerId AND status = 'DELIVERED'")
    long revenue(long sellerId);

    @Query("SELECT COUNT(*) FROM orders")
    int countAll();

    @Query("SELECT COALESCE(SUM(total), 0) FROM orders WHERE status = 'DELIVERED'")
    long turnover();

    /** Купил ли покупатель что-нибудь у этого продавца (для проверки при отзыве/чате). */
    @Query("SELECT EXISTS(SELECT 1 FROM orders WHERE buyerId = :buyerId AND sellerId = :sellerId)")
    boolean hasOrdersBetween(long buyerId, long sellerId);
}
