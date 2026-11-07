package com.russianresale.app.ui.common;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.DrawableRes;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.russianresale.app.R;
import com.russianresale.app.data.model.ListingStatus;
import com.russianresale.app.data.model.OrderStatus;
import com.russianresale.app.data.repo.Callback;
import com.russianresale.app.databinding.ViewEmptyBinding;

/** Мелкие помощники интерфейса. */
public final class Ui {
    private Ui() {
    }

    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static int dp(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }

    public static String text(TextInputEditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString().trim();
    }

    public static String text(TextView view) {
        return view.getText() == null ? "" : view.getText().toString().trim();
    }

    /** Callback, который показывает ошибку тостом, а при успехе выполняет действие. */
    public static <T> Callback<T> callback(Context context, OnResult<T> onSuccess) {
        return new Callback<T>() {
            @Override
            public void onSuccess(T result) {
                if (onSuccess != null) onSuccess.accept(result);
            }

            @Override
            public void onError(String message) {
                toast(context, message);
            }
        };
    }

    public interface OnResult<T> {
        void accept(T value);
    }

    public static void showEmpty(ViewEmptyBinding empty, boolean show, @DrawableRes int icon, String title, String text) {
        empty.getRoot().setVisibility(show ? View.VISIBLE : View.GONE);
        if (!show) return;
        empty.emptyIcon.setImageResource(icon);
        empty.emptyTitle.setText(title);
        empty.emptyText.setText(text);
        empty.emptyText.setVisibility(text == null || text.isEmpty() ? View.GONE : View.VISIBLE);
    }

    public static Chip addChoiceChip(ChipGroup group, String text, int id) {
        Chip chip = (Chip) LayoutInflater.from(group.getContext())
                .inflate(R.layout.view_choice_chip, group, false);
        chip.setId(id);
        chip.setText(text);
        group.addView(chip);
        return chip;
    }

    /** Цветная плашка статуса заказа. */
    public static void bindOrderStatus(TextView view, OrderStatus status) {
        int color;
        switch (status) {
            case NEW:
                color = R.color.warning;
                break;
            case ACCEPTED:
                color = R.color.info;
                break;
            case SHIPPED:
                color = R.color.tertiary;
                break;
            case DELIVERED:
                color = R.color.success;
                break;
            default:
                color = R.color.on_surface_variant;
        }
        tintLabel(view, status.title, color);
    }

    public static void bindListingStatus(TextView view, ListingStatus status) {
        int color;
        switch (status) {
            case ACTIVE:
                color = R.color.success;
                break;
            case SOLD:
                color = R.color.info;
                break;
            case BLOCKED:
                color = R.color.error;
                break;
            default:
                color = R.color.on_surface_variant;
        }
        tintLabel(view, status.title, color);
    }

    public static void tintLabel(TextView view, String text, int colorRes) {
        int color = view.getContext().getColor(colorRes);
        view.setText(text);
        view.setTextColor(color);
        view.setBackgroundResource(R.drawable.bg_status);
        view.setBackgroundTintList(ColorStateList.valueOf((color & 0x00FFFFFF) | 0x22000000));
    }

    /** Диалог с полем ввода. */
    public static void inputDialog(Context context, String title, String hint, String initial, boolean multiline,
                                   String positive, OnResult<String> onOk) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_input, null, false);
        TextInputLayout layout = view.findViewById(R.id.inputLayout);
        TextInputEditText edit = view.findViewById(R.id.input);
        layout.setHint(hint);
        if (initial != null) edit.setText(initial);
        if (multiline) {
            edit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
            edit.setMinLines(3);
        }
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setView(view)
                .setNegativeButton("Отмена", null)
                .setPositiveButton(positive, (d, w) -> onOk.accept(text(edit)))
                .show();
    }

    public static void confirm(Context context, String title, String message, String positive, Runnable onOk) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton("Отмена", null)
                .setPositiveButton(positive, (d, w) -> onOk.run())
                .show();
    }
}
