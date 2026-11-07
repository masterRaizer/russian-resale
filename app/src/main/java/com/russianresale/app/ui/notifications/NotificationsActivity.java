package com.russianresale.app.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.R;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.databinding.ActivityFragmentHostBinding;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.databinding.ItemNotificationBinding;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.ui.orders.OrderDetailsActivity;
import com.russianresale.app.ui.seller.SellerProfileActivity;
import com.russianresale.app.util.Format;

/** Уведомления о заказах, отзывах, жалобах и модерации. */
public class NotificationsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFragmentHostBinding host = ActivityFragmentHostBinding.inflate(getLayoutInflater());
        setContentView(host.getRoot());
        setupToolbar(host.toolbar, "Уведомления");
        FragmentListBinding b = FragmentListBinding.inflate(getLayoutInflater(), host.container, true);

        long uid = userId();
        Adapter adapter = new Adapter();
        b.recycler.setPadding(0, 0, 0, 0);
        b.recycler.setLayoutManager(new LinearLayoutManager(this));
        b.recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        b.recycler.setAdapter(adapter);
        app().notifications().observe(uid).observe(this, list -> {
            adapter.submitList(list);
            Ui.showEmpty(b.empty, list == null || list.isEmpty(), R.drawable.ic_notifications,
                    "Уведомлений нет", "Здесь появятся новости о заказах и отзывах");
        });

        host.toolbar.inflateMenu(R.menu.menu_notifications);
        host.toolbar.setOnMenuItemClickListener(item -> {
            Ui.confirm(this, "Очистить уведомления?", "Все уведомления будут удалены", "Очистить",
                    () -> app().notifications().clear(uid));
            return true;
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // всё, что пользователь увидел, считается прочитанным
        app().notifications().markAllRead(userId());
    }

    private void openTarget(AppNotification n) {
        if (n.type == null || n.refId == 0) return;
        switch (n.type) {
            case AppNotification.TYPE_ORDER:
                OrderDetailsActivity.open(this, n.refId);
                break;
            case AppNotification.TYPE_LISTING:
                ListingDetailsActivity.open(this, n.refId);
                break;
            case AppNotification.TYPE_REVIEW:
                SellerProfileActivity.open(this, n.refId);
                break;
            default:
                break;
        }
    }

    private static int iconFor(String type) {
        if (AppNotification.TYPE_ORDER.equals(type)) return R.drawable.ic_orders;
        if (AppNotification.TYPE_REVIEW.equals(type)) return R.drawable.ic_star;
        if (AppNotification.TYPE_LISTING.equals(type)) return R.drawable.ic_flag;
        if (AppNotification.TYPE_CHAT.equals(type)) return R.drawable.ic_chat;
        return R.drawable.ic_notifications;
    }

    private class Adapter extends ListAdapter<AppNotification, Holder> {
        Adapter() {
            super(new DiffUtil.ItemCallback<AppNotification>() {
                @Override
                public boolean areItemsTheSame(@NonNull AppNotification a, @NonNull AppNotification b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull AppNotification a, @NonNull AppNotification b) {
                    return a.read == b.read;
                }
            });
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            AppNotification n = getItem(position);
            h.b.icon.setImageResource(iconFor(n.type));
            h.b.title.setText(n.title);
            h.b.text.setText(n.text);
            h.b.time.setText(Format.relative(n.createdAt));
            h.b.unreadDot.setVisibility(n.read ? View.INVISIBLE : View.VISIBLE);
            h.itemView.setOnClickListener(v -> openTarget(n));
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemNotificationBinding b;

        Holder(ItemNotificationBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }
}
