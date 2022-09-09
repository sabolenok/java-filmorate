package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.CustomValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int id;

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film, HttpServletRequest request) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error(
                    "Дата релиза должна быть не раньше 28.12.1895, Строка параметров запроса: '{}'",
                    request.getQueryString()
            );
            throw new CustomValidationException("Дата релиза должна быть не раньше 28.12.1895");
        }
        log.info("Получен запрос к эндпоинту: '{} {}', Строка параметров запроса: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        film.setId(++id);
        films.put(film.getId(), film);
        return film;
    }

    @PutMapping
    public Film put(@Valid @RequestBody Film film, HttpServletRequest request) {
        if (!films.containsKey(film.getId())) {
            log.error("Фильм с Id = '{}' не найден", film.getId());
            throw new NotFoundException("Фильм не найден");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error(
                    "Дата релиза должна быть не раньше 28.12.1895, Строка параметров запроса: '{}'",
                    request.getQueryString()
            );
            throw new CustomValidationException("Дата релиза должна быть не раньше 28.12.1895");
        }
        log.info("Получен запрос к эндпоинту: '{} {}', Строка параметров запроса: '{}'",
                request.getMethod(), request.getRequestURI(), request.getQueryString());
        films.put(film.getId(), film);
        return film;
    }
}
