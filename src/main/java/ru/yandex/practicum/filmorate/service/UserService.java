package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    @Autowired
    @Getter
    @Qualifier("inDbUser")
    private UserDbStorage userStorage;

    public Collection<User> findAll() {
        log.info("Получен запрос к эндпоинту GET /users");
        return userStorage.findAll();
    }

    public User create(User user) {
        log.info("Получен запрос к эндпоинту POST /users");
        return userStorage.create(user);
    }

    public User put(User user) {
        log.info("Получен запрос к эндпоинту PUT /users");
        return userStorage.put(user);
    }

    public User findById(Integer id) {
        log.info("Получен запрос к эндпоинту GET /users/{id}");
        return userStorage.findById(id);
    }

    public void addFriend(Integer userId, Integer friendId) {
        log.info("Получен запрос к эндпоинту PUT /users/{id}/friends/{friendId}");
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Получен запрос к эндпоинту DELETE /users/{id}/friends/{friendId}");
        userStorage.removeFriend(userId, friendId);
    }

    public Collection<User> commonFriends(Integer userId, Integer otherId) {
        log.info("Получен запрос к эндпоинту GET /users//{id}/friends/common/{otherId}");
        return userStorage.commonFriends(userId, otherId);
    }

    public Collection<User> getUsersFriends(Integer userId) {
        log.info("Получен запрос к эндпоинту GET /users/{id}/friends");
        return userStorage.getUsersFriends(userId);
    }
}
