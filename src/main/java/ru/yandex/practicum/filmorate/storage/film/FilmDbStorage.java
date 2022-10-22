package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Component("inDbFilm")
@Slf4j
public class FilmDbStorage implements FilmStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<Film> findAll() {
        Collection<Film> films = new ArrayList<>();
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films");
        while (filmRows.next()) {
            log.info("Найден фильм: {} {}", filmRows.getString("film_id"), filmRows.getString("film_name"));
            Film film = new Film(
                    filmRows.getString("film_name"),
                    filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"));
            film.setId(filmRows.getInt("film_id"));
            films.add(film);
        }
        return films;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pst = connection.prepareStatement(
                    "insert into films (film_name, description, release_date, duration) values (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, film.getName());
            pst.setString(2, film.getDescription());
            pst.setDate(3, Date.valueOf(film.getReleaseDate()));
            pst.setInt(4, film.getDuration());
            return pst;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        film.setId(id);
        SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select * from rating where rating_id = ?", film.getMpa().getId());
        if (ratingRows.next()) {
            jdbcTemplate.update(
                    "insert into films_rating (film_id, rating_id) values (?, ?)",
                    id,
                    ratingRows.getInt("rating_id")
            );
            film.getMpa().setName(ratingRows.getString("rating_name"));
        }
        String filmsGenres = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()).toString().replace("[", "").replace("]", "");
        if (!filmsGenres.isBlank()) {
            jdbcTemplate.update(
                    "insert into films_genres (film_id, genre_id) select ?, genre_id from genres where genre_id in (?)",
                    id,
                    filmsGenres
            );
        }
        return film;
    }

    @Override
    public Film put(Film film) {
        findById(film.getId());
        jdbcTemplate.update(
                "update films set film_name = ?, description = ?, release_date = ?, duration = ? where film_id = ?",
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getId()
        );
        SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select * from rating where rating_id = ?", film.getMpa().getId());
        if (ratingRows.next()) {
            jdbcTemplate.update(
                    "update films_rating set rating_id = ? where film_id = ?",
                    ratingRows.getInt("rating_id"),
                    film.getId()
            );
            film.getMpa().setName(ratingRows.getString("rating_name"));
        }
        String filmsGenres = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()).toString().replace("[", "").replace("]", "");
        if (!filmsGenres.isBlank()) {
            jdbcTemplate.update(
                    "insert into films_genres (film_id, genre_id) select ?, genre_id from genres where genre_id in (?) on duplicate key update genre_id=values(genre_id)",
                    film.getId(),
                    filmsGenres
            );
        }
        return film;
    }

    @Override
    public Film findById(Integer id) {
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("select * from films where film_id = ?", id);
        if (filmRows.next()) {
            log.info("Найден фильм: {} {}", filmRows.getString("film_id"), filmRows.getString("film_name"));
            Film film = new Film(
                    filmRows.getString("film_name"),
                    filmRows.getString("description"),
                    filmRows.getDate("release_date").toLocalDate(),
                    filmRows.getInt("duration"));
            film.setId(filmRows.getInt("film_id"));
            SqlRowSet filmRatingRows = jdbcTemplate.queryForRowSet("select * from films_rating where film_id = ?", film.getId());
            if (filmRatingRows.next()) {
                SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select * from rating where rating_id = ?", filmRatingRows.getInt("rating_id"));
                if (ratingRows.next()) {
                    film.setMpa(new Rating(filmRatingRows.getInt("rating_id"), ratingRows.getString("rating_name")));
                }
            }
            SqlRowSet filmGenreRows = jdbcTemplate.queryForRowSet(
                    "select g.genre_id, g.genre_name from films_genres as fg left join genres as g on g.genre_id = fg.genre_id where fg.film_id = ?",
                    film.getId()
            );
            Set<Genre> filmGenres = new HashSet<>();
            while (filmGenreRows.next()) {
                filmGenres.add(new Genre(filmGenreRows.getInt("genre_id"), filmGenreRows.getString("genre_name")));
            }
            film.setGenres(filmGenres);
            SqlRowSet filmLikesRows = jdbcTemplate.queryForRowSet(
                    "select user_id from films_likes where film_id = ?",
                    film.getId()
            );
            Set<Integer> filmLikes = new HashSet<>();
            while (filmLikesRows.next()) {
                filmLikes.add(filmLikesRows.getInt("user_id"));
            }
            film.setLikes(filmLikes);
            return film;
        } else {
            log.info("Фильм с идентификатором {} не найден.", id);
            throw new NotFoundException("Фильм не найден");
        }
    }

    public void like(Film film, Integer userId) {
        Set<Integer> likes = film.getLikes();
        likes.add(userId);
        film.setLikes(likes);

        jdbcTemplate.update(
                "insert into films_likes (film_id, user_id) values (?, ?)",
                film.getId(),
                userId
        );

        log.info("К фильму '{}' добавлен лайк", film.getName());
    }

    public void dislike(Film film, Integer userId) {
        Set<Integer> likes = film.getLikes();
        likes.remove(userId);
        film.setLikes(likes);

        jdbcTemplate.update(
                "delete from films_likes where film_id = ? and user_id = ?",
                film.getId(),
                userId
        );

        log.info("С фильма '{}' снят лайк", film.getName());
    }

    public List<Film> getMostPopular(Integer count) {
        List<Film> popular = new ArrayList<>();
        SqlRowSet likesRows = jdbcTemplate.queryForRowSet(
                "SELECT f.film_id, count(fl.user_id) FROM films AS f LEFT JOIN films_likes AS fl ON f.film_id = fl.film_id GROUP BY f.film_id ORDER BY count(fl.user_id) DESC LIMIT ?",
                count
        );
        while (likesRows.next()) {
            popular.add(findById(likesRows.getInt("film_id")));
        }
        return popular;
    }
}
