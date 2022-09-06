package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();

    @GetMapping("/films")
    public Collection<Film> findAll() {
        return films.values();
    }

    @RequestMapping(value = "/films", method = {RequestMethod.POST, RequestMethod.PUT})
    public Film create(@RequestBody Film film) {
        films.put(film.getId(), film);
        return film;
    }
}
