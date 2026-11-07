package com.russianresale.app.ui.orders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.russianresale.app.R;
import com.russianresale.app.data.entity.Order;
import com.russianresale.app.data.entity.OrderItem;
import com.russianresale.app.data.model.OrderCard;
import com.russianresale.app.data.model.OrderStatus;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.databinding.ActivityOrderDetailsBinding;
import com.russianresale.app.databinding.ItemOrderLineBinding;
import com.russianresale.app.ui.chat.ChatActivity;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.SummaryRows;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.ui.seller.SellerProfileActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;
import com.russianresale.app.util.OrderRules;

import java.util.ArrayList;
import java.util.List;

/** Детали заказа и действия по нему (зависят от роли и статуса). */
public class OrderDetailsActivity extends BaseActivity {
    private static final String EXTRA_ID = "order_id";
    private static final String[] RATING_LABELS = {"Ужасно", "Плохо", "Нормально", "Хорошо", "Отлично"};

    private ActivityOrderDetailsBinding b;
    private long orderId;
    private Role role;
    private OrderCard card;
    private List<OrderItem> items = new ArrayList<>();

    public static void open(Context context, long orderId) {
        context.startActivity(new Intent(context, OrderDetailsActivity.class).putExtra(EXTRA_ID, orderId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        orderId = getIntent().getLongExtra(EXTRA_ID, 0);
        role = session().getRole();
        setupToolbar(b.toolbar, "Заказ " + Format.orderNumber(orderId));

        app().orders().observeOrder(orderId).observe(this, c -> {
            if (c == null) {
                toast("Заказ не найден");
                finish();
                return;
            }
            // заказ видят только его участники и администратор
            long uid = userId();
            if (role != Role.ADMIN && c.order.buyerId != uid && c.order.sellerId != uid) {
                toast("Нет доступа к заказу");
                finish();
                return;
            }
            card = c;
            bind();
        });
        app().orders().observeItems(orderId).observe(this, list -> {
            items = list == null ? new ArrayList<>() : list;
            bindItems();
        });
    }

    private boolean isSeller() {
        return card != null && card.order.sellerId == userId();
    }

    private boolean isBuyer() {
        return card != null && card.order.buyerId == userId();
    }

    private void bind() {
        Order o = card.order;
        OrderStatus status = o.statusEnum();
        Ui.bindOrderStatus(b.status, status);
        int step = OrderRules.step(status);
        b.progress.setVisibility(step < 0 ? View.GONE : View.VISIBLE);
        b.steps.setVisibility(step < 0 ? View.GONE : View.VISIBLE);
        b.progress.setProgress(Math.max(step, 0));
        b.statusHint.setText(statusHint(o));
        b.dates.setText("Оформлен " + Format.dateTime(o.createdAt)
                + (o.updatedAt > o.createdAt ? " · обновлён " + Format.dateTime(o.updatedAt) : ""));

        // вторая сторона сделки
        if (isSeller()) {
            b.partyTitle.setText("Покупатель");
            b.partyName.setText(card.buyerName);
            b.partyInfo.setText(o.phone);
            b.partyChevron.setVisibility(View.GONE);
            b.partyCard.setOnClickListener(null);
            ImageLoader.loadAvatar(b.partyAvatar, null, card.buyerName);
        } else {
            b.partyTitle.setText("Продавец");
            b.partyName.setText(card.sellerName);
            b.partyInfo.setText("Профиль, отзывы и другие вещи");
            b.partyChevron.setVisibility(View.VISIBLE);
            b.partyCard.setOnClickListener(v -> SellerProfileActivity.open(this, o.sellerId));
            ImageLoader.loadAvatar(b.partyAvatar, null, card.sellerName);
        }

        b.deliveryInfo.removeAllViews();
        SummaryRows.add(b.deliveryInfo, "Способ", o.deliveryEnum().title, false);
        if (o.address != null) SummaryRows.add(b.deliveryInfo, "Адрес", o.address, false);
        SummaryRows.add(b.deliveryInfo, "Телефон", o.phone, false);
        if (o.trackNumber != null && !o.trackNumber.isEmpty()) {
            SummaryRows.add(b.deliveryInfo, "Трек-номер", o.trackNumber, false);
        }
        if (o.measurements != null && !o.measurements.isEmpty()) {
            SummaryRows.add(b.deliveryInfo, "Мерки", o.measurements, false);
        }
        if (o.comment != null && !o.comment.isEmpty()) SummaryRows.add(b.deliveryInfo, "Комментарий", o.comment, false);
        if (o.cancelReason != null) SummaryRows.add(b.deliveryInfo, "Причина отмены", o.cancelReason, false);

        b.paymentInfo.removeAllViews();
        SummaryRows.add(b.paymentInfo, o.paymentEnum().title, o.paid ? "оплачено" : "не оплачено", false);
        SummaryRows.add(b.paymentInfo, "Товары", Format.price(o.itemsTotal), false);
        SummaryRows.add(b.paymentInfo, "Доставка", o.deliveryCost == 0 ? "бесплатно" : Format.price(o.deliveryCost), false);
        SummaryRows.add(b.paymentInfo, "Итого", Format.price(o.total), true);

        bindActions();
        bindItems();
    }

    private String statusHint(Order o) {
        boolean seller = isSeller();
        switch (o.statusEnum()) {
            case NEW:
                return seller ? "Проверьте наличие и примите заказ или отклоните его с указанием причины"
                        : "Продавец скоро подтвердит заказ. До подтверждения его можно отменить";
            case ACCEPTED:
                return seller ? "Подготовьте заказ и отметьте отправку (для доставки — с трек-номером)"
                        : "Продавец готовит заказ" + (hasCustom() ? " — вещи шьются по вашим меркам" : "");
            case SHIPPED:
                return seller ? "Ждём, пока покупатель подтвердит получение"
                        : "Заказ в пути. Когда заберёте его, нажмите «Я получил заказ»";
            case DELIVERED:
                return seller ? "Сделка завершена" : "Сделка завершена. Оцените вещи — это помогает другим покупателям";
            default:
                return "Заказ отменён";
        }
    }

    private boolean hasCustom() {
        for (OrderItem i : items) if (i.madeToOrder) return true;
        return false;
    }

    private void bindItems() {
        b.items.removeAllViews();
        boolean canReview = card != null && isBuyer() && OrderRules.buyerCanReview(card.order.statusEnum());
        for (OrderItem item : items) {
            ItemOrderLineBinding row = ItemOrderLineBinding.inflate(LayoutInflater.from(this), b.items, false);
            ImageLoader.load(row.photo, item.photo);
            row.title.setText(item.title);
            row.meta.setText("Размер " + item.size + (item.madeToOrder ? " · ✂ пошив на заказ" : ""));
            row.price.setText(item.quantity > 1
                    ? item.quantity + " × " + Format.price(item.price)
                    : Format.price(item.price));
            row.review.setVisibility(canReview && !item.reviewed ? View.VISIBLE : View.GONE);
            row.reviewed.setVisibility(canReview && item.reviewed ? View.VISIBLE : View.GONE);
            row.review.setOnClickListener(v -> reviewDialog(item));
            row.getRoot().setOnClickListener(v -> ListingDetailsActivity.open(this, item.listingId));
            b.items.addView(row.getRoot());
        }
    }

    // ---------------------------------------------------------------- действия

    private void bindActions() {
        OrderStatus status = card.order.statusEnum();
        b.btnPrimary.setVisibility(View.GONE);
        b.btnSecondary.setVisibility(View.GONE);
        b.btnChat.setVisibility(View.GONE);

        if (isSeller()) {
            if (OrderRules.sellerCanAccept(status)) {
                primary("Принять заказ", () -> app().orders().accept(userId(), orderId,
                        Ui.callback(this, ok -> toast("Заказ принят"))));
                secondary("Отклонить", this::sellerCancel);
            } else if (OrderRules.sellerCanShip(status)) {
                boolean delivery = card.order.deliveryEnum().needsAddress;
                primary(delivery ? "Отправить" : "Готов к выдаче", this::ship);
                secondary("Отменить", this::sellerCancel);
            }
        } else if (isBuyer()) {
            b.btnChat.setVisibility(View.VISIBLE);
            b.btnChat.setOnClickListener(v -> {
                if (items.isEmpty()) return;
                app().chats().openChat(userId(), items.get(0).listingId,
                        Ui.callback(this, chatId -> ChatActivity.open(this, chatId)));
            });
            if (OrderRules.buyerCanConfirm(status)) {
                primary("Я получил заказ", () -> Ui.confirm(this, "Подтвердить получение?",
                        "Подтверждайте, только если заказ у вас на руках и вы его проверили.", "Подтверждаю",
                        () -> app().orders().confirmReceived(userId(), orderId,
                                Ui.callback(this, ok -> toast("Спасибо! Теперь можно оставить отзыв")))));
            }
            if (OrderRules.buyerCanCancel(status)) {
                secondary("Отменить заказ", () -> Ui.inputDialog(this, "Отменить заказ?", "Причина (необязательно)",
                        null, false, "Отменить заказ", reason -> app().orders().cancelByBuyer(userId(), orderId, reason,
                                Ui.callback(this, ok -> toast("Заказ отменён")))));
            }
        }
        boolean any = b.btnPrimary.getVisibility() == View.VISIBLE || b.btnSecondary.getVisibility() == View.VISIBLE
                || b.btnChat.getVisibility() == View.VISIBLE;
        b.actions.setVisibility(any ? View.VISIBLE : View.GONE);
    }

    private void primary(String text, Runnable action) {
        b.btnPrimary.setVisibility(View.VISIBLE);
        b.btnPrimary.setText(text);
        b.btnPrimary.setOnClickListener(v -> action.run());
    }

    private void secondary(String text, Runnable action) {
        b.btnSecondary.setVisibility(View.VISIBLE);
        b.btnSecondary.setText(text);
        b.btnSecondary.setOnClickListener(v -> action.run());
    }

    private void sellerCancel() {
        Ui.inputDialog(this, "Отменить заказ", "Причина для покупателя", null, true, "Отменить заказ",
                reason -> app().orders().cancelBySeller(userId(), orderId, reason,
                        Ui.callback(this, ok -> toast("Заказ отменён, товар вернулся в продажу"))));
    }

    private void ship() {
        if (card.order.deliveryEnum().needsAddress) {
            Ui.inputDialog(this, "Отправка заказа", "Трек-номер отправления", null, false, "Отправлено",
                    track -> app().orders().ship(userId(), orderId, track,
                            Ui.callback(this, ok -> toast("Покупатель получил трек-номер"))));
        } else {
            app().orders().ship(userId(), orderId, null, Ui.callback(this, ok -> toast("Покупатель уведомлён")));
        }
    }

    private void reviewDialog(OrderItem item) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_review, null, false);
        RatingBar rating = view.findViewById(R.id.rating);
        TextView label = view.findViewById(R.id.ratingLabel);
        TextInputEditText text = view.findViewById(R.id.text);
        rating.setOnRatingBarChangeListener((bar, value, fromUser) -> {
            if (value < 1) bar.setRating(1);
            label.setText(RATING_LABELS[Math.max(1, Math.round(bar.getRating())) - 1]);
        });
        new MaterialAlertDialogBuilder(this)
                .setTitle("Отзыв: " + item.title)
                .setView(view)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Отправить", (d, w) -> app().orders().addReview(userId(), item.id,
                        Math.round(rating.getRating()), Ui.text(text), Ui.callback(this, ok -> toast("Спасибо за отзыв!"))))
                .show();
    }
}
