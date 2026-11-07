package com.russianresale.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.russianresale.app.data.model.Category;
import com.russianresale.app.data.model.ListingFilter;
import com.russianresale.app.data.model.SortOrder;

import org.junit.Test;

public class ListingFilterTest {
    @Test
    public void emptyFilterShowsOnlyActiveListings() {
        String sql = new ListingFilter().toQuery(1).getSql();
        assertTrue(sql.contains("l.status = 'ACTIVE'"));
        assertTrue(sql.contains("u.blocked = 0"));
        assertTrue(sql.endsWith("ORDER BY l.createdAt DESC"));
    }

    @Test
    public void filtersAddConditionsAndArguments() {
        ListingFilter f = new ListingFilter();
        f.query = "Льняное платье";
        f.category = Category.DRESSES;
        f.minPrice = 1000;
        f.maxPrice = 9000;
        f.madeToOrder = true;
        f.sort = SortOrder.CHEAPEST;
        String sql = f.toQuery(5).getSql();
        assertTrue(sql.contains("l.category = ?"));
        assertTrue(sql.contains("l.price >= ?"));
        assertTrue(sql.contains("l.madeToOrder = ?"));
        assertTrue(sql.endsWith("ORDER BY l.price ASC"));
        // userId + 2 слова поиска + категория + 2 цены + тип
        assertEquals(7, f.toQuery(5).getArgCount());
    }

    @Test
    public void activeCountIgnoresCategoryAndQuery() {
        ListingFilter f = new ListingFilter();
        f.category = Category.SHOES;
        f.query = "кеды";
        assertEquals(0, f.activeCount());
        f.size = "38";
        f.maxPrice = 5000;
        assertEquals(2, f.activeCount());
    }
}
