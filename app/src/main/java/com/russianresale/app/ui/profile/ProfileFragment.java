package com.russianresale.app.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.russianresale.app.App;
import com.russianresale.app.BuildConfig;
import com.russianresale.app.R;
import com.russianresale.app.data.SessionManager;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.AdminStats;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.data.model.SellerStats;
import com.russianresale.app.databinding.FragmentProfileBinding;
import com.russianresale.app.databinding.ItemMenuRowBinding;
import com.russianresale.app.databinding.ItemStatTileBinding;
import com.russianresale.app.ui.common.FragmentHostActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.listing.EditListingActivity;
import com.russianresale.app.ui.main.MainActivity;
import com.russianresale.app.ui.notifications.NotificationsActivity;
import com.russianresale.app.ui.seller.SellerProfileActivity;
import com.russianresale.app.util.Format;
import com.russianresale.app.util.ImageLoader;
import com.russianresale.app.util.PriceCalculator;
import com.russianresale.app.util.ThemeSettings;

/** Профиль: данные пользователя, статистика (продавец, администратор) и настройки. */
public class ProfileFragment extends Fragment {
    private FragmentProfileBinding b;
    private App app;
    private SessionManager session;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        b = FragmentProfileBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        app = App.get(requireContext());
        session = app.session();
        long uid = session.getUserId();
        Role role = session.getRole();

        app.auth().observeUser(uid).observe(getViewLifecycleOwner(), this::bindUser);
        b.version.setText("«Русский ресейл», версия " + BuildConfig.VERSION_NAME);

        if (role == Role.SELLER) {
            row(R.drawable.ic_store, "Мой публичный профиль", "Так вас видят покупатели",
                    () -> SellerProfileActivity.open(requireContext(), uid));
            row(R.drawable.ic_add, "Новое объявление", null, () -> EditListingActivity.create(requireContext()));
        } else if (role == Role.BUYER) {
            row(R.drawable.ic_chat, "Сообщения", "Переписка с продавцами",
                    () -> FragmentHostActivity.open(requireContext(), FragmentHostActivity.SCREEN_CHATS));
            row(R.drawable.ic_star, "Мои отзывы", null,
                    () -> FragmentHostActivity.open(requireContext(), FragmentHostActivity.SCREEN_MY_REVIEWS));
        }
        row(R.drawable.ic_notifications, "Уведомления", null,
                () -> startActivity(new Intent(requireContext(), NotificationsActivity.class)));
        if (role != Role.ADMIN) {
            row(R.drawable.ic_edit, "Редактировать профиль", null,
                    () -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        }
        row(R.drawable.ic_lock, "Сменить пароль", null, this::changePassword);
        row(R.drawable.ic_visibility, "Тема оформления", ThemeSettings.TITLES[ThemeSettings.index(requireContext())],
                this::chooseTheme);
        row(R.drawable.ic_info, "О приложении", null, this::about);
        row(R.drawable.ic_logout, "Выйти", null, () -> Ui.confirm(requireContext(), "Выйти из аккаунта?", null, "Выйти",
                () -> ((MainActivity) requireActivity()).logout()));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStats();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadStats();
    }

    private void bindUser(User u) {
        if (u == null || b == null) return;
        ImageLoader.loadAvatar(b.avatar, u.avatarPath, u.displayName());
        b.name.setText(u.displayName());
        String sub = u.email;
        if (u.isSeller()) sub = u.fullName + " · " + u.email;
        if (u.city != null && !u.city.isEmpty()) sub += "\n" + u.city;
        b.subtitle.setText(sub);
        int color = u.isSeller() ? R.color.secondary : u.isAdmin() ? R.color.error : R.color.primary;
        Ui.tintLabel(b.role, u.roleEnum().title, color);
    }

    private void loadStats() {
        if (b == null) return;
        Role role = session.getRole();
        long uid = session.getUserId();
        if (role == Role.SELLER) {
            app.orders().sellerStats(uid, Ui.callback(requireContext(), (SellerStats s) -> {
                if (b == null) return;
                showStatsTitle("Статистика продаж");
                b.stats.removeAllViews();
                LinearLayout row1 = statsRow();
                tile(row1, String.valueOf(s.newOrders), "новых заказов");
                tile(row1, String.valueOf(s.ordersInProgress), "в работе");
                tile(row1, String.valueOf(s.activeListings), "в продаже");
                LinearLayout row2 = statsRow();
                tile(row2, String.valueOf(s.soldItems), "продано вещей");
                tile(row2, Format.price(s.revenue), "выручка");
                tile(row2, s.reviewsCount == 0 ? "—" : "★ " + Format.rating(s.rating), Format.plural(s.reviewsCount, "отзыв", "отзыва", "отзывов"));
                b.statsNote.setVisibility(View.VISIBLE);
                b.statsNote.setText("Комиссия площадки " + PriceCalculator.COMMISSION_PERCENT + "% — "
                        + Format.price(PriceCalculator.commission(s.revenue)) + ". Учитываются только полученные заказы.");
            }));
        } else if (role == Role.ADMIN) {
            app.admin().stats(Ui.callback(requireContext(), (AdminStats s) -> {
                if (b == null) return;
                showStatsTitle("Площадка");
                b.stats.removeAllViews();
                LinearLayout row1 = statsRow();
                tile(row1, String.valueOf(s.buyers), "покупателей");
                tile(row1, String.valueOf(s.sellers), "продавцов");
                tile(row1, String.valueOf(s.activeListings), "объявлений");
                LinearLayout row2 = statsRow();
                tile(row2, String.valueOf(s.orders), "заказов");
                tile(row2, Format.price(s.turnover), "оборот");
                tile(row2, String.valueOf(s.openComplaints), "открытых жалоб");
            }));
        }
    }

    private void showStatsTitle(String title) {
        b.statsTitle.setVisibility(View.VISIBLE);
        b.statsTitle.setText(title);
        b.stats.setVisibility(View.VISIBLE);
    }

    private LinearLayout statsRow() {
        LinearLayout row = new LinearLayout(requireContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        b.stats.addView(row, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private void tile(LinearLayout row, String value, String label) {
        ItemStatTileBinding t = ItemStatTileBinding.inflate(getLayoutInflater(), row, true);
        t.value.setText(value);
        t.label.setText(label);
    }

    private void row(@DrawableRes int icon, String title, String subtitle, Runnable action) {
        ItemMenuRowBinding r = ItemMenuRowBinding.inflate(getLayoutInflater(), b.menu, true);
        r.icon.setImageResource(icon);
        r.title.setText(title);
        r.subtitle.setVisibility(subtitle == null ? View.GONE : View.VISIBLE);
        r.subtitle.setText(subtitle);
        r.getRoot().setOnClickListener(v -> action.run());
    }

    private void changePassword() {
        View view = getLayoutInflater().inflate(R.layout.dialog_change_password, null, false);
        TextInputEditText oldP = view.findViewById(R.id.oldPassword);
        TextInputEditText newP = view.findViewById(R.id.newPassword);
        TextInputEditText newP2 = view.findViewById(R.id.newPassword2);
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Смена пароля")
                .setView(view)
                .setNegativeButton("Отмена", null)
                .setPositiveButton("Сохранить", (d, w) -> {
                    String p1 = newP.getText() == null ? "" : newP.getText().toString();
                    String p2 = newP2.getText() == null ? "" : newP2.getText().toString();
                    if (!p1.equals(p2)) {
                        Ui.toast(requireContext(), "Новые пароли не совпадают");
                        return;
                    }
                    String old = oldP.getText() == null ? "" : oldP.getText().toString();
                    app.auth().changePassword(session.getUserId(), old, p1,
                            Ui.callback(requireContext(), ok -> Ui.toast(requireContext(), "Пароль изменён")));
                })
                .show();
    }

    private void chooseTheme() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Тема оформления")
                .setSingleChoiceItems(ThemeSettings.TITLES, ThemeSettings.index(requireContext()), (d, which) -> {
                    d.dismiss();
                    ThemeSettings.set(requireContext().getApplicationContext(), which);
                })
                .show();
    }

    private void about() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("«Русский ресейл»")
                .setMessage("Площадка для покупки и продажи одежды, обуви и аксессуаров: от винтажа "
                        + "до вещей, сшитых в ателье по меркам покупателя.\n\n"
                        + "• Покупатель выбирает вещи, оформляет заказы, переписывается с продавцами и оставляет отзывы.\n"
                        + "• Продавец (ателье, мастерская, магазин) размещает объявления и обрабатывает заказы.\n"
                        + "• Администратор рассматривает жалобы и следит за порядком.\n\n"
                        + "Курсовой проект. Java, Android SDK, Room (SQLite), Material Design 3.")
                .setPositiveButton("Понятно", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        b = null;
    }
}
