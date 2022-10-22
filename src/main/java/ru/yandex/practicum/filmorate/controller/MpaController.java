package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final FilmService filmService;

    public MpaController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public Rating findById(@PathVariable Integer id) {
        return filmService.findMpaById(id);
    }

    @GetMapping()
    public List<Rating> findAll() {
        return filmService.findAllMpa();
    }
}
