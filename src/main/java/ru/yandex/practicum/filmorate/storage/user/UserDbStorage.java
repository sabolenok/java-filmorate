package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Component("inDbUser")
@Slf4j
public class UserDbStorage implements UserStorage {

    @Getter
    private static Integer id = 0;
    @Autowired
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        Collection<User> users = new ArrayList<>();
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users");
        while (userRows.next()) {
            log.info("Найден пользователь: {} {}", userRows.getString("user_id"), userRows.getString("user_name"));
            User user = new User(
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("user_name"),
                    userRows.getDate("birthday").toLocalDate());
            user.setId(userRows.getInt("user_id"));
            users.add(user);
        }
        return users;
    }

    @Override
    public User create(User user) {
        jdbcTemplate.update(
                "insert into users (email, login, user_name, birthday) values (?, ?, ?, ?)",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        return user;
    }

    @Override
    public User put(User user) {
        jdbcTemplate.update(
                "update users set email = ?, login = ?, user_name = ?, birthday = ? where user_id = ?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );
        return user;
    }

    @Override
    public User findById(Integer id) {
        SqlRowSet userRows = jdbcTemplate.queryForRowSet("select * from users where user_id = ?", id);
        if (userRows.next()) {
            log.info("Найден пользователь: {} {}", userRows.getString("user_id"), userRows.getString("user_name"));
            User user = new User(
                    userRows.getString("email"),
                    userRows.getString("login"),
                    userRows.getString("user_name"),
                    userRows.getDate("birthday").toLocalDate());
            user.setId(userRows.getInt("user_id"));
            return user;
        } else {
            log.info("Пользователь с идентификатором {} не найден.", id);
            throw new NotFoundException("Пользователь не найден");
        }
    }
}
