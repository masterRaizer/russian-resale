package com.russianresale.app.ui.cart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.CartLine;
import com.russianresale.app.data.model.DeliveryMethod;
import com.russianresale.app.data.model.PaymentMethod;
import com.russianresale.app.data.repo.Callback;
import com.russianresale.app.data.repo.CheckoutRequest;
import com.russianresale.app.databinding.ActivityCheckoutBinding;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.SummaryRows;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.main.MainActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.PriceCalculator;
import com.russianresale.app.util.Validators;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Оформление заказа: вся корзина или одна вещь («Купить сейчас»). Доступно только покупателю. */
public class CheckoutActivity extends BaseActivity {
    private static final String EXTRA_LISTING = "listing_id";
    private static final int ID_DELIVERY = 100;
    private static final int ID_PAYMENT = 200;

    private ActivityCheckoutBinding b;
    private Long buyNowId;
    private List<CartLine> lines = new ArrayList<>();

    public static void openCart(Context context) {
        context.startActivity(new Intent(context, CheckoutActivity.class));
    }

    public static void openBuyNow(Context context, long listingId) {
        context.startActivity(new Intent(context, CheckoutActivity.class).putExtra(EXTRA_LISTING, listingId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "Оформление заказа");
        if (!session().isBuyer()) {
            toast("Покупать могут только покупатели");
            finish();
            return;
        }
        long id = getIntent().getLongExtra(EXTRA_LISTING, 0);
        buyNowId = id == 0 ? null : id;

        for (DeliveryMethod m : DeliveryMethod.values()) {
            RadioButton rb = new RadioButton(this);
            rb.setId(ID_DELIVERY + m.ordinal());
            rb.setText(m.title);
            b.delivery.addView(rb);
        }
        for (PaymentMethod m : PaymentMethod.values()) {
            RadioButton rb = new RadioButton(this);
            rb.setId(ID_PAYMENT + m.ordinal());
            rb.setText(m.title);
            b.payment.addView(rb);
        }
        b.delivery.setOnCheckedChangeListener((g, checked) -> onDeliveryChanged());
        b.payment.setOnCheckedChangeListener((g, checked) -> {
            b.cardSection.setVisibility(payment() == PaymentMethod.CARD ? View.VISIBLE : View.GONE);
            bindTotals();
        });
        b.delivery.check(ID_DELIVERY + DeliveryMethod.CDEK.ordinal());
        b.payment.check(ID_PAYMENT + PaymentMethod.CARD.ordinal());
        b.submit.setOnClickListener(v -> submit());

        app().auth().getUser(userId(), Ui.callback(this, (User u) -> {
            if (Ui.text(b.phone).isEmpty() && u.phone != null) b.phone.setText(u.phone);
            if (Ui.text(b.address).isEmpty() && u.city != null) b.address.setText(u.city + ", ");
        }));
        app().orders().previewLines(userId(), buyNowId, new Callback<List<CartLine>>() {
            @Override
            public void onSuccess(List<CartLine> result) {
                if (result.isEmpty()) {
                    toast("Корзина пуста");
                    finish();
                    return;
                }
                lines = result;
                bindLines();
            }

            @Override
            public void onError(String message) {
                toast(message);
                finish();
            }
        });
    }

    private DeliveryMethod delivery() {
        int id = b.delivery.getCheckedRadioButtonId();
        return id < ID_DELIVERY ? DeliveryMethod.PICKUP : DeliveryMethod.values()[id - ID_DELIVERY];
    }

    private PaymentMethod payment() {
        int id = b.payment.getCheckedRadioButtonId();
        return id < ID_PAYMENT ? PaymentMethod.CASH : PaymentMethod.values()[id - ID_PAYMENT];
    }

    /** Позиции, сгруппированные по продавцам: каждый продавец — отдельный заказ. */
    private Map<Long, List<CartLine>> groups() {
        Map<Long, List<CartLine>> map = new LinkedHashMap<>();
        for (CartLine line : lines) {
            List<CartLine> list = map.get(line.sellerId);
            if (list == null) {
                list = new ArrayList<>();
                map.put(line.sellerId, list);
            }
            list.add(line);
        }
        return map;
    }

    private void bindLines() {
        b.items.removeAllViews();
        boolean custom = false;
        for (List<CartLine> group : groups().values()) {
            SummaryRows.header(b.items, group.get(0).sellerName);
            for (CartLine line : group) {
                custom |= line.madeToOrder;
                SummaryRows.add(b.items, line.title + " (" + line.size + ")"
                        + (line.quantity > 1 ? " × " + line.quantity : ""), Format.price(line.lineTotal()), false);
            }
        }
        b.measurementsSection.setVisibility(custom ? View.VISIBLE : View.GONE);
        onDeliveryChanged();
    }

    private void onDeliveryChanged() {
        DeliveryMethod method = delivery();
        b.addressLayout.setVisibility(method.needsAddress ? View.VISIBLE : View.GONE);
        // подписи с ценой доставки для текущей корзины
        int itemsTotal = 0;
        for (CartLine l : lines) itemsTotal += l.lineTotal();
        for (DeliveryMethod m : DeliveryMethod.values()) {
            RadioButton rb = b.delivery.findViewById(ID_DELIVERY + m.ordinal());
            int cost = PriceCalculator.deliveryCost(m, itemsTotal);
            rb.setText(m.title + " — " + (cost == 0 ? "бесплатно" : Format.price(cost)));
        }
        bindTotals();
    }

    private int bindTotals() {
        b.totals.removeAllViews();
        if (lines.isEmpty()) return 0;
        DeliveryMethod method = delivery();
        int items = 0;
        int deliveryTotal = 0;
        Map<Long, List<CartLine>> groups = groups();
        for (List<CartLine> group : groups.values()) {
            int sum = 0;
            for (CartLine l : group) sum += l.lineTotal();
            items += sum;
            deliveryTotal += PriceCalculator.deliveryCost(method, sum);
        }
        SummaryRows.add(b.totals, "Товары", Format.price(items), false);
        SummaryRows.add(b.totals, groups.size() > 1 ? "Доставка (" + groups.size() + " отправления)" : "Доставка",
                deliveryTotal == 0 ? "бесплатно" : Format.price(deliveryTotal), false);
        int total = items + deliveryTotal;
        SummaryRows.add(b.totals, "К оплате", Format.price(total), true);
        if (method != DeliveryMethod.COURIER && method != DeliveryMethod.PICKUP) {
            SummaryRows.add(b.totals, "Доставка бесплатна от " + Format.price(PriceCalculator.FREE_DELIVERY_FROM)
                    + " у одного продавца", "", false);
        }
        b.submit.setText((payment() == PaymentMethod.CASH ? "Оформить заказ · " : "Оплатить · ") + Format.price(total));
        return total;
    }

    private String measurements() {
        if (b.measurementsSection.getVisibility() != View.VISIBLE) return null;
        String h = Ui.text(b.mHeight), c = Ui.text(b.mChest), w = Ui.text(b.mWaist), hips = Ui.text(b.mHips);
        if (h.isEmpty() || c.isEmpty() || w.isEmpty() || hips.isEmpty()) return "";
        return "Рост " + h + ", грудь " + c + ", талия " + w + ", бёдра " + hips;
    }

    private void submit() {
        CheckoutRequest req = new CheckoutRequest();
        req.buyNowListingId = buyNowId;
        req.delivery = delivery();
        req.payment = payment();
        req.address = Ui.text(b.address);
        req.phone = Ui.text(b.phone);
        req.comment = Ui.text(b.comment);
        req.measurements = measurements();
        if (req.payment == PaymentMethod.CARD) {
            if (!Validators.isValidCardNumber(Ui.text(b.cardNumber))) {
                toast("Проверьте номер карты");
                return;
            }
            if (!Validators.isValidCardExpiry(Ui.text(b.cardExpiry)) || Ui.text(b.cardCvc).length() != 3) {
                toast("Проверьте срок действия и CVC");
                return;
            }
        }
        b.submit.setEnabled(false);
        app().orders().checkout(userId(), req, new Callback<List<Long>>() {
            @Override
            public void onSuccess(List<Long> ids) {
                StringBuilder numbers = new StringBuilder();
                for (Long id : ids) {
                    if (numbers.length() > 0) numbers.append(", ");
                    numbers.append(Format.orderNumber(id));
                }
                new MaterialAlertDialogBuilder(CheckoutActivity.this)
                        .setTitle(ids.size() > 1 ? "Заказы оформлены" : "Заказ оформлен")
                        .setMessage((ids.size() > 1 ? "Номера заказов: " : "Номер заказа: ") + numbers
                                + "\n\nПродавец подтвердит заказ, статус можно отслеживать во вкладке «Заказы».")
                        .setCancelable(false)
                        .setPositiveButton("К заказам", (d, w) -> {
                            MainActivity.openTab(CheckoutActivity.this, MainActivity.TAB_ORDERS);
                            finish();
                        })
                        .show();
            }

            @Override
            public void onError(String message) {
                b.submit.setEnabled(true);
                toast(message);
            }
        });
    }
}
