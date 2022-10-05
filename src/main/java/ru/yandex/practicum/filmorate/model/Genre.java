package ru.yandex.practicum.filmorate.model;

public enum Genre {
    COMEDY("Комедия"),
    DRAMA("Драма"),
    CARTOON("Мультфильм"),
    THRILLER("Триллер"),
    DOCUMENTARY("Документальный"),
    ACTION("");

    String value;

    Genre(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
