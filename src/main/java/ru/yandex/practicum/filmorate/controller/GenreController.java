package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {

    private final FilmService filmService;

    @GetMapping("/{id}")
    public Genre findById(@PathVariable Integer id) {
        return filmService.findGenreById(id);
    }

    @GetMapping()
    public List<Genre> findAll() {
        return filmService.findAllGenres();
    }
}
