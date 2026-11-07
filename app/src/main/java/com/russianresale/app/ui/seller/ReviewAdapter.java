package com.russianresale.app.ui.seller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.russianresale.app.data.model.ReviewItem;
import com.russianresale.app.databinding.ItemReviewBinding;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;

import java.util.Objects;

/** Список отзывов. Режимы: на странице вещи, в профиле продавца (с ответом), «мои отзывы» покупателя. */
public class ReviewAdapter extends ListAdapter<ReviewItem, ReviewAdapter.Holder> {
    public static final int MODE_LISTING = 0;
    public static final int MODE_SELLER = 1;
    public static final int MODE_BUYER = 2;

    public interface OnReply {
        void onReply(ReviewItem item);
    }

    private final int mode;
    private final OnReply onReply;

    public ReviewAdapter(int mode, OnReply onReply) {
        super(DIFF);
        this.mode = mode;
        this.onReply = onReply;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemReviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder h, int position) {
        ReviewItem item = getItem(position);
        String author = mode == MODE_BUYER ? item.sellerName : item.buyerName;
        ImageLoader.loadAvatar(h.b.avatar, null, author);
        h.b.author.setText(mode == MODE_BUYER ? "Продавец: " + author : author);
        h.b.date.setText(Format.date(item.review.createdAt));
        h.b.rating.setRating(item.review.rating);
        h.b.text.setText(item.review.text);
        boolean showSubject = mode != MODE_LISTING && item.listingTitle != null;
        h.b.subject.setVisibility(showSubject ? View.VISIBLE : View.GONE);
        h.b.subject.setText(item.listingTitle);

        boolean hasReply = item.review.sellerReply != null && !item.review.sellerReply.isEmpty();
        h.b.replyBlock.setVisibility(hasReply ? View.VISIBLE : View.GONE);
        if (hasReply) {
            h.b.replyTitle.setText("Ответ продавца · " + Format.date(item.review.replyAt));
            h.b.replyText.setText(item.review.sellerReply);
        }
        boolean canReply = onReply != null;
        h.b.replyButton.setVisibility(canReply ? View.VISIBLE : View.GONE);
        h.b.replyButton.setText(hasReply ? "Изменить ответ" : "Ответить");
        h.b.replyButton.setOnClickListener(v -> onReply.onReply(item));
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ItemReviewBinding b;

        Holder(ItemReviewBinding b) {
            super(b.getRoot());
            this.b = b;
        }
    }

    private static final DiffUtil.ItemCallback<ReviewItem> DIFF = new DiffUtil.ItemCallback<ReviewItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull ReviewItem a, @NonNull ReviewItem b) {
            return a.review.id == b.review.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ReviewItem a, @NonNull ReviewItem b) {
            return Objects.equals(a.review.sellerReply, b.review.sellerReply)
                    && Objects.equals(a.review.text, b.review.text)
                    && a.review.rating == b.review.rating;
        }
    };
}
