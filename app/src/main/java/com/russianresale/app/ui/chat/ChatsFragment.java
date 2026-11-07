package com.russianresale.app.ui.chat;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.model.ChatItem;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.databinding.ItemChatBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

/** Список переписок пользователя. */
public class ChatsFragment extends Fragment {
    private FragmentListBinding b;
    private long userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentListBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        App app = App.get(requireContext());
        userId = app.session().getUserId();
        Adapter adapter = new Adapter();
        b.recycler.setPadding(0, 0, 0, 0);
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        b.recycler.setAdapter(adapter);
        boolean seller = app.session().isSeller();
        app.chats().observeChats(userId).observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            Ui.showEmpty(b.empty, list == null || list.isEmpty(), R.drawable.ic_chat, "Сообщений пока нет",
                    seller ? "Здесь появятся вопросы покупателей о ваших вещах"
                            : "Задайте вопрос продавцу кнопкой чата на странице вещи");
        });
    }

    private class Adapter extends ListAdapter<ChatItem, Holder> {
        Adapter() {
            super(new DiffUtil.ItemCallback<ChatItem>() {
                @Override
                public boolean areItemsTheSame(@NonNull ChatItem a, @NonNull ChatItem b) {
                    return a.chat.id == b.chat.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull ChatItem a, @NonNull ChatItem b) {
                    return a.chat.updatedAt == b.chat.updatedAt && a.chat.unreadBuyer == b.chat.unreadBuyer
                            && a.chat.unreadSeller == b.chat.unreadSeller;
                }
            });
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(ItemChatBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            ChatItem c = getItem(position);
            boolean iAmBuyer = c.chat.buyerId == userId;
            int unread = iAmBuyer ? c.chat.unreadBuyer : c.chat.unreadSeller;
            ImageLoader.load(h.b.photo, ImageLoader.first(c.listingPhoto));
            h.b.name.setText(iAmBuyer ? c.sellerName : c.buyerName);
            h.b.listing.setText(c.listingTitle == null ? "Объявление удалено" : c.listingTitle);
            h.b.last.setText(c.chat.lastMessage);
            h.b.last.setTypeface(unread > 0 ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            h.b.time.setText(Format.relative(c.chat.updatedAt));
            h.b.unread.setVisibility(unread > 0 ? View.VISIBLE : View.GONE);
            h.b.unread.setText(String.valueOf(unread));
            h.itemView.setOnClickListener(v -> ChatActivity.open(requireContext(), c.chat.id));
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemChatBinding b;

        Holder(ItemChatBinding b) {
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
