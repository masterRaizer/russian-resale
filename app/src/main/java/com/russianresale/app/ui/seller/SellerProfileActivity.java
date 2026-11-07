package com.russianresale.app.ui.seller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.ListingItem;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.data.model.SellerStats;
import com.russianresale.app.databinding.ActivitySellerProfileBinding;
import com.russianresale.app.ui.catalog.ListingAdapter;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

/** Публичный профиль продавца (ателье): описание, рейтинг, вещи в продаже и отзывы. */
public class SellerProfileActivity extends BaseActivity {
    private static final String EXTRA_ID = "seller_id";

    private ActivitySellerProfileBinding b;
    private int listingsCount;
    private int reviewsCount;

    public static void open(Context context, long sellerId) {
        context.startActivity(new Intent(context, SellerProfileActivity.class).putExtra(EXTRA_ID, sellerId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySellerProfileBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "Профиль продавца");
        long sellerId = getIntent().getLongExtra(EXTRA_ID, 0);
        long uid = userId();
        boolean buyer = session().getRole() == Role.BUYER;

        ListingAdapter listings = new ListingAdapter(buyer, new ListingAdapter.Listener() {
            @Override
            public void onOpen(ListingItem item) {
                ListingDetailsActivity.open(SellerProfileActivity.this, item.listing.id);
            }

            @Override
            public void onFavorite(ListingItem item) {
                app().listings().toggleFavorite(uid, item.listing.id, Ui.callback(SellerProfileActivity.this, null));
            }
        });
        b.listings.setLayoutManager(new GridLayoutManager(this, 2));
        b.listings.setAdapter(listings);

        ReviewAdapter reviews = new ReviewAdapter(ReviewAdapter.MODE_SELLER, null);
        b.reviews.setLayoutManager(new LinearLayoutManager(this));
        b.reviews.setAdapter(reviews);

        b.tabs.addTab(b.tabs.newTab().setText("Вещи"));
        b.tabs.addTab(b.tabs.newTab().setText("Отзывы"));
        b.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateTab();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        app().auth().observeUser(sellerId).observe(this, this::bindSeller);
        app().listings().observeSellerActive(sellerId, uid).observe(this, list -> {
            listings.submitList(list);
            listingsCount = list == null ? 0 : list.size();
            b.tabs.getTabAt(0).setText("Вещи · " + listingsCount);
            b.statListings.setText(listingsCount + "\nв продаже");
            updateTab();
        });
        app().orders().observeSellerReviews(sellerId).observe(this, list -> {
            reviews.submitList(list);
            reviewsCount = list == null ? 0 : list.size();
            b.tabs.getTabAt(1).setText("Отзывы · " + reviewsCount);
            updateTab();
        });
        app().orders().observeSellerRating(sellerId).observe(this, r -> {
            int count = r == null ? 0 : r.count;
            b.statRating.setText((count == 0 ? "—" : "★ " + Format.rating(r.average)) + "\nрейтинг");
        });
        app().orders().sellerStats(sellerId, Ui.callback(this, (SellerStats s) ->
                b.statSold.setText(s.soldItems + "\nпродано")));
    }

    private void bindSeller(User seller) {
        if (seller == null || !seller.isSeller()) {
            toast("Продавец не найден");
            finish();
            return;
        }
        ImageLoader.loadAvatar(b.avatar, seller.avatarPath, seller.displayName());
        b.shopName.setText(seller.displayName());
        b.owner.setText(seller.fullName + (seller.city == null ? "" : " · " + seller.city));
        b.since.setText("На площадке с " + Format.date(seller.createdAt));
        boolean hasDescription = seller.shopDescription != null && !seller.shopDescription.isEmpty();
        b.description.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        b.description.setText(seller.shopDescription);
        if (seller.blocked) b.since.setText("Аккаунт заблокирован");
    }

    private void updateTab() {
        boolean listingsTab = b.tabs.getSelectedTabPosition() == 0;
        b.listings.setVisibility(listingsTab ? View.VISIBLE : View.GONE);
        b.reviews.setVisibility(listingsTab ? View.GONE : View.VISIBLE);
        boolean empty = listingsTab ? listingsCount == 0 : reviewsCount == 0;
        b.emptyText.setVisibility(empty ? View.VISIBLE : View.GONE);
        b.emptyText.setText(listingsTab ? "Сейчас у продавца нет вещей в продаже" : "Отзывов пока нет");
    }
}
