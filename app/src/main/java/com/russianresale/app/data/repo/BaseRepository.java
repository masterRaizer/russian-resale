package com.russianresale.app.data.repo;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.russianresale.app.data.db.AppDatabase;
import com.russianresale.app.data.entity.AppNotification;
import com.russianresale.app.data.entity.User;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Общая часть репозиториев. Все изменения выполняются в одном фоновом потоке и в транзакции,
 * поэтому, например, два заказа не могут одновременно забрать последнюю вещь.
 * Проверки ролей и бизнес-правил выполняются здесь, а не только в интерфейсе — как на сервере.
 */
public abstract class BaseRepository {
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    protected final AppDatabase db;

    protected BaseRepository(AppDatabase db) {
        this.db = db;
    }

    protected <T> void exec(Callable<T> task, Callback<T> callback) {
        IO.execute(() -> {
            try {
                T result = db.runInTransaction(task);
                if (callback != null) MAIN.post(() -> callback.onSuccess(result));
            } catch (AppException e) {
                if (callback != null) MAIN.post(() -> callback.onError(e.getMessage()));
            } catch (Throwable e) {
                Log.e("Repository", "Ошибка операции", e);
                String msg = "Не удалось выполнить действие: " + e.getMessage();
                if (callback != null) MAIN.post(() -> callback.onError(msg));
            }
        });
    }

    protected void runInBackground(Runnable runnable) {
        IO.execute(() -> {
            try {
                runnable.run();
            } catch (Throwable e) {
                Log.e("Repository", "Ошибка фоновой операции", e);
            }
        });
    }

    /** Пользователь существует и не заблокирован. */
    protected User requireUser(long userId) {
        User user = db.users().getById(userId);
        if (user == null) throw new AppException("Пользователь не найден. Войдите заново");
        if (user.blocked) throw new AppException("Аккаунт заблокирован администратором");
        return user;
    }

    protected User requireBuyer(long userId) {
        User user = requireUser(userId);
        if (!user.isBuyer()) {
            throw new AppException("Покупать могут только покупатели. Аккаунт продавца предназначен для продажи");
        }
        return user;
    }

    protected User requireSeller(long userId) {
        User user = requireUser(userId);
        if (!user.isSeller()) {
            throw new AppException("Размещать объявления могут только продавцы");
        }
        return user;
    }

    protected User requireAdmin(long userId) {
        User user = requireUser(userId);
        if (!user.isAdmin()) throw new AppException("Действие доступно только администратору");
        return user;
    }

    protected void notify(long userId, String type, long refId, String title, String text) {
        db.notifications().insert(new AppNotification(userId, type, refId, title, text));
    }
}
