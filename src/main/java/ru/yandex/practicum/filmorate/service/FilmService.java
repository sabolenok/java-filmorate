package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    @Getter
    private final FilmStorage filmStorage;

    public FilmService(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
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
                .sorted(this::compare)
                .limit(count)
                .collect(Collectors.toList());
    }

    private int compare(Film f0, Film f1) {
        Integer likes0 = f0.getLikes().size();
        Integer likes1 = f1.getLikes().size();
        return likes0.compareTo(likes1);
    }
}
