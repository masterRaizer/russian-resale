package com.russianresale.app.ui.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
import com.russianresale.app.data.model.ListingItem;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.databinding.ItemMyListingBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Все объявления площадки для администратора: поиск и блокировка. */
public class AdminListingsFragment extends Fragment {
    private FragmentListBinding b;
    private Adapter adapter;
    private List<ListingItem> all = new ArrayList<>();
    private String query = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentListBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        App app = App.get(requireContext());
        long adminId = app.session().getUserId();

        EditText search = new EditText(requireContext());
        search.setHint("Поиск по названию или продавцу");
        search.setBackgroundResource(R.drawable.bg_input_bar);
        int p = Ui.dp(requireContext(), 14);
        search.setPadding(p, p / 2, p, p / 2);
        search.setSingleLine(true);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                Ui.dp(requireContext(), 44));
        lp.setMargins(Ui.dp(requireContext(), 16), Ui.dp(requireContext(), 4), Ui.dp(requireContext(), 16),
                Ui.dp(requireContext(), 8));
        b.header.addView(search, lp);
        b.header.setVisibility(View.VISIBLE);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                query = s.toString().trim().toLowerCase();
                applyFilter();
            }
        });

        adapter = new Adapter(item -> {
            boolean block = item.listing.statusEnum() != ListingStatus.BLOCKED;
            Ui.confirm(requireContext(), block ? "Заблокировать объявление?" : "Разблокировать объявление?",
                    "«" + item.listing.title + "»", block ? "Заблокировать" : "Разблокировать",
                    () -> app.admin().setListingBlocked(adminId, item.listing.id, block, Ui.callback(requireContext(), null)));
        });
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setAdapter(adapter);
        app.admin().observeListings().observe(getViewLifecycleOwner(), list -> {
            all = list == null ? new ArrayList<>() : list;
            applyFilter();
        });
    }

    private void applyFilter() {
        List<ListingItem> result = new ArrayList<>();
        for (ListingItem item : all) {
            String hay = (item.listing.title + " " + item.sellerName).toLowerCase();
            if (query.isEmpty() || hay.contains(query)) result.add(item);
        }
        adapter.submitList(result);
        Ui.showEmpty(b.empty, result.isEmpty(), R.drawable.ic_store, "Объявлений не найдено", null);
    }

    private interface OnAction {
        void onAction(ListingItem item);
    }

    private class Adapter extends ListAdapter<ListingItem, Holder> {
        private final OnAction onAction;

        Adapter(OnAction onAction) {
            super(new DiffUtil.ItemCallback<ListingItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ListingItem a, @NonNull ListingItem b) {
                    return a.listing.id == b.listing.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull ListingItem a, @NonNull ListingItem b) {
                    return Objects.equals(a.listing.status, b.listing.status) && a.listing.updatedAt == b.listing.updatedAt;
                }
            });
            this.onAction = onAction;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemMyListingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            ListingItem item = getItem(position);
            Listing l = item.listing;
            ImageLoader.load(h.b.photo, l.mainPhoto());
            Ui.bindListingStatus(h.b.status, l.statusEnum());
            h.b.title.setText(l.title);
            h.b.price.setText(Format.price(l.price));
            h.b.meta.setText(item.sellerName + " · 👁 " + l.views);
            h.b.action.setIconResource(l.statusEnum() == ListingStatus.BLOCKED ? R.drawable.ic_check : R.drawable.ic_block);
            h.b.action.setContentDescription(l.statusEnum() == ListingStatus.BLOCKED ? "Разблокировать" : "Заблокировать");
            h.b.action.setOnClickListener(v -> onAction.onAction(item));
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
