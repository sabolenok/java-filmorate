package ru.yandex.practicum.filmorate.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum FriendshipStatus {
    REQUESTED("Отправлен запрос"),
    CONFIRMED("Подтверждено");

    private final String value;

    @Override
    public String toString() {
        return value;
    }
}
