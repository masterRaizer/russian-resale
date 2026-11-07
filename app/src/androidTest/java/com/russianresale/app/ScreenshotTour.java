package com.russianresale.app;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.russianresale.app.data.SessionManager;
import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.Order;
import com.russianresale.app.data.entity.OrderItem;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.DeliveryMethod;
import com.russianresale.app.data.model.OrderStatus;
import com.russianresale.app.data.model.PaymentMethod;
import com.russianresale.app.ui.auth.LoginActivity;
import com.russianresale.app.ui.auth.RegisterActivity;
import com.russianresale.app.ui.cart.CheckoutActivity;
import com.russianresale.app.ui.catalog.FilterBottomSheet;
import com.russianresale.app.ui.chat.ChatActivity;
import com.russianresale.app.ui.listing.EditListingActivity;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.ui.main.MainActivity;
import com.russianresale.app.ui.notifications.NotificationsActivity;
import com.russianresale.app.ui.orders.OrderDetailsActivity;
import com.russianresale.app.ui.seller.SellerProfileActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;

/**
 * Обход экранов со скриншотами (workflow «UI screenshots»).
 * Снимки сохраняются в files/screens приложения. Неудачный шаг не прерывает обход и помечается в log.txt.
 */
@RunWith(AndroidJUnit4.class)
public class ScreenshotTour {
    // id демо-данных (DemoSeeder заполняет базу всегда в одном порядке)
    private static final long ADMIN = 1, ANNA = 2, NIT = 4;

    private final Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
    private final Context context = instrumentation.getTargetContext();
    private final File dir = new File(context.getFilesDir(), "screens");
    private final StringBuilder log = new StringBuilder();
    private AppDatabase db;
    private long reviewOrderId;

    private interface Step {
        void run() throws Exception;
    }

    @Test
    public void tour() throws Exception {
        dir.mkdirs();
        db = AppDatabase.get(context);
        waitForDemoData();
        prepareReviewOrder();

        // ---------------------------------------------------------------- гость
        logout();
        step("01_login", () -> show(new Intent(context, LoginActivity.class), null));
        step("02_register_seller", () -> show(new Intent(context, RegisterActivity.class), a ->
                ((MaterialButtonToggleGroup) a.findViewById(R.id.roleToggle)).check(R.id.roleSeller)));

        // ---------------------------------------------------------------- покупатель
        login(ANNA);
        step("03_catalog", () -> main(R.id.nav_catalog, null));
        step("04_filters", () -> main(R.id.nav_catalog, a -> {
            Fragment f = ((FragmentActivity) a).getSupportFragmentManager().findFragmentByTag("tab_" + R.id.nav_catalog);
            new FilterBottomSheet().show(f.getChildFragmentManager(), "filter");
        }));
        step("05_details", () -> show(listing(3), null));
        step("06_details_specs", () -> show(listing(1), a -> scroll(a, 0.45f)));
        step("07_details_reviews", () -> show(listing(7), a -> scroll(a, 1f)));
        step("08_favorites", () -> main(R.id.nav_favorites, null));
        step("09_cart", () -> main(R.id.nav_cart, null));
        step("10_checkout", () -> show(new Intent(context, CheckoutActivity.class), null));
        step("11_checkout_bottom", () -> show(new Intent(context, CheckoutActivity.class), a -> scroll(a, 1f)));
        step("12_orders_buyer", () -> main(R.id.nav_orders, null));
        step("13_order_shipped", () -> show(order(4), null));
        step("14_review_dialog", () -> show(order(reviewOrderId), a -> {
            View button = a.findViewById(R.id.review);
            if (button != null) button.performClick();
        }));
        step("15_chat", () -> show(new Intent(context, ChatActivity.class).putExtra("chat_id", 1L), null));
        step("16_seller_profile", () -> show(new Intent(context, SellerProfileActivity.class).putExtra("seller_id", NIT), null));
        step("17_profile_buyer", () -> main(R.id.nav_profile, null));
        step("18_notifications", () -> show(new Intent(context, NotificationsActivity.class), null));

        // ---------------------------------------------------------------- продавец
        login(NIT);
        step("19_my_listings", () -> main(R.id.nav_my_listings, null));
        step("20_seller_orders", () -> main(R.id.nav_seller_orders, null));
        step("21_order_new_seller", () -> show(order(5), null));
        step("22_edit_listing", () -> show(new Intent(context, EditListingActivity.class).putExtra("listing_id", 1L), null));
        step("23_seller_reviews", () -> main(R.id.nav_reviews, null));
        step("24_seller_chats", () -> main(R.id.nav_chats, null));
        step("25_profile_seller", () -> main(R.id.nav_profile, null));
        step("26_details_other_seller", () -> show(listing(10), null));

        // ---------------------------------------------------------------- администратор
        login(ADMIN);
        step("27_complaints", () -> main(R.id.nav_complaints, null));
        step("28_admin_listings", () -> main(R.id.nav_admin_listings, null));
        step("29_users", () -> main(R.id.nav_users, null));
        step("30_profile_admin", () -> main(R.id.nav_profile, null));
        step("31_details_admin", () -> show(listing(17), null));

        // ---------------------------------------------------------------- тёмная тема
        login(ANNA);
        instrumentation.runOnMainSync(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES));
        step("32_dark_catalog", () -> main(R.id.nav_catalog, null));
        step("33_dark_details", () -> show(listing(1), null));
        step("34_dark_order", () -> show(order(4), null));
        instrumentation.runOnMainSync(() -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM));

        try (FileOutputStream out = new FileOutputStream(new File(dir, "log.txt"))) {
            out.write(log.toString().getBytes("UTF-8"));
        }
    }

    // ================================================================ подготовка

    private void waitForDemoData() {
        long deadline = SystemClock.uptimeMillis() + 30_000;
        while (SystemClock.uptimeMillis() < deadline) {
            if (db.listings().getById(23) != null && db.complaints().countOpen() > 0) break;
            SystemClock.sleep(300);
        }
        SystemClock.sleep(1500);
    }

    /** Полученный заказ без отзыва — чтобы показать диалог отзыва. */
    private void prepareReviewOrder() {
        Listing l = db.listings().getById(22);
        Order o = new Order();
        o.buyerId = ANNA;
        o.sellerId = l.sellerId;
        o.status = OrderStatus.DELIVERED.name();
        o.deliveryMethod = DeliveryMethod.CDEK.name();
        o.itemsTotal = l.price;
        o.total = l.price;
        o.phone = "+79161234567";
        o.address = "Москва, ул. Тверская, д. 7, кв. 12";
        o.paymentMethod = PaymentMethod.CARD.name();
        o.paid = true;
        o.trackNumber = "CDEK-10511200";
        o.createdAt = System.currentTimeMillis() - 6L * 24 * 3600 * 1000;
        o.updatedAt = System.currentTimeMillis() - 24L * 3600 * 1000;
        reviewOrderId = db.orders().insert(o);
        OrderItem item = new OrderItem();
        item.orderId = reviewOrderId;
        item.listingId = l.id;
        item.title = l.title;
        item.photo = l.mainPhoto();
        item.size = l.size;
        item.price = l.price;
        item.quantity = 1;
        db.orders().insertItems(Collections.singletonList(item));
    }

    private void login(long userId) {
        User user = db.users().getById(userId);
        new SessionManager(context).save(user);
    }

    private void logout() {
        new SessionManager(context).clear();
    }

    private Intent listing(long id) {
        return new Intent(context, ListingDetailsActivity.class).putExtra("listing_id", id);
    }

    private Intent order(long id) {
        return new Intent(context, OrderDetailsActivity.class).putExtra("order_id", id);
    }

    // ================================================================ шаги

    private interface OnActivity {
        void run(Activity activity);
    }

    private void step(String name, Step step) {
        String result = "ok";
        nextName = name;
        try {
            step.run();
        } catch (Throwable e) {
            result = "FAIL " + e.getClass().getSimpleName() + ": " + e.getMessage();
            shot(name + "_FAIL");
        }
        log.append(name).append(": ").append(result).append('\n');
    }

    private void main(int tabId, OnActivity extra) {
        show(new Intent(context, MainActivity.class), a -> {
            ((BottomNavigationView) a.findViewById(R.id.bottomNav)).setSelectedItemId(tabId);
            if (extra != null) extra.run(a);
        });
    }

    /** Открывает экран, ждёт загрузки данных, выполняет действие и делает снимок. */
    private void show(Intent intent, OnActivity action) {
        try (ActivityScenario<Activity> scenario = ActivityScenario.launch(intent)) {
            SystemClock.sleep(1500);
            if (action != null) {
                scenario.onActivity(action::run);
                SystemClock.sleep(1200);
            }
            shot(nextName);
        }
    }

    /** Имя снимка текущего шага. */
    private String nextName = "shot";

    private void shot(String name) {
        instrumentation.waitForIdleSync();
        SystemClock.sleep(400);
        Bitmap bitmap = instrumentation.getUiAutomation().takeScreenshot();
        if (bitmap == null) return;
        try (FileOutputStream out = new FileOutputStream(new File(dir, name + ".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (Exception ignored) {
        }
    }

    private static void scroll(Activity a, float fraction) {
        View scroller = findScroller(a.getWindow().getDecorView());
        if (scroller == null) return;
        int height = ((ViewGroup) scroller).getChildAt(0).getHeight() - scroller.getHeight();
        int y = Math.max(0, Math.round(height * fraction));
        if (scroller instanceof NestedScrollView) ((NestedScrollView) scroller).scrollTo(0, y);
        else ((ScrollView) scroller).scrollTo(0, y);
    }

    private static View findScroller(View v) {
        if (v instanceof NestedScrollView || v instanceof ScrollView) return v;
        if (v instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) {
                View found = findScroller(g.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}
