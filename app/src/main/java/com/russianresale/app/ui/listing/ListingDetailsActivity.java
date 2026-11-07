package com.russianresale.app.ui.listing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.russianresale.app.R;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.databinding.ActivityListingDetailsBinding;
import com.russianresale.app.ui.cart.CheckoutActivity;
import com.russianresale.app.ui.chat.ChatActivity;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.main.MainActivity;
import com.russianresale.app.ui.seller.ReviewAdapter;
import com.russianresale.app.ui.seller.SellerProfileActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

/**
 * Карточка объявления. Набор кнопок зависит от роли:
 * покупатель — написать, в корзину, купить; продавец-владелец — редактировать, снять с продажи;
 * другой продавец — только просмотр; администратор — блокировка.
 */
public class ListingDetailsActivity extends BaseActivity {
    private static final String EXTRA_ID = "listing_id";
    private static final String[] COMPLAINT_REASONS = {
            "Подделка / реплика", "Не одежда и не аксессуар", "Фото не соответствуют описанию",
            "Мошенничество", "Оскорбительный контент", "Другое"};

    private ActivityListingDetailsBinding b;
    private final PhotoPagerAdapter photos = new PhotoPagerAdapter();
    private Listing listing;
    private Role role;
    private long listingId;
    private boolean favorite;
    private boolean inCart;
    private int favoritesCount;
    private boolean sellerObserved;

    public static void open(Context context, long listingId) {
        context.startActivity(new Intent(context, ListingDetailsActivity.class).putExtra(EXTRA_ID, listingId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityListingDetailsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "");
        role = session().getRole();
        listingId = getIntent().getLongExtra(EXTRA_ID, 0);

        b.pager.setAdapter(photos);
        b.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                b.pageIndicator.setText((position + 1) + " / " + photos.getItemCount());
            }
        });

        ReviewAdapter reviews = new ReviewAdapter(ReviewAdapter.MODE_LISTING, null);
        b.reviews.setLayoutManager(new LinearLayoutManager(this));
        b.reviews.setAdapter(reviews);
        app().orders().observeListingReviews(listingId).observe(this, list -> {
            reviews.submitList(list);
            b.noReviews.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        setupMenu();
        setupButtons();

        if (savedInstanceState == null) app().listings().registerView(listingId, userId());
        app().listings().observe(listingId).observe(this, l -> {
            if (l == null) {
                toast("Объявление удалено");
                finish();
                return;
            }
            listing = l;
            bind();
        });
        app().listings().observeFavoritesCount(listingId).observe(this, n -> {
            favoritesCount = n == null ? 0 : n;
            if (listing != null) bindStats();
        });
        if (role == Role.BUYER) {
            app().listings().observeIsFavorite(userId(), listingId).observe(this, fav -> {
                favorite = Boolean.TRUE.equals(fav);
                MenuItem item = b.toolbar.getMenu().findItem(R.id.action_favorite);
                item.setIcon(favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
            });
            app().orders().observeInCart(userId(), listingId).observe(this, value -> {
                inCart = Boolean.TRUE.equals(value);
                b.btnCart.setText(inCart ? "В корзине" : "В корзину");
            });
        }
    }

    private void bind() {
        Listing l = listing;
        photos.setPhotos(l.photoList());
        b.pageIndicator.setText((b.pager.getCurrentItem() + 1) + " / " + photos.getItemCount());
        b.pageIndicator.setVisibility(photos.getItemCount() > 1 ? View.VISIBLE : View.GONE);
        b.customBadge.setVisibility(l.madeToOrder ? View.VISIBLE : View.GONE);
        b.customBadge.setText("✂ Пошив на заказ · " + Format.plural(l.productionDays, "день", "дня", "дней"));

        b.price.setText(Format.price(l.price));
        b.title.setText(l.title);
        b.description.setText(l.description);

        ListingStatus status = l.statusEnum();
        b.statusLabel.setVisibility(status == ListingStatus.ACTIVE ? View.GONE : View.VISIBLE);
        Ui.bindListingStatus(b.statusLabel, status);

        b.tags.removeAllViews();
        addTag(l.conditionEnum().title);
        addTag("Размер: " + l.size);
        addTag(l.genderEnum().title);
        addTag(l.categoryEnum().title);

        b.specs.removeAllViews();
        addSpec("Категория", l.categoryEnum().title);
        addSpec("Для кого", l.genderEnum().title);
        addSpec("Размер", l.size);
        addSpec("Бренд", l.brand);
        addSpec("Цвет", l.color);
        addSpec("Материал", l.material);
        addSpec("Состояние", l.conditionEnum().title);
        addSpec("Тип", l.madeToOrder
                ? "Пошив на заказ, " + Format.plural(l.productionDays, "день", "дня", "дней")
                : "Готовая вещь");
        addSpec("В наличии", l.quantity > 0 ? l.quantity + " шт." : "нет");
        addSpec("Город", l.city);
        bindStats();

        if (!sellerObserved) {
            sellerObserved = true;
            app().auth().observeUser(l.sellerId).observe(this, this::bindSeller);
            app().orders().observeSellerRating(l.sellerId).observe(this, r -> {
                double avg = r == null ? 0 : r.average;
                int count = r == null ? 0 : r.count;
                b.sellerRatingBar.setRating((float) avg);
                b.sellerRating.setText(count == 0 ? "нет отзывов"
                        : Format.rating(avg) + " · " + Format.plural(count, "отзыв", "отзыва", "отзывов"));
            });
        }
        updateActions();
    }

    private void bindStats() {
        b.stats.setText(Format.plural(listing.views, "просмотр", "просмотра", "просмотров")
                + " · ♥ " + favoritesCount + " · опубликовано " + Format.date(listing.createdAt));
    }

    private void bindSeller(User seller) {
        if (seller == null) return;
        ImageLoader.loadAvatar(b.sellerAvatar, seller.avatarPath, seller.displayName());
        b.sellerName.setText(seller.displayName());
        b.sellerCity.setText((seller.city == null ? "" : seller.city + " · ")
                + "на площадке с " + Format.date(seller.createdAt));
        b.sellerCard.setOnClickListener(v -> SellerProfileActivity.open(this, seller.id));
    }

    private void addTag(String text) {
        Chip chip = new Chip(this);
        chip.setText(text);
        chip.setClickable(false);
        chip.setCheckable(false);
        b.tags.addView(chip);
    }

    private void addSpec(String label, String value) {
        if (value == null || value.trim().isEmpty()) return;
        View row = LayoutInflater.from(this).inflate(R.layout.item_spec_row, b.specs, false);
        ((TextView) row.findViewById(R.id.label)).setText(label);
        ((TextView) row.findViewById(R.id.value)).setText(value);
        b.specs.addView(row);
    }

    // ---------------------------------------------------------------- действия

    private void setupMenu() {
        b.toolbar.inflateMenu(R.menu.menu_listing);
        b.toolbar.getMenu().findItem(R.id.action_favorite).setVisible(role == Role.BUYER);
        b.toolbar.setOnMenuItemClickListener(item -> {
            if (listing == null) return false;
            int id = item.getItemId();
            if (id == R.id.action_favorite) {
                app().listings().toggleFavorite(userId(), listingId, Ui.callback(this, fav ->
                        toast(fav ? "Добавлено в избранное" : "Удалено из избранного")));
            } else if (id == R.id.action_share) {
                share();
            } else if (id == R.id.action_complain) {
                complain();
            }
            return true;
        });
    }

    private void updateActions() {
        Listing l = listing;
        boolean active = l.isActive();
        boolean own = role == Role.SELLER && l.sellerId == userId();
        MenuItem complainItem = b.toolbar.getMenu().findItem(R.id.action_complain);
        complainItem.setVisible(!own && role != Role.ADMIN);

        hide(b.btnChat, b.btnCart, b.btnBuy, b.btnEdit, b.btnArchive, b.btnBlock);
        String note = null;
        switch (role) {
            case BUYER:
                b.btnChat.setVisibility(View.VISIBLE);
                if (active) {
                    b.btnCart.setVisibility(View.VISIBLE);
                    b.btnBuy.setVisibility(View.VISIBLE);
                } else {
                    note = l.statusEnum() == ListingStatus.SOLD ? "Вещь продана" : "Объявление снято с продажи";
                }
                break;
            case SELLER:
                if (own) {
                    if (l.statusEnum() == ListingStatus.BLOCKED) {
                        note = "Объявление заблокировано модератором и скрыто из каталога";
                    } else {
                        b.btnEdit.setVisibility(View.VISIBLE);
                        b.btnArchive.setVisibility(View.VISIBLE);
                        b.btnArchive.setText(l.statusEnum() == ListingStatus.ARCHIVED ? "Вернуть в продажу" : "Снять с продажи");
                        b.btnArchive.setEnabled(l.statusEnum() != ListingStatus.SOLD);
                        if (l.statusEnum() == ListingStatus.SOLD) b.btnArchive.setText("Продано");
                    }
                } else {
                    note = "Вы вошли как продавец. Покупать с аккаунта продавца нельзя — "
                            + "для покупок зарегистрируйте аккаунт покупателя.";
                }
                break;
            case ADMIN:
                b.btnBlock.setVisibility(View.VISIBLE);
                boolean blocked = l.statusEnum() == ListingStatus.BLOCKED;
                b.btnBlock.setText(blocked ? "Разблокировать" : "Заблокировать");
                break;
        }
        b.bottomNote.setVisibility(note == null ? View.GONE : View.VISIBLE);
        b.bottomNote.setText(note);
    }

    private static void hide(View... views) {
        for (View v : views) v.setVisibility(View.GONE);
    }

    private void setupButtons() {
        b.btnCart.setOnClickListener(v -> {
            if (inCart) {
                MainActivity.openTab(this, MainActivity.TAB_CART);
                return;
            }
            app().orders().addToCart(userId(), listingId, Ui.callback(this, ok -> toast("Добавлено в корзину")));
        });
        b.btnBuy.setOnClickListener(v -> CheckoutActivity.openBuyNow(this, listingId));
        b.btnChat.setOnClickListener(v -> app().chats().openChat(userId(), listingId,
                Ui.callback(this, chatId -> ChatActivity.open(this, chatId))));
        b.btnEdit.setOnClickListener(v -> EditListingActivity.open(this, listingId));
        b.btnArchive.setOnClickListener(v -> {
            boolean archive = listing.statusEnum() != ListingStatus.ARCHIVED;
            app().listings().setArchived(userId(), listingId, archive, Ui.callback(this, ok ->
                    toast(archive ? "Объявление снято с продажи" : "Объявление снова в продаже")));
        });
        b.btnBlock.setOnClickListener(v -> {
            boolean block = listing.statusEnum() != ListingStatus.BLOCKED;
            Ui.confirm(this, block ? "Заблокировать объявление?" : "Разблокировать объявление?",
                    block ? "Объявление пропадёт из каталога, продавец получит уведомление"
                            : "Объявление снова станет доступно покупателям",
                    block ? "Заблокировать" : "Разблокировать",
                    () -> app().admin().setListingBlocked(userId(), listingId, block, Ui.callback(this, null)));
        });
    }

    private void share() {
        String text = listing.title + " — " + Format.price(listing.price) + "\nВ приложении «Русский ресейл»";
        Intent send = new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, "Поделиться"));
    }

    private void complain() {
        final int[] selected = {0};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Причина жалобы")
                .setSingleChoiceItems(COMPLAINT_REASONS, 0, (d, which) -> selected[0] = which)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Далее", (d, w) -> Ui.inputDialog(this, "Жалоба: " + COMPLAINT_REASONS[selected[0]],
                        "Комментарий для модератора", null, true, "Отправить",
                        text -> app().listings().complain(userId(), listingId, COMPLAINT_REASONS[selected[0]], text,
                                Ui.callback(this, ok -> toast("Жалоба отправлена модератору")))))
                .show();
    }
}
