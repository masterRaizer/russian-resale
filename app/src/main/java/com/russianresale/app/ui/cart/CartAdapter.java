package com.russianresale.app.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.data.model.CartLine;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.databinding.ItemCartBinding;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.Objects;

public class CartAdapter extends ListAdapter<CartLine, CartAdapter.Holder> {

    public interface Listener {
        void onOpen(CartLine line);

        void onQuantity(CartLine line, int quantity);

        void onRemove(CartLine line);
    }

    private final Listener listener;

    public CartAdapter(Listener listener) {
        super(DIFF);
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        CartLine line = getItem(position);
        ImageLoader.load(h.b.photo, line.mainPhoto());
        h.b.title.setText(line.title);
        h.b.meta.setText(line.sellerName + " · размер " + line.size + (line.madeToOrder ? " · ✂ на заказ" : ""));
        h.b.price.setText(Format.price(line.lineTotal()));
        h.b.quantity.setText(String.valueOf(line.quantity));
        String warning = null;
        if (!ListingStatus.ACTIVE.name().equals(line.status)) warning = "Больше не продаётся";
        else if (line.stock < line.quantity) warning = "В наличии только " + line.stock + " шт.";
        h.b.warning.setVisibility(warning == null ? View.GONE : View.VISIBLE);
        h.b.warning.setText(warning);
        h.b.minus.setEnabled(line.quantity > 1);
        h.b.plus.setEnabled(line.quantity < line.stock);
        h.b.minus.setOnClickListener(v -> listener.onQuantity(line, line.quantity - 1));
        h.b.plus.setOnClickListener(v -> listener.onQuantity(line, line.quantity + 1));
        h.b.remove.setOnClickListener(v -> listener.onRemove(line));
        h.itemView.setOnClickListener(v -> listener.onOpen(line));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemCartBinding b;

        Holder(ItemCartBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    private static final DiffUtil.ItemCallback<CartLine> DIFF = new DiffUtil.ItemCallback<CartLine>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartLine a, @NonNull CartLine b) {
            return a.listingId == b.listingId;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartLine a, @NonNull CartLine b) {
            return a.quantity == b.quantity && a.price == b.price && a.stock == b.stock
                    && Objects.equals(a.status, b.status) && Objects.equals(a.title, b.title);
        }
    };
}
