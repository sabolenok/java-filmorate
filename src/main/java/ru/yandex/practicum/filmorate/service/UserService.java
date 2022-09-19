package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    @Getter
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

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
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        Set<Integer> friends = user.getFriends();
        friends.add(friendId);
        user.setFriends(friends);

        friends = friend.getFriends();
        friends.add(userId);
        friend.setFriends(friends);
        log.info("Пользователи '{}' и '{}' теперь друзья", user.getName(), friend.getName());
    }

    public void removeFriend(Integer userId, Integer friendId) {
        log.info("Получен запрос к эндпоинту DELETE /users/{id}/friends/{friendId}");
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        Set<Integer> friends = user.getFriends();
        friends.remove(friendId);
        user.setFriends(friends);

        friends = friend.getFriends();
        friends.remove(userId);
        friend.setFriends(friends);
        log.info("Пользователи '{}' и '{}' больше не друзья", user.getName(), friend.getName());
    }

    public Collection<User> commonFriends(Integer userId, Integer otherId) {
        log.info("Получен запрос к эндпоинту GET /users//{id}/friends/common/{otherId}");
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);
        Set<Integer> otherFriends = other.getFriends();
        return user.getFriends()
                .stream()
                .filter(otherFriends::contains)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public Collection<User> getUsersFriends(Integer userId) {
        log.info("Получен запрос к эндпоинту GET /users/{id}/friends");
        User user = userStorage.findById(userId);
        return user.getFriends().stream().map(userStorage::findById).collect(Collectors.toList());
    }
}
