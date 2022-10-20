package ru.yandex.practicum.filmorate.model;

public enum FriendshipStatus {
    REQUESTED("Отправлен запрос"),
    CONFIRMED("Подтверждено");

    private final String value;

    FriendshipStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
