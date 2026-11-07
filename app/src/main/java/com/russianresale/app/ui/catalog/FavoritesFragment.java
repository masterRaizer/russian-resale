package com.russianresale.app.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.model.ListingItem;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;

/** Избранное покупателя. */
public class FavoritesFragment extends Fragment {
    private FragmentListBinding b;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentListBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        App app = App.get(requireContext());
        long userId = app.session().getUserId();
        ListingAdapter adapter = new ListingAdapter(true, new ListingAdapter.Listener() {
            @Override
            public void onOpen(ListingItem item) {
                ListingDetailsActivity.open(requireContext(), item.listing.id);
            }

            @Override
            public void onFavorite(ListingItem item) {
                app.listings().toggleFavorite(userId, item.listing.id, Ui.callback(requireContext(), null));
            }
        });
        b.recycler.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        b.recycler.setAdapter(adapter);
        app.listings().observeFavorites(userId).observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            Ui.showEmpty(b.empty, list == null || list.isEmpty(), R.drawable.ic_favorite_border,
                    "В избранном пусто", "Нажимайте ♥ на карточках, чтобы сохранить понравившиеся вещи");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
