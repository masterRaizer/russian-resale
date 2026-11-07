package com.russianresale.app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.badge.BadgeDrawable;
import com.russianresale.app.R;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.databinding.ActivityMainBinding;
import com.russianresale.app.ui.admin.AdminListingsFragment;
import com.russianresale.app.ui.admin.ComplaintsFragment;
import com.russianresale.app.ui.admin.UsersFragment;
import com.russianresale.app.ui.auth.LoginActivity;
import com.russianresale.app.ui.cart.CartFragment;
import com.russianresale.app.ui.catalog.CatalogFragment;
import com.russianresale.app.ui.catalog.FavoritesFragment;
import com.russianresale.app.ui.chat.ChatsFragment;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.FragmentHostActivity;
import com.russianresale.app.ui.notifications.NotificationsActivity;
import com.russianresale.app.ui.orders.OrdersFragment;
import com.russianresale.app.ui.profile.ProfileFragment;
import com.russianresale.app.ui.seller.MyListingsFragment;
import com.russianresale.app.ui.seller.ReviewsFragment;

/**
 * Главный экран с нижней навигацией. Набор вкладок зависит от роли:
 * покупатель — каталог, избранное, корзина, заказы; продавец — объявления, заказы, сообщения, отзывы;
 * администратор — жалобы, объявления, пользователи.
 */
public class MainActivity extends BaseActivity {
    public static final String EXTRA_TAB = "tab";
    public static final String TAB_ORDERS = "orders";
    public static final String TAB_CART = "cart";

    private ActivityMainBinding b;
    private Role role;
    private int currentTab;

    public static void openTab(Context context, String tab) {
        Intent intent = new Intent(context, MainActivity.class).putExtra(EXTRA_TAB, tab);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!session().isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        role = session().getRole();

        switch (role) {
            case SELLER:
                b.bottomNav.inflateMenu(R.menu.menu_bottom_seller);
                break;
            case ADMIN:
                b.bottomNav.inflateMenu(R.menu.menu_bottom_admin);
                break;
            default:
                b.bottomNav.inflateMenu(R.menu.menu_bottom_buyer);
        }
        b.bottomNav.setOnItemSelectedListener(item -> {
            showTab(item);
            return true;
        });
        b.bottomNav.setOnItemReselectedListener(item -> {
        });

        setupToolbarMenu();
        observeBadges();

        int startTab = b.bottomNav.getMenu().getItem(0).getItemId();
        if (savedInstanceState != null) startTab = savedInstanceState.getInt("tab", startTab);
        b.bottomNav.setSelectedItemId(startTab);
        showTab(b.bottomNav.getMenu().findItem(startTab));
        handleIntent(getIntent());

        // если администратор заблокировал аккаунт — выходим
        app().auth().observeUser(userId()).observe(this, user -> {
            if (user == null || user.blocked) {
                if (user != null) toast("Аккаунт заблокирован администратором");
                logout();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null || b == null) return;
        String tab = intent.getStringExtra(EXTRA_TAB);
        if (TAB_ORDERS.equals(tab)) {
            int id = role == Role.SELLER ? R.id.nav_seller_orders : R.id.nav_orders;
            if (b.bottomNav.getMenu().findItem(id) != null) b.bottomNav.setSelectedItemId(id);
        } else if (TAB_CART.equals(tab) && b.bottomNav.getMenu().findItem(R.id.nav_cart) != null) {
            b.bottomNav.setSelectedItemId(R.id.nav_cart);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", currentTab);
    }

    private void showTab(MenuItem item) {
        if (item == null) return;
        int id = item.getItemId();
        currentTab = id;
        b.toolbar.setTitle(id == b.bottomNav.getMenu().getItem(0).getItemId() && role == Role.BUYER
                ? getString(R.string.app_name) : item.getTitle());

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction tx = fm.beginTransaction();
        for (Fragment f : fm.getFragments()) {
            if (f.getTag() != null && f.getTag().startsWith("tab_")) tx.hide(f);
        }
        String tag = "tab_" + id;
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = createFragment(id);
            tx.add(b.container.getId(), fragment, tag);
        } else {
            tx.show(fragment);
        }
        tx.commitNow();
    }

    private Fragment createFragment(int id) {
        if (id == R.id.nav_catalog) return new CatalogFragment();
        if (id == R.id.nav_favorites) return new FavoritesFragment();
        if (id == R.id.nav_cart) return new CartFragment();
        if (id == R.id.nav_orders) return OrdersFragment.forBuyer();
        if (id == R.id.nav_my_listings) return new MyListingsFragment();
        if (id == R.id.nav_seller_orders) return OrdersFragment.forSeller();
        if (id == R.id.nav_chats) return new ChatsFragment();
        if (id == R.id.nav_reviews) return ReviewsFragment.forSeller(userId());
        if (id == R.id.nav_complaints) return new ComplaintsFragment();
        if (id == R.id.nav_admin_listings) return new AdminListingsFragment();
        if (id == R.id.nav_users) return new UsersFragment();
        return new ProfileFragment();
    }

    // ---------------------------------------------------------------- тулбар и счётчики

    private TextView chatsBadge;
    private TextView notificationsBadge;

    private void setupToolbarMenu() {
        b.toolbar.inflateMenu(R.menu.menu_main);
        MenuItem chats = b.toolbar.getMenu().findItem(R.id.action_chats);
        MenuItem notifications = b.toolbar.getMenu().findItem(R.id.action_notifications);
        // у продавца сообщения — отдельная вкладка, у администратора их нет
        chats.setVisible(role == Role.BUYER);
        chatsBadge = bindAction(chats, R.drawable.ic_chat,
                v -> FragmentHostActivity.open(this, FragmentHostActivity.SCREEN_CHATS));
        notificationsBadge = bindAction(notifications, R.drawable.ic_notifications,
                v -> startActivity(new Intent(this, NotificationsActivity.class)));
    }

    private TextView bindAction(MenuItem item, int icon, View.OnClickListener listener) {
        View view = item.getActionView();
        if (view == null) return null;
        ((ImageView) view.findViewById(R.id.actionIcon)).setImageResource(icon);
        view.setContentDescription(item.getTitle());
        view.setOnClickListener(listener);
        return view.findViewById(R.id.actionBadge);
    }

    private void setBadgeText(TextView badge, Integer count) {
        if (badge == null) return;
        int n = count == null ? 0 : count;
        badge.setVisibility(n > 0 ? View.VISIBLE : View.GONE);
        badge.setText(n > 99 ? "99+" : String.valueOf(n));
    }

    private void setNavBadge(int itemId, Integer count) {
        if (b.bottomNav.getMenu().findItem(itemId) == null) return;
        int n = count == null ? 0 : count;
        if (n > 0) {
            BadgeDrawable badge = b.bottomNav.getOrCreateBadge(itemId);
            badge.setNumber(n);
            badge.setVisible(true);
        } else {
            b.bottomNav.removeBadge(itemId);
        }
    }

    private void observeBadges() {
        long uid = userId();
        app().notifications().observeUnread(uid).observe(this, n -> setBadgeText(notificationsBadge, n));
        if (role == Role.BUYER) {
            app().orders().observeCartCount(uid).observe(this, n -> setNavBadge(R.id.nav_cart, n));
            app().chats().observeUnread(uid).observe(this, n -> setBadgeText(chatsBadge, n));
        } else if (role == Role.SELLER) {
            app().orders().observeNewOrdersForSeller(uid).observe(this, n -> setNavBadge(R.id.nav_seller_orders, n));
            app().chats().observeUnread(uid).observe(this, n -> setNavBadge(R.id.nav_chats, n));
        }
    }

    public void logout() {
        session().clear();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
