package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;
    private final UserService userService;

    public FilmController(FilmService filmService, UserService userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film put(@Valid @RequestBody Film film) {
        return filmService.put(film);
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Integer id) {
        return filmService.findById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable Integer id, @PathVariable Integer userId) {
        // Проверка, существует ли такой пользователь. Если его нет, выбросит NotFoundException
        userService.findById(userId);
        filmService.like(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void dislike(@PathVariable Integer id, @PathVariable Integer userId) {
        // Проверка, существует ли такой пользователь. Если его нет, выбросит NotFoundException
        userService.findById(userId);
        filmService.dislike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> mostPopular(@RequestParam(defaultValue = "10", required = false) Integer count) {
        return filmService.getMostPopular(count);
    }
}
