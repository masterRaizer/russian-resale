package com.russianresale.app.ui.seller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.databinding.FragmentListBinding;
import com.russianresale.app.databinding.ViewRatingHeaderBinding;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.util.Format;

/**
 * Отзывы. Для продавца — отзывы о нём с возможностью ответить (если это его профиль),
 * для покупателя — отзывы, которые он оставил.
 */
public class ReviewsFragment extends Fragment {
    private static final String ARG_SELLER_ID = "seller_id";
    private static final String ARG_BUYER = "buyer";

    private FragmentListBinding b;

    public static ReviewsFragment forSeller(long sellerId) {
        ReviewsFragment f = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_SELLER_ID, sellerId);
        f.setArguments(args);
        return f;
    }

    public static ReviewsFragment forBuyer() {
        ReviewsFragment f = new ReviewsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_BUYER, true);
        f.setArguments(args);
        return f;
    }

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
        b.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        b.recycler.setPadding(Ui.dp(requireContext(), 12), b.recycler.getPaddingTop(),
                Ui.dp(requireContext(), 12), b.recycler.getPaddingBottom());

        if (requireArguments().getBoolean(ARG_BUYER)) {
            ReviewAdapter adapter = new ReviewAdapter(ReviewAdapter.MODE_BUYER, null);
            b.recycler.setAdapter(adapter);
            app.orders().observeBuyerReviews(userId).observe(getViewLifecycleOwner(), list -> {
                adapter.submitList(list);
                Ui.showEmpty(b.empty, list == null || list.isEmpty(), R.drawable.ic_star, "Вы ещё не оставляли отзывов",
                        "Оценить вещь можно в деталях заказа после его получения");
            });
            return;
        }

        long sellerId = requireArguments().getLong(ARG_SELLER_ID);
        boolean own = sellerId == userId && app.session().isSeller();
        ReviewAdapter.OnReply onReply = null;
        if (own) {
            onReply = item -> Ui.inputDialog(requireContext(), "Ответ на отзыв", "Ваш ответ покупателю",
                    item.review.sellerReply, true, "Опубликовать",
                    text -> app.orders().replyToReview(userId, item.review.id, text,
                            Ui.callback(requireContext(), ok -> Ui.toast(requireContext(), "Ответ опубликован"))));
        }
        ReviewAdapter adapter = new ReviewAdapter(ReviewAdapter.MODE_SELLER, onReply);
        b.recycler.setAdapter(adapter);

        ViewRatingHeaderBinding header = ViewRatingHeaderBinding.inflate(getLayoutInflater(), b.header, true);
        b.header.setVisibility(View.VISIBLE);
        app.orders().observeSellerRating(sellerId).observe(getViewLifecycleOwner(), r -> {
            double avg = r == null ? 0 : r.average;
            int count = r == null ? 0 : r.count;
            header.average.setText(count == 0 ? "—" : Format.rating(avg));
            header.stars.setRating((float) avg);
            header.count.setText(count == 0 ? "Пока нет оценок"
                    : Format.plural(count, "отзыв", "отзыва", "отзывов") + " покупателей");
        });
        app.orders().observeSellerReviews(sellerId).observe(getViewLifecycleOwner(), list -> {
            adapter.submitList(list);
            Ui.showEmpty(b.empty, list == null || list.isEmpty(), R.drawable.ic_star, "Отзывов пока нет",
                    own ? "Покупатели оставляют отзывы после получения заказа" : null);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
