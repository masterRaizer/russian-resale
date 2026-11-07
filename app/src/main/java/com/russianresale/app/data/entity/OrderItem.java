package com.russianresale.app.data.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/** Позиция заказа. Название, цена и фото копируются, чтобы заказ не менялся при правке объявления. */
@Entity(tableName = "order_items",
        foreignKeys = @ForeignKey(entity = Order.class, parentColumns = "id", childColumns = "orderId",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index("orderId"), @Index("listingId")})
public class OrderItem {
    @PrimaryKey(autoGenerate = true)
    public long id;
    public long orderId;
    public long listingId;
    public String title;
    public String photo;
    public String size;
    public int price;
    public int quantity;
    public boolean madeToOrder;
    /** Покупатель уже оставил отзыв на эту позицию. */
    public boolean reviewed;
}
