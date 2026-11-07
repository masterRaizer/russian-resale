package com.russianresale.app.ui.orders;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.data.model.OrderCard;
import com.russianresale.app.databinding.ItemOrderBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.Objects;

public class OrderAdapter extends ListAdapter<OrderCard, OrderAdapter.Holder> {

    public interface Listener {
        void onOpen(OrderCard card);
    }

    private final boolean sellerView;
    private final Listener listener;

    public OrderAdapter(boolean sellerView, Listener listener) {
        super(DIFF);
        this.sellerView = sellerView;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        OrderCard c = getItem(position);
        ImageLoader.load(h.b.photo, c.photo);
        h.b.number.setText("Заказ " + Format.orderNumber(c.order.id));
        Ui.bindOrderStatus(h.b.status, c.order.statusEnum());
        String title = c.firstTitle == null ? "" : c.firstTitle;
        if (c.itemsCount > 1) title += " и ещё " + (c.itemsCount - 1);
        h.b.title.setText(title);
        h.b.party.setText(sellerView ? "Покупатель: " + c.buyerName : "Продавец: " + c.sellerName);
        h.b.date.setText(Format.dateTime(c.order.createdAt) + " · " + c.order.deliveryEnum().title);
        h.b.total.setText(Format.price(c.order.total));
        h.itemView.setOnClickListener(v -> listener.onOpen(c));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemOrderBinding b;

        Holder(ItemOrderBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    private static final DiffUtil.ItemCallback<OrderCard> DIFF = new DiffUtil.ItemCallback<OrderCard>() {
        @Override
        public boolean areItemsTheSame(@NonNull OrderCard a, @NonNull OrderCard b) {
            return a.order.id == b.order.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull OrderCard a, @NonNull OrderCard b) {
            return a.order.updatedAt == b.order.updatedAt && Objects.equals(a.order.status, b.order.status);
        }
    };
}
