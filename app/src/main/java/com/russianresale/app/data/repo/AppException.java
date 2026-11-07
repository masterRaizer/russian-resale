package com.russianresale.app.data.repo;

/** Ошибка бизнес-правила: текст показывается пользователю как есть. */
public class AppException extends RuntimeException {
    public AppException(String message) {
        super(message);
    }
}
