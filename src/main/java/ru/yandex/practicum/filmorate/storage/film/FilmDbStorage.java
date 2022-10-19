package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Component("inDbFilm")
@Slf4j
public class FilmDbStorage implements FilmStorage {
    @Override
    public Collection<Film> findAll() {
        return null;
    }

    @Override
    public Film create(Film film) {
        return null;
    }

    @Override
    public Film put(Film film) {
        return null;
    }

    @Override
    public Film findById(Integer id) {
        return null;
    }
}
