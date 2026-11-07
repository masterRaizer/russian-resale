package com.russianresale.app.ui.cart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.model.CartLine;
import com.russianresale.app.databinding.FragmentCartBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.util.Format;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Корзина покупателя. */
public class CartFragment extends Fragment {
    private FragmentCartBinding b;
    private boolean hasProblems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentCartBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        App app = App.get(requireContext());
        long userId = app.session().getUserId();
        CartAdapter adapter = new CartAdapter(new CartAdapter.Listener() {
            @Override
            public void onOpen(CartLine line) {
                ListingDetailsActivity.open(requireContext(), line.listingId);
            }

            @Override
            public void onQuantity(CartLine line, int quantity) {
                app.orders().setCartQuantity(userId, line.listingId, quantity, Ui.callback(requireContext(), null));
            }

            @Override
            public void onRemove(CartLine line) {
                app.orders().removeFromCart(userId, line.listingId);
            }
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setAdapter(adapter);
        b.checkout.setOnClickListener(v -> {
            if (hasProblems) {
                Ui.toast(requireContext(), "Уберите из корзины вещи, которых нет в наличии");
                return;
            }
            CheckoutActivity.openCart(requireContext());
        });

        app.orders().observeCart(userId).observe(getViewLifecycleOwner(), lines -> {
            adapter.submitList(lines);
            bindSummary(lines);
        });
    }

    private void bindSummary(List<CartLine> lines) {
        boolean empty = lines == null || lines.isEmpty();
        Ui.showEmpty(b.empty, empty, R.drawable.ic_cart, "Корзина пуста",
                "Добавляйте вещи из каталога — оформить можно сразу у нескольких продавцов");
        b.summary.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) return;
        int total = 0;
        int count = 0;
        hasProblems = false;
        Set<Long> sellers = new HashSet<>();
        for (CartLine line : lines) {
            total += line.lineTotal();
            count += line.quantity;
            sellers.add(line.sellerId);
            if (!line.isAvailable()) hasProblems = true;
        }
        b.itemsLabel.setText("Товары: " + Format.plural(count, "вещь", "вещи", "вещей"));
        b.total.setText(Format.price(total));
        b.sellersHint.setText(sellers.size() > 1
                ? "Вещи от " + sellers.size() + " продавцов — будет оформлено "
                        + Format.plural(sellers.size(), "заказ", "заказа", "заказов")
                : "Стоимость доставки рассчитается при оформлении");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
