package com.russianresale.app.ui.catalog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.model.Category;
import com.russianresale.app.data.model.ListingFilter;
import com.russianresale.app.data.model.ListingItem;
import com.russianresale.app.data.model.SortOrder;
import com.russianresale.app.databinding.FragmentCatalogBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.util.Format;

/** Каталог для покупателя: поиск, категории, фильтры, сортировка. */
public class CatalogFragment extends Fragment {
    private static final int CHIP_ALL = 1000;

    private FragmentCatalogBinding b;
    private CatalogViewModel vm;
    private ListingAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingSearch;
    private boolean binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentCatalogBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        vm = new ViewModelProvider(requireActivity()).get(CatalogViewModel.class);
        App app = App.get(requireContext());
        long userId = app.session().getUserId();

        adapter = new ListingAdapter(true, new ListingAdapter.Listener() {
            @Override
            public void onOpen(ListingItem item) {
                ListingDetailsActivity.open(requireContext(), item.listing.id);
            }

            @Override
            public void onFavorite(ListingItem item) {
                app.listings().toggleFavorite(userId, item.listing.id, Ui.callback(requireContext(), fav ->
                        Ui.toast(requireContext(), fav ? "Добавлено в избранное" : "Удалено из избранного")));
            }
        });
        b.recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        b.recycler.setAdapter(adapter);

        setupCategories();
        setupSearch();
        b.customChip.setOnCheckedChangeListener((chip, checked) -> {
            if (binding) return;
            ListingFilter f = vm.current();
            f.madeToOrder = checked ? Boolean.TRUE : null;
            vm.apply(f);
        });
        b.sortButton.setOnClickListener(v -> chooseSort());
        b.filterButton.setOnClickListener(v -> new FilterBottomSheet().show(getChildFragmentManager(), "filter"));

        vm.getFilter().observe(getViewLifecycleOwner(), this::bindFilter);
        vm.getResults().observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            int n = list == null ? 0 : list.size();
            b.resultCount.setText(Format.plural(n, "вещь", "вещи", "вещей"));
            Ui.showEmpty(b.empty, n == 0, R.drawable.ic_search, "Ничего не нашлось",
                    "Попробуйте изменить запрос или сбросить фильтры");
        });
    }

    private void setupCategories() {
        Chip all = Ui.addChoiceChip(b.categories, "Все", CHIP_ALL);
        all.setChecked(true);
        for (Category c : Category.values()) {
            Chip chip = Ui.addChoiceChip(b.categories, c.title, c.ordinal() + 1);
            chip.setChipIconResource(c.icon);
            chip.setChipIconVisible(true);
        }
        b.categories.setOnCheckedStateChangeListener((group, ids) -> {
            if (binding || ids.isEmpty()) return;
            int id = ids.get(0);
            ListingFilter f = vm.current();
            f.category = id == CHIP_ALL ? null : Category.values()[id - 1];
            vm.apply(f);
        });
    }

    private void setupSearch() {
        b.search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding) return;
                if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
                String query = s.toString();
                pendingSearch = () -> {
                    ListingFilter f = vm.current();
                    f.query = query;
                    vm.apply(f);
                };
                handler.postDelayed(pendingSearch, 300);
            }
        });
    }

    private void chooseSort() {
        SortOrder[] values = SortOrder.values();
        String[] titles = new String[values.length];
        for (int i = 0; i < values.length; i++) titles[i] = values[i].title;
        ListingFilter current = vm.current();
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Сортировка")
                .setSingleChoiceItems(titles, current.sort.ordinal(), (dialog, which) -> {
                    ListingFilter f = vm.current();
                    f.sort = values[which];
                    vm.apply(f);
                    dialog.dismiss();
                })
                .show();
    }

    /** Синхронизирует элементы управления с фильтром (например, после поворота экрана). */
    private void bindFilter(ListingFilter f) {
        binding = true;
        int chipId = f.category == null ? CHIP_ALL : f.category.ordinal() + 1;
        if (b.categories.getCheckedChipId() != chipId) b.categories.check(chipId);
        b.customChip.setChecked(Boolean.TRUE.equals(f.madeToOrder));
        int extra = f.activeCount() - (f.madeToOrder != null ? 1 : 0);
        b.filterButton.setText(extra > 0 ? "Фильтры · " + extra : "Фильтры");
        binding = false;
    }

    @Override
    public void onDestroyView() {
        if (pendingSearch != null) handler.removeCallbacks(pendingSearch);
        super.onDestroyView();
        b = null;
    }
}
