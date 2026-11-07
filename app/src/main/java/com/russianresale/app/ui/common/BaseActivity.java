package com.russianresale.app.ui.common;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.russianresale.app.App;
import com.russianresale.app.R;
import com.russianresale.app.data.SessionManager;

/** Общая часть экранов: доступ к репозиториям, сессии и тулбару. */
public abstract class BaseActivity extends AppCompatActivity {

    protected App app() {
        return App.get(this);
    }

    protected SessionManager session() {
        return app().session();
    }

    protected long userId() {
        return session().getUserId();
    }

    protected void setupToolbar(MaterialToolbar toolbar, CharSequence title) {
        toolbar.setTitle(title);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationContentDescription("Назад");
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
