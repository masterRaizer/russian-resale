package com.russianresale.app.ui.catalog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.R;
import com.russianresale.app.data.entity.Listing;
import com.russianresale.app.data.model.ListingItem;
import com.russianresale.app.databinding.ItemListingBinding;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.Objects;

/** Карточки объявлений в сетке (каталог, избранное, профиль продавца). */
public class ListingAdapter extends ListAdapter<ListingItem, ListingAdapter.Holder> {

    public interface Listener {
        void onOpen(ListingItem item);

        void onFavorite(ListingItem item);
    }

    private final Listener listener;
    private final boolean showFavorite;

    public ListingAdapter(boolean showFavorite, Listener listener) {
        super(DIFF);
        this.showFavorite = showFavorite;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemListingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ListingItem item = getItem(position);
        Listing l = item.listing;
        ImageLoader.load(h.b.photo, l.mainPhoto());
        h.b.price.setText(Format.price(l.price));
        h.b.title.setText(l.title);
        h.b.meta.setText(l.size + " · " + l.conditionEnum().title);
        h.b.seller.setText(item.sellerName + (l.city == null ? "" : " · " + l.city));
        h.b.customBadge.setVisibility(l.madeToOrder ? View.VISIBLE : View.GONE);
        h.b.favorite.setVisibility(showFavorite ? View.VISIBLE : View.GONE);
        h.b.favorite.setImageResource(item.favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
        h.b.favorite.setOnClickListener(v -> listener.onFavorite(item));
        h.itemView.setOnClickListener(v -> listener.onOpen(item));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemListingBinding b;

        Holder(ItemListingBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    private static final DiffUtil.ItemCallback<ListingItem> DIFF = new DiffUtil.ItemCallback<ListingItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ListingItem a, @NonNull ListingItem b) {
            return a.listing.id == b.listing.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ListingItem a, @NonNull ListingItem b) {
            return a.favorite == b.favorite
                    && a.listing.price == b.listing.price
                    && a.listing.updatedAt == b.listing.updatedAt
                    && Objects.equals(a.listing.title, b.listing.title)
                    && Objects.equals(a.listing.photos, b.listing.photos)
                    && Objects.equals(a.listing.status, b.listing.status)
                    && Objects.equals(a.sellerName, b.sellerName);
        }
    };
}
