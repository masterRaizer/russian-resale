package com.russianresale.app.ui.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.russianresale.app.databinding.ActivityFragmentHostBinding;
import com.russianresale.app.ui.chat.ChatsFragment;
import com.russianresale.app.ui.seller.ReviewsFragment;

/** Отдельный экран для фрагмента, который открывается из профиля (сообщения, мои отзывы). */
public class FragmentHostActivity extends BaseActivity {
    private static final String EXTRA_SCREEN = "screen";
    public static final String SCREEN_CHATS = "chats";
    public static final String SCREEN_MY_REVIEWS = "my_reviews";

    public static void open(Context context, String screen) {
        context.startActivity(new Intent(context, FragmentHostActivity.class).putExtra(EXTRA_SCREEN, screen));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityFragmentHostBinding b = ActivityFragmentHostBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        String screen = getIntent().getStringExtra(EXTRA_SCREEN);
        Fragment fragment;
        if (SCREEN_MY_REVIEWS.equals(screen)) {
            setupToolbar(b.toolbar, "Мои отзывы");
            fragment = ReviewsFragment.forBuyer();
        } else {
            setupToolbar(b.toolbar, "Сообщения");
            fragment = new ChatsFragment();
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(b.container.getId(), fragment).commit();
        }
    }
}
