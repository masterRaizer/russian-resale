package com.russianresale.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.russianresale.app.R;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.databinding.ActivityRegisterBinding;
import com.russianresale.app.ui.common.BaseActivity;
import com.russianresale.app.ui.common.Ui;
import com.russianresale.app.ui.main.MainActivity;
import com.russianresale.app.util.Validators;

/**
 * Регистрация. Роль выбирается один раз:
 * покупатель только покупает, продавец только размещает объявления.
 */
public class RegisterActivity extends BaseActivity {
    private ActivityRegisterBinding b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        setupToolbar(b.toolbar, "Регистрация");

        b.roleToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) updateRole();
        });
        updateRole();
        b.registerButton.setOnClickListener(v -> register());
    }

    private Role selectedRole() {
        return b.roleToggle.getCheckedButtonId() == R.id.roleSeller ? Role.SELLER : Role.BUYER;
    }

    private void updateRole() {
        boolean seller = selectedRole() == Role.SELLER;
        b.sellerSection.setVisibility(seller ? View.VISIBLE : View.GONE);
        b.roleHint.setText(seller
                ? "Продавец — ателье, мастерская или частный продавец. Размещает объявления, принимает заказы, "
                + "отвечает на отзывы. Покупать с аккаунта продавца нельзя."
                : "Покупатель выбирает вещи, добавляет их в избранное и корзину, оформляет заказы, "
                + "пишет продавцам и оставляет отзывы. Размещать объявления покупатель не может.");
    }

    private void register() {
        String name = Ui.text(b.name);
        String email = Ui.text(b.email);
        String phone = Ui.text(b.phone);
        String password = b.password.getText() == null ? "" : b.password.getText().toString();
        String password2 = b.password2.getText() == null ? "" : b.password2.getText().toString();
        Role role = selectedRole();

        boolean ok = true;
        b.nameLayout.setError(null);
        b.emailLayout.setError(null);
        b.phoneLayout.setError(null);
        b.passwordLayout.setError(null);
        b.password2Layout.setError(null);
        b.shopNameLayout.setError(null);
        if (!Validators.isValidName(name)) {
            b.nameLayout.setError("Введите имя");
            ok = false;
        }
        if (!Validators.isValidEmail(email)) {
            b.emailLayout.setError("Некорректный email");
            ok = false;
        }
        if (!Validators.isValidPhone(phone)) {
            b.phoneLayout.setError("Формат: +79991234567");
            ok = false;
        }
        if (!Validators.isValidPassword(password)) {
            b.passwordLayout.setError("Минимум " + Validators.MIN_PASSWORD + " символов");
            ok = false;
        }
        if (!password.equals(password2)) {
            b.password2Layout.setError("Пароли не совпадают");
            ok = false;
        }
        if (role == Role.SELLER && !Validators.isNotBlank(Ui.text(b.shopName))) {
            b.shopNameLayout.setError("Укажите название");
            ok = false;
        }
        if (!b.agree.isChecked()) {
            toast("Нужно согласиться с правилами площадки");
            ok = false;
        }
        if (!ok) return;

        b.registerButton.setEnabled(false);
        app().auth().register(name, email, phone, Ui.text(b.city), password, role,
                Ui.text(b.shopName), Ui.text(b.shopDescription), new com.russianresale.app.data.repo.Callback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        session().save(user);
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String message) {
                        b.registerButton.setEnabled(true);
                        toast(message);
                    }
                });
    }
}
