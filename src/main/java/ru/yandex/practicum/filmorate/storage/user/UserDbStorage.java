package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component("inDbUser")
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    @Autowired
    private final JdbcTemplate jdbcTemplate;

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
        checkName(user);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement pst = connection.prepareStatement(
                    "insert into users (email, login, user_name, birthday) values (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            pst.setString(1, user.getEmail());
            pst.setString(2, user.getLogin());
            pst.setString(3, user.getName());
            pst.setDate(4, Date.valueOf(user.getBirthday()));
            return pst;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        user.setId(id);
        return user;
    }

    @Override
    public User put(User user) {
        checkName(user);
        findById(user.getId());
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

    public void addFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        Set<Integer> friends = user.getFriends();
        friends.add(friendId);
        user.setFriends(friends);

        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "select * from friendship where (user1_id = ? and user2_id = ?) or (user1_id = ? and user2_id = ?)",
                userId,
                friendId,
                friendId,
                userId);
        if (userRows.next()) {
            friends = friend.getFriends();
            friends.add(userId);
            friend.setFriends(friends);

            SqlRowSet friendshipStatus = jdbcTemplate.queryForRowSet("select * from friendship_status where status_name = ?", "Подтверждено");
            if (friendshipStatus.next()) {
                jdbcTemplate.update(
                        "delete from friendship where friendship_id = ?",
                        userRows.getInt("friendship_id")
                );
                jdbcTemplate.update(
                        "insert into friendship (user1_id, user2_id, status_id) values (?, ?, ?), (?, ?, ?)",
                        userId,
                        friendId,
                        friendshipStatus.getInt("status_id"),
                        friendId,
                        userId,
                        friendshipStatus.getInt("status_id")
                );
            }
        } else {
            SqlRowSet friendshipStatus = jdbcTemplate.queryForRowSet("select * from friendship_status where status_name = ?", "Отправлен запрос");
            if (friendshipStatus.next()) {
                jdbcTemplate.update(
                        "insert into friendship (user1_id, user2_id, status_id) values (?, ?, ?)",
                        userId,
                        friendId,
                        friendshipStatus.getInt("status_id")
                );
            }
        }
        log.info("Пользователи '{}' и '{}' теперь друзья", user.getName(), friend.getName());
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = findById(userId);
        User friend = findById(friendId);

        Set<Integer> friends = user.getFriends();
        friends.remove(friendId);
        user.setFriends(friends);

        SqlRowSet userRows = jdbcTemplate.queryForRowSet(
                "select * from friendship where user1_id = ? and user2_id = ?",
                userId,
                friendId);
        if (userRows.next()) {
            jdbcTemplate.update(
                    "delete from friendship where user1_id = ? and user2_id = ?",
                    userId,
                    friendId
            );
            SqlRowSet friendRows = jdbcTemplate.queryForRowSet(
                    "select * from friendship where user1_id = ? and user2_id = ?",
                    friendId,
                    userId);
            if (friendRows.next()) {
                SqlRowSet friendshipStatus = jdbcTemplate.queryForRowSet("select * from friendship_status where status_name = ?", "Отправлен запрос");
                if (friendshipStatus.next()) {
                    jdbcTemplate.update(
                            "update friendship set status_id = ? where user1_id = ? and user2_id = ?",
                            friendshipStatus.getInt("status_id"),
                            friendId,
                            userId
                    );
                }
            }
        }
        log.info("Пользователи '{}' и '{}' больше не друзья", user.getName(), friend.getName());
    }

    public Collection<User> commonFriends(Integer userId, Integer otherId) {
        findById(userId);
        findById(otherId);
        List<User> common = new ArrayList<>();

        SqlRowSet commonRows = jdbcTemplate.queryForRowSet(
                "SELECT f1.USER2_ID FROM FRIENDSHIP AS f1 INNER JOIN FRIENDSHIP AS f2 " +
                        "ON f1.USER2_ID = f2.USER2_ID WHERE f1.USER1_ID = ? AND f2.USER1_ID = ?",
                userId,
                otherId
        );
        while (commonRows.next()) {
            common.add(findById(commonRows.getInt("USER2_ID")));
        }

        return common;
    }

    public Collection<User> getUsersFriends(Integer userId) {
        findById(userId);
        List<User> friends = new ArrayList<>();

        SqlRowSet friendsRows = jdbcTemplate.queryForRowSet(
                "SELECT USER2_ID FROM FRIENDSHIP WHERE USER1_ID = ?",
                userId
        );
        while (friendsRows.next()) {
            friends.add(findById(friendsRows.getInt("USER2_ID")));
        }

        return friends;
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public Optional<User> findUserById(int i) {
        return Optional.ofNullable(findById(i));
    }
}
