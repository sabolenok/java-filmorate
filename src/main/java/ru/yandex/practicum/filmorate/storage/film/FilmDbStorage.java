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
            SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select r.rating_id, r.rating_name from films_rating as fr left join rating as r on fr.rating_id = r.rating_id where fr.film_id = ?", film.getId());
            if (ratingRows.next()) {
                film.setMpa(new Rating(ratingRows.getInt("rating_id"), ratingRows.getString("rating_name")));
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
        if (!(film.getMpa() == null)) {
            SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select * from rating where rating_id = ?", film.getMpa().getId());
            if (ratingRows.next()) {
                jdbcTemplate.update(
                        "insert into films_rating (film_id, rating_id) values (?, ?)",
                        id,
                        ratingRows.getInt("rating_id")
                );
                film.getMpa().setName(ratingRows.getString("rating_name"));
            }
        }
        String filmsGenres = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()).toString().replace("[", "").replace("]", "");
        if (!filmsGenres.isBlank()) {
            jdbcTemplate.update(
                    "insert into films_genres (film_id, genre_id) select f.film_id, g.genre_id from films as f join genres as g where f.film_id = ? and g.genre_id in (" + filmsGenres + ")",
                    id
            );
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
        if (!(film.getMpa() == null)) {
            SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select * from rating where rating_id = ?", film.getMpa().getId());
            if (ratingRows.next()) {
                jdbcTemplate.update(
                        "update films_rating set rating_id = ? where film_id = ?",
                        ratingRows.getInt("rating_id"),
                        film.getId()
                );
                film.getMpa().setName(ratingRows.getString("rating_name"));
            }
        }
        jdbcTemplate.update(
                "delete from films_genres where film_id = ?",
                film.getId()
        );
        String filmsGenres = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet()).toString().replace("[", "").replace("]", "");
        if (!filmsGenres.isBlank()) {
            jdbcTemplate.update(
                    "insert into films_genres (film_id, genre_id) select f.film_id, g.genre_id from films as f join genres as g where f.film_id = ? and g.genre_id in (" + filmsGenres + ")",
                    film.getId()
            );
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
            SqlRowSet ratingRows = jdbcTemplate.queryForRowSet("select r.rating_id, r.rating_name from films_rating as fr left join rating as r on fr.rating_id = r.rating_id where fr.film_id = ?", film.getId());
            if (ratingRows.next()) {
                film.setMpa(new Rating(ratingRows.getInt("rating_id"), ratingRows.getString("rating_name")));
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

    public Rating findMpaById(Integer mpaId) {
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM rating WHERE rating_id = ?",
                mpaId
        );
        if (mpaRows.next()) {
            return new Rating(mpaRows.getInt("rating_id"), mpaRows.getString("rating_name"));
        } else {
            log.info("Рейтинг с идентификатором {} не найден.", mpaId);
            throw new NotFoundException("Рейтинг не найден");
        }
    }

    public List<Rating> findAllMpa() {
        List<Rating> mpa = new ArrayList<>();
        SqlRowSet mpaRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM rating"
        );
        while (mpaRows.next()) {
            mpa.add(new Rating(mpaRows.getInt("rating_id"), mpaRows.getString("rating_name")));
        }
        return mpa;
    }

    public Genre findGenreById(Integer genreId) {
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM genres WHERE genre_id = ?",
                genreId
        );
        if (genreRows.next()) {
            return new Genre(genreRows.getInt("genre_id"), genreRows.getString("genre_name"));
        } else {
            log.info("Жанр с идентификатором {} не найден.", genreId);
            throw new NotFoundException("Жанр не найден");
        }
    }

    public List<Genre> findAllGenres() {
        List<Genre> genres = new ArrayList<>();
        SqlRowSet genreRows = jdbcTemplate.queryForRowSet(
                "SELECT * FROM genres"
        );
        while (genreRows.next()) {
            genres.add(new Genre(genreRows.getInt("genre_id"), genreRows.getString("genre_name")));
        }
        return genres;
    }
}
