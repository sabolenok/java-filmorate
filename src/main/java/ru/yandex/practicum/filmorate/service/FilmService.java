package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
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
        Set<Integer> likes = film.getLikes();
        likes.add(userId);
        film.setLikes(likes);

        log.info("К фильму '{}' добавлен лайк", film.getName());
    }

    public void dislike(Integer filmId, Integer userId) {
        log.info("Получен запрос к эндпоинту DELETE /films/{id}/like/{userId}");

        Film film = filmStorage.findById(filmId);
        Set<Integer> likes = film.getLikes();
        likes.remove(userId);
        film.setLikes(likes);

        log.info("С фильма '{}' снят лайк", film.getName());
    }

    public List<Film> getMostPopular(Integer count) {
        log.info("Получен запрос к эндпоинту GET /films/popular");

        return filmStorage.findAll()
                .stream()
                .sorted(this::reverseCompare)
                .limit(count)
                .collect(Collectors.toList());
    }

    private int reverseCompare(Film f0, Film f1) {
        int result = Integer.compare(f0.getLikes().size(), f1.getLikes().size());
        return (-1 * result);   // нужна сортировка по убыванию
    }
}
