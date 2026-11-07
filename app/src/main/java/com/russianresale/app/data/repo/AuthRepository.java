package com.russianresale.app.data.repo;

import androidx.lifecycle.LiveData;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.data.entity.User;
import com.russianresale.app.data.model.Role;
import com.russianresale.app.util.PasswordHasher;
import com.russianresale.app.util.Validators;

public class AuthRepository extends BaseRepository {

    public AuthRepository(AppDatabase db) {
        super(db);
    }

    public void login(String email, String password, Callback<User> callback) {
        exec(() -> {
            if (!Validators.isValidEmail(email)) throw new AppException("Введите корректный email");
            User user = db.users().findByEmail(email.trim());
            if (user == null || !PasswordHasher.verify(password, user.salt, user.passwordHash)) {
                throw new AppException("Неверный email или пароль");
            }
            if (user.blocked) throw new AppException("Аккаунт заблокирован администратором");
            return user;
        }, callback);
    }

    /** Регистрация покупателя или продавца. Администратора зарегистрировать нельзя. */
    public void register(String fullName, String email, String phone, String city, String password,
                         Role role, String shopName, String shopDescription, Callback<User> callback) {
        exec(() -> {
            if (role == Role.ADMIN) throw new AppException("Нельзя зарегистрироваться администратором");
            if (!Validators.isValidName(fullName)) throw new AppException("Введите имя");
            if (!Validators.isValidEmail(email)) throw new AppException("Введите корректный email");
            if (!Validators.isValidPhone(phone)) throw new AppException("Введите телефон в формате +7XXXXXXXXXX");
            if (!Validators.isValidPassword(password)) {
                throw new AppException("Пароль должен быть не короче " + Validators.MIN_PASSWORD + " символов");
            }
            if (role == Role.SELLER && !Validators.isNotBlank(shopName)) {
                throw new AppException("Укажите название ателье или магазина");
            }
            if (db.users().findByEmail(email.trim()) != null) {
                throw new AppException("Пользователь с таким email уже зарегистрирован");
            }
            User user = new User();
            user.fullName = fullName.trim();
            user.email = email.trim().toLowerCase();
            user.phone = phone.trim();
            user.city = city == null ? null : city.trim();
            user.role = role.name();
            user.salt = PasswordHasher.newSalt();
            user.passwordHash = PasswordHasher.hash(password, user.salt);
            if (role == Role.SELLER) {
                user.shopName = shopName.trim();
                user.shopDescription = shopDescription == null ? null : shopDescription.trim();
            }
            user.createdAt = System.currentTimeMillis();
            user.id = db.users().insert(user);
            String welcome = role == Role.SELLER
                    ? "Разместите первое объявление: вкладка «Объявления» → кнопка «+»"
                    : "Выбирайте одежду и аксессуары от ателье и продавцов";
            notify(user.id, AppNotification.TYPE_SYSTEM, 0, "Добро пожаловать в «Русский ресейл»!", welcome);
            return user;
        }, callback);
    }

    public LiveData<User> observeUser(long id) {
        return db.users().observe(id);
    }

    public void getUser(long id, Callback<User> callback) {
        exec(() -> {
            User user = db.users().getById(id);
            if (user == null) throw new AppException("Пользователь не найден");
            return user;
        }, callback);
    }

    public void updateProfile(long userId, String fullName, String phone, String city, String shopName,
                              String shopDescription, String avatarPath, Callback<User> callback) {
        exec(() -> {
            User user = requireUser(userId);
            if (!Validators.isValidName(fullName)) throw new AppException("Введите имя");
            if (!Validators.isValidPhone(phone)) throw new AppException("Введите телефон в формате +7XXXXXXXXXX");
            user.fullName = fullName.trim();
            user.phone = phone.trim();
            user.city = city == null ? null : city.trim();
            if (user.isSeller()) {
                if (!Validators.isNotBlank(shopName)) throw new AppException("Укажите название ателье или магазина");
                user.shopName = shopName.trim();
                user.shopDescription = shopDescription == null ? null : shopDescription.trim();
            }
            if (avatarPath != null) user.avatarPath = avatarPath;
            db.users().update(user);
            return user;
        }, callback);
    }

    public void changePassword(long userId, String oldPassword, String newPassword, Callback<Boolean> callback) {
        exec(() -> {
            User user = requireUser(userId);
            if (!PasswordHasher.verify(oldPassword, user.salt, user.passwordHash)) {
                throw new AppException("Текущий пароль введён неверно");
            }
            if (!Validators.isValidPassword(newPassword)) {
                throw new AppException("Новый пароль должен быть не короче " + Validators.MIN_PASSWORD + " символов");
            }
            user.salt = PasswordHasher.newSalt();
            user.passwordHash = PasswordHasher.hash(newPassword, user.salt);
            db.users().update(user);
            return true;
        }, callback);
    }
}
