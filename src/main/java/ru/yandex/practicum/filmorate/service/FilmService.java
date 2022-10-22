package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    @Qualifier("inDbFilm")
    private final FilmDbStorage filmStorage;
    @Autowired
    private UserDbStorage userStorage;

    public FilmService(FilmDbStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

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

    private int reverseCompare(Film f0, Film f1) {
        int result = Integer.compare(f0.getLikes().size(), f1.getLikes().size());
        return (-1 * result);   // нужна сортировка по убыванию
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
