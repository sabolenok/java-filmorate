package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.getFilmStorage().findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос к эндпоинту POST /films");
        return filmService.getFilmStorage().create(film);
    }

    @PutMapping
    public Film put(@Valid @RequestBody Film film) {
        return filmService.getFilmStorage().put(film);
    }

    @GetMapping("/films/{id}")
    public Film findById(@PathVariable Integer id) {
        return filmService.getFilmStorage().findById(id);
    }

    @PutMapping("/films/{id}/like/{userId}")
    public void like(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.like(id, userId);
    }

    @DeleteMapping("/films/{id}/like/{userId}")
    public void dislike(@PathVariable Integer id, @PathVariable Integer userId) {
        filmService.dislike(id, userId);
    }

    @GetMapping("/films/popular")
    public List<Film> mostPopular(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getMostPopular(count);
    }
}
