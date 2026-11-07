package com.russianresale.app.ui.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.databinding.ItemMyListingBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.EditListingActivity;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** «Мои объявления» продавца: создание, редактирование, снятие с продажи, удаление. */
public class MyListingsFragment extends Fragment {
    private static final int F_ACTIVE = 1;
    private static final int F_SOLD = 2;
    private static final int F_ARCHIVE = 3;
    private static final int F_ALL = 4;

    private FragmentListBinding b;
    private Adapter adapter;
    private List<Listing> all = new ArrayList<>();
    private App app;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentListBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        app = App.get(requireContext());
        userId = app.session().getUserId();

        b.chipsScroll.setVisibility(View.VISIBLE);
        Ui.addChoiceChip(b.chips, "В продаже", F_ACTIVE).setChecked(true);
        Ui.addChoiceChip(b.chips, "Проданные", F_SOLD);
        Ui.addChoiceChip(b.chips, "Архив и блокировки", F_ARCHIVE);
        Ui.addChoiceChip(b.chips, "Все", F_ALL);
        b.chips.setOnCheckedStateChangeListener((g, ids) -> applyFilter());

        b.fab.setVisibility(View.VISIBLE);
        b.fab.setText("Объявление");
        b.fab.setOnClickListener(v -> EditListingActivity.create(requireContext()));

        adapter = new Adapter();
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setAdapter(adapter);
        app.listings().observeMyListings(userId).observe(getViewLifecycleOwner(), list -> {
            all = list == null ? new ArrayList<>() : list;
            applyFilter();
        });
    }

    private void applyFilter() {
        int f = b.chips.getCheckedChipId();
        List<Listing> result = new ArrayList<>();
        for (Listing l : all) {
            ListingStatus s = l.statusEnum();
            boolean match = f == F_ALL
                    || (f == F_ACTIVE && s == ListingStatus.ACTIVE)
                    || (f == F_SOLD && s == ListingStatus.SOLD)
                    || (f == F_ARCHIVE && (s == ListingStatus.ARCHIVED || s == ListingStatus.BLOCKED));
            if (match) result.add(l);
        }
        adapter.submitList(result);
        Ui.showEmpty(b.empty, result.isEmpty(), R.drawable.ic_store, "Объявлений нет",
                all.isEmpty() ? "Нажмите «+ Объявление», чтобы выставить первую вещь" : "В этом разделе пока пусто");
    }

    private void showMenu(View anchor, Listing l) {
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        Menu m = menu.getMenu();
        ListingStatus s = l.statusEnum();
        if (s != ListingStatus.BLOCKED) m.add(0, 1, 0, "Редактировать");
        if (s == ListingStatus.ACTIVE) m.add(0, 2, 1, "Снять с продажи");
        if (s == ListingStatus.ARCHIVED) m.add(0, 3, 1, "Вернуть в продажу");
        m.add(0, 4, 2, "Удалить");
        menu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 1:
                    EditListingActivity.open(requireContext(), l.id);
                    break;
                case 2:
                case 3:
                    boolean archive = item.getItemId() == 2;
                    app.listings().setArchived(userId, l.id, archive, Ui.callback(requireContext(), ok ->
                            Ui.toast(requireContext(), archive ? "Снято с продажи" : "Снова в продаже")));
                    break;
                case 4:
                    Ui.confirm(requireContext(), "Удалить объявление?",
                            "«" + l.title + "» будет удалено без возможности восстановления. "
                                    + "Оформленные заказы сохранятся.", "Удалить",
                            () -> app.listings().delete(userId, l.id, Ui.callback(requireContext(), ok ->
                                    Ui.toast(requireContext(), "Объявление удалено"))));
                    break;
            }
            return true;
        });
        menu.show();
    }

    private class Adapter extends ListAdapter<Listing, Holder> {
        Adapter() {
            super(new DiffUtil.ItemCallback<Listing>() {
                @Override
                public boolean areItemsTheSame(@NonNull Listing a, @NonNull Listing b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Listing a, @NonNull Listing b) {
                    return a.updatedAt == b.updatedAt && a.views == b.views && a.quantity == b.quantity
                            && Objects.equals(a.status, b.status) && Objects.equals(a.title, b.title);
                }
            });
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemMyListingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Listing l = getItem(position);
            ImageLoader.load(h.b.photo, l.mainPhoto());
            Ui.bindListingStatus(h.b.status, l.statusEnum());
            h.b.title.setText(l.title);
            h.b.price.setText(Format.price(l.price));
            h.b.meta.setText("👁 " + l.views + " · в наличии " + l.quantity + " шт."
                    + (l.madeToOrder ? " · ✂ на заказ" : ""));
            h.b.action.setOnClickListener(v -> showMenu(v, l));
            h.itemView.setOnClickListener(v -> ListingDetailsActivity.open(requireContext(), l.id));
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemMyListingBinding b;

        Holder(ItemMyListingBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
