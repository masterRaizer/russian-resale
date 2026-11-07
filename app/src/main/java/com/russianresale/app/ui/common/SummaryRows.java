package com.russianresale.app.ui.common;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.russianresale.app.R;

/** Строки «название — значение» в итогах заказа. */
public final class SummaryRows {
    private SummaryRows() {
    }

    public static TextView add(LinearLayout container, String label, String value, boolean bold) {
        View row = LayoutInflater.from(container.getContext()).inflate(R.layout.item_summary_row, container, false);
        TextView l = row.findViewById(R.id.label);
        TextView v = row.findViewById(R.id.value);
        l.setText(label);
        v.setText(value);
        if (bold) {
            l.setTypeface(Typeface.DEFAULT_BOLD);
            v.setTypeface(Typeface.DEFAULT_BOLD);
            l.setTextSize(17);
            v.setTextSize(17);
        }
        container.addView(row);
        return v;
    }

    public static void header(LinearLayout container, String text) {
        TextView t = new TextView(container.getContext());
        t.setText(text);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        t.setPadding(0, Ui.dp(container.getContext(), 8), 0, Ui.dp(container.getContext(), 2));
        t.setTextColor(container.getContext().getColor(R.color.primary));
        container.addView(t);
    }
}
