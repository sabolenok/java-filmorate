package ru.yandex.practicum.filmorate.model;

public enum Rating {
    G("G"),
    PG("PG"),
    PG13("PG-13"),
    R("R"),
    NC17("NC-17");

    String value;

    Rating(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
