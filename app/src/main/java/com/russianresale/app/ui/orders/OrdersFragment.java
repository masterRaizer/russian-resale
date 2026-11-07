package com.russianresale.app.ui.orders;

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
import com.russianresale.app.data.model.OrderCard;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.ui.common.Ui;

import java.util.ArrayList;
import java.util.List;

/** Заказы: у покупателя — его покупки, у продавца — входящие заказы. */
public class OrdersFragment extends Fragment {
    private static final String ARG_SELLER = "seller";
    private static final int FILTER_ALL = 1;
    private static final int FILTER_ACTIVE = 2;
    private static final int FILTER_DONE = 3;

    private FragmentListBinding b;
    private OrderAdapter adapter;
    private List<OrderCard> all = new ArrayList<>();
    private boolean seller;

    public static OrdersFragment forBuyer() {
        return create(false);
    }

    public static OrdersFragment forSeller() {
        return create(true);
    }

    private static OrdersFragment create(boolean seller) {
        OrdersFragment f = new OrdersFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_SELLER, seller);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentListBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        seller = requireArguments().getBoolean(ARG_SELLER);
        App app = App.get(requireContext());
        long userId = app.session().getUserId();

        b.chipsScroll.setVisibility(View.VISIBLE);
        Ui.addChoiceChip(b.chips, "Активные", FILTER_ACTIVE).setChecked(true);
        Ui.addChoiceChip(b.chips, "Завершённые", FILTER_DONE);
        Ui.addChoiceChip(b.chips, "Все", FILTER_ALL);
        b.chips.setOnCheckedStateChangeListener((g, ids) -> applyFilter());

        adapter = new OrderAdapter(seller, card -> OrderDetailsActivity.open(requireContext(), card.order.id));
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setAdapter(adapter);

        (seller ? app.orders().observeSellerOrders(userId) : app.orders().observeBuyerOrders(userId))
                .observe(getViewLifecycleOwner(), list -> {
                    all = list == null ? new ArrayList<>() : list;
                    applyFilter();
                });
    }

    private void applyFilter() {
        int filter = b.chips.getCheckedChipId();
        List<OrderCard> result = new ArrayList<>();
        for (OrderCard c : all) {
            boolean done = c.order.statusEnum().isFinal();
            if (filter == FILTER_ALL || (filter == FILTER_DONE) == done) result.add(c);
        }
        adapter.submitList(result);
        String text = seller
                ? (filter == FILTER_DONE ? "Завершённых заказов пока нет" : "Когда покупатели оформят заказ, он появится здесь")
                : (filter == FILTER_DONE ? "Здесь будут полученные и отменённые заказы" : "Оформите заказ в каталоге — он появится здесь");
        Ui.showEmpty(b.empty, result.isEmpty(), R.drawable.ic_orders, "Заказов нет", text);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
