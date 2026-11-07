package com.russianresale.app.data.model;

import androidx.sqlite.db.SimpleSQLiteQuery;

import java.util.ArrayList;
import java.util.List;

/** Параметры поиска в каталоге. Собирает SQL-запрос для {@code @RawQuery}. */
public class ListingFilter {
    public String query;
    public Category category;
    public Gender gender;
    public Condition condition;
    public String size;
    public Integer minPrice;
    public Integer maxPrice;
    /** null — все, true — только пошив на заказ, false — только готовые вещи. */
    public Boolean madeToOrder;
    public SortOrder sort = SortOrder.NEWEST;

    public ListingFilter copy() {
        ListingFilter f = new ListingFilter();
        f.query = query;
        f.category = category;
        f.gender = gender;
        f.condition = condition;
        f.size = size;
        f.minPrice = minPrice;
        f.maxPrice = maxPrice;
        f.madeToOrder = madeToOrder;
        f.sort = sort;
        return f;
    }

    /** Сколько дополнительных фильтров включено (для значка на кнопке «Фильтры»). */
    public int activeCount() {
        int n = 0;
        if (gender != null) n++;
        if (condition != null) n++;
        if (size != null && !size.isEmpty()) n++;
        if (minPrice != null) n++;
        if (maxPrice != null) n++;
        if (madeToOrder != null) n++;
        return n;
    }

    public SimpleSQLiteQuery toQuery(long userId) {
        List<Object> args = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT l.*, ")
                .append("CASE WHEN u.shopName IS NOT NULL AND u.shopName <> '' THEN u.shopName ELSE u.fullName END AS sellerName, ")
                .append("EXISTS(SELECT 1 FROM favorites f WHERE f.listingId = l.id AND f.userId = ?) AS favorite ")
                .append("FROM listings l JOIN users u ON u.id = l.sellerId ")
                .append("WHERE l.status = 'ACTIVE' AND u.blocked = 0");
        args.add(userId);
        if (query != null && !query.trim().isEmpty()) {
            // LOWER() в SQLite понимает только латиницу, поэтому ищем по заранее
            // подготовленному полю searchText (оно хранится в нижнем регистре)
            for (String word : query.trim().toLowerCase().split("\\s+")) {
                sql.append(" AND l.searchText LIKE ?");
                args.add("%" + word + "%");
            }
        }
        if (category != null) {
            sql.append(" AND l.category = ?");
            args.add(category.name());
        }
        if (gender != null) {
            sql.append(" AND l.gender = ?");
            args.add(gender.name());
        }
        if (condition != null) {
            sql.append(" AND l.condition = ?");
            args.add(condition.name());
        }
        if (size != null && !size.trim().isEmpty()) {
            sql.append(" AND l.size LIKE ?");
            args.add("%" + size.trim() + "%");
        }
        if (minPrice != null) {
            sql.append(" AND l.price >= ?");
            args.add(minPrice);
        }
        if (maxPrice != null) {
            sql.append(" AND l.price <= ?");
            args.add(maxPrice);
        }
        if (madeToOrder != null) {
            sql.append(" AND l.madeToOrder = ?");
            args.add(madeToOrder ? 1 : 0);
        }
        sql.append(" ORDER BY ").append((sort == null ? SortOrder.NEWEST : sort).sql);
        return new SimpleSQLiteQuery(sql.toString(), args.toArray());
    }
}
