package com.russianresale.app.data.repo;

/** Результат асинхронной операции репозитория. Вызывается в главном потоке. */
public interface Callback<T> {
    void onSuccess(T result);

    void onError(String message);
}
