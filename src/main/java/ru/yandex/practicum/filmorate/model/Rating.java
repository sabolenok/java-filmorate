package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Rating {
    private int id;
    private String name;

    public Rating(int id, String name) {
        this.id = id;
        this.name = name;
    }
}
