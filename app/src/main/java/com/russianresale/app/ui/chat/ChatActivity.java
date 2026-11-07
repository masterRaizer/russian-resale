package com.russianresale.app.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.R;
import com.russianresale.app.data.entity.Message;
import com.russianresale.app.data.model.ChatItem;
import com.russianresale.app.databinding.ActivityChatBinding;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.ListingDetailsActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

/** Переписка покупателя и продавца по объявлению. */
public class ChatActivity extends BaseActivity {
    private static final String EXTRA_ID = "chat_id";
    private static final int TYPE_IN = 0;
    private static final int TYPE_OUT = 1;

    private ActivityChatBinding b;
    private long chatId;

    public static void open(Context context, long chatId) {
        context.startActivity(new Intent(context, ChatActivity.class).putExtra(EXTRA_ID, chatId));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "Чат");
        chatId = getIntent().getLongExtra(EXTRA_ID, 0);
        long uid = userId();

        MessageAdapter adapter = new MessageAdapter(uid);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        b.messages.setLayoutManager(lm);
        b.messages.setAdapter(adapter);

        app().chats().observeChat(chatId).observe(this, item -> {
            if (item == null) {
                finish();
                return;
            }
            if (item.chat.buyerId != uid && item.chat.sellerId != uid) {
                toast("Нет доступа к чату");
                finish();
                return;
            }
            bindHeader(item, uid);
        });
        app().chats().observeMessages(chatId).observe(this, list -> {
            adapter.submitList(list, () -> {
                if (adapter.getItemCount() > 0) b.messages.scrollToPosition(adapter.getItemCount() - 1);
            });
            app().chats().markRead(uid, chatId);
        });

        b.send.setOnClickListener(v -> {
            String text = Ui.text(b.input);
            if (text.isEmpty()) return;
            b.send.setEnabled(false);
            app().chats().send(uid, chatId, text, new com.russianresale.app.data.repo.Callback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    b.input.setText("");
                    b.send.setEnabled(true);
                }

                @Override
                public void onError(String message) {
                    b.send.setEnabled(true);
                    toast(message);
                }
            });
        });
    }

    private void bindHeader(ChatItem item, long uid) {
        boolean iAmBuyer = item.chat.buyerId == uid;
        b.toolbar.setTitle(iAmBuyer ? item.sellerName : item.buyerName);
        b.toolbar.setSubtitle(iAmBuyer ? "Продавец" : "Покупатель");
        ImageLoader.load(b.listingPhoto, ImageLoader.first(item.listingPhoto));
        b.listingTitle.setText(item.listingTitle == null ? "Объявление удалено" : item.listingTitle);
        b.listingBar.setOnClickListener(v -> {
            if (item.listingTitle != null) ListingDetailsActivity.open(this, item.chat.listingId);
        });
    }

    private static class MessageAdapter extends ListAdapter<Message, MessageAdapter.Holder> {
        private final long me;

        MessageAdapter(long me) {
            super(new DiffUtil.ItemCallback<Message>() {
                @Override
                public boolean areItemsTheSame(@NonNull Message a, @NonNull Message b) {
                    return a.id == b.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Message a, @NonNull Message b) {
                    return true;
                }
            });
            this.me = me;
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).senderId == me ? TYPE_OUT : TYPE_IN;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int layout = viewType == TYPE_OUT ? R.layout.item_message_out : R.layout.item_message_in;
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Message m = getItem(position);
            h.text.setText(m.text);
            h.time.setText(Format.relative(m.createdAt));
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView text;
            final TextView time;

            Holder(android.view.View view) {
                super(view);
                text = view.findViewById(R.id.text);
                time = view.findViewById(R.id.time);
            }
        }
    }
}
