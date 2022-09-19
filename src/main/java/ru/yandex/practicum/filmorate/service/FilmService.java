package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film put(Film film) {
        return filmStorage.put(film);
    }

    public Film findById(Integer id) {
        return filmStorage.findById(id);
    }

    public void like(Integer filmId, Integer userId) {
        Film film = filmStorage.findById(filmId);
        Set<Integer> likes = film.getLikes();
        likes.add(userId);
        film.setLikes(likes);
    }

    public void dislike(Integer filmId, Integer userId) {
        Film film = filmStorage.findById(filmId);
        Set<Integer> likes = film.getLikes();
        likes.remove(userId);
        film.setLikes(likes);
    }

    public List<Film> getMostPopular(Integer count) {
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
