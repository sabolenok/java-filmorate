package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class FilmService {
    @Autowired
    @Qualifier("inDbFilm")
    private FilmDbStorage filmStorage;
    @Autowired
    @Qualifier("inDbUser")
    private UserDbStorage userStorage;

    public Collection<Film> findAll() {
        log.info("Получен запрос к эндпоинту GET /films");
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        log.info("Получен запрос к эндпоинту POST /films");
        return filmStorage.create(film);
    }

    public Film put(Film film) {
        log.info("Получен запрос к эндпоинту PUT /films");
        return filmStorage.put(film);
    }

    public Film findById(Integer id) {
        log.info("Получен запрос к эндпоинту GET /films/{id}");
        return filmStorage.findById(id);
    }

    public void like(Integer filmId, Integer userId) {
        log.info("Получен запрос к эндпоинту PUT /films/{id}/like/{userId}");

        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.like(film, userId);
    }

    public void dislike(Integer filmId, Integer userId) {
        log.info("Получен запрос к эндпоинту DELETE /films/{id}/like/{userId}");

        Film film = filmStorage.findById(filmId);
        userStorage.findById(userId);
        filmStorage.dislike(film, userId);
    }

    public List<Film> getMostPopular(Integer count) {
        log.info("Получен запрос к эндпоинту GET /films/popular");

        return filmStorage.getMostPopular(count);
    }

    public Rating findMpaById(Integer mpaId) {
        log.info("Получен запрос к эндпоинту GET /mpa/{id}");
        return filmStorage.findMpaById(mpaId);
    }

    public List<Rating> findAllMpa() {
        log.info("Получен запрос к эндпоинту GET /mpa");
        return filmStorage.findAllMpa();
    }

    public Genre findGenreById(Integer genreId) {
        log.info("Получен запрос к эндпоинту GET /genres/{id}");
        return filmStorage.findGenreById(genreId);
    }

    public List<Genre> findAllGenres() {
        log.info("Получен запрос к эндпоинту GET /genres");
        return filmStorage.findAllGenres();
    }
}
