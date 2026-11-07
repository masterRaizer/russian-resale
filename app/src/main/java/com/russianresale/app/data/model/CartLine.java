package com.russianresale.app.data.model;

/** Строка корзины, собранная запросом из cart_items, listings и users. */
public class CartLine {
    public long listingId;
    public int quantity;
    public String title;
    public int price;
    public String photos;
    public String size;
    public String category;
    public int stock;
    public String status;
    public long sellerId;
    public boolean madeToOrder;
    public String sellerName;

    public int lineTotal() {
        return price * quantity;
    }

    public boolean isAvailable() {
        return ListingStatus.ACTIVE.name().equals(status) && stock >= quantity;
    }

    public String mainPhoto() {
        if (photos == null || photos.isEmpty()) return null;
        int idx = photos.indexOf('\n');
        return idx < 0 ? photos : photos.substring(0, idx);
    }
}
