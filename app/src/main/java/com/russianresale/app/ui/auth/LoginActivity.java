package com.russianresale.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import com.russianresale.app.data.DemoSeeder;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.repo.Callback;
import com.russianresale.app.databinding.ActivityLoginBinding;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.main.MainActivity;
import com.russianresale.app.util.Validators;

public class LoginActivity extends BaseActivity {
    private ActivityLoginBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        b.loginButton.setOnClickListener(v -> login());
        b.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                login();
                return true;
            }
            return false;
        });
        b.registerButton.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        b.demoBuyer.setOnClickListener(v -> fill("buyer@resale.ru"));
        b.demoSeller.setOnClickListener(v -> fill("seller@resale.ru"));
        b.demoAdmin.setOnClickListener(v -> fill("admin@resale.ru"));
    }

    private void fill(String email) {
        b.email.setText(email);
        b.password.setText(DemoSeeder.PASSWORD);
    }

    private void login() {
        String email = Ui.text(b.email);
        String password = b.password.getText() == null ? "" : b.password.getText().toString();
        b.emailLayout.setError(Validators.isValidEmail(email) ? null : "Введите корректный email");
        b.passwordLayout.setError(password.isEmpty() ? "Введите пароль" : null);
        if (!Validators.isValidEmail(email) || password.isEmpty()) return;

        setLoading(true);
        app().auth().login(email, password, new Callback<User>() {
            @Override
            public void onSuccess(User user) {
                session().save(user);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                toast(message);
            }
        });
    }

    private void setLoading(boolean loading) {
        b.progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        b.loginButton.setEnabled(!loading);
    }
}
