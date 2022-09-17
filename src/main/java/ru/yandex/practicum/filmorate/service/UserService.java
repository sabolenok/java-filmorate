package ru.yandex.practicum.filmorate.service;

import lombok.Getter;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Getter
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        Set<Integer> friends = user.getFriends();
        friends.add(friendId);
        user.setFriends(friends);

        friends = friend.getFriends();
        friends.add(userId);
        friend.setFriends(friends);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.findById(userId);
        User friend = userStorage.findById(friendId);

        Set<Integer> friends = user.getFriends();
        friends.remove(friendId);
        user.setFriends(friends);

        friends = friend.getFriends();
        friends.remove(userId);
        friend.setFriends(friends);
    }

    public Collection<User> commonFriends(Integer userId, Integer otherId) {
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
        User user = userStorage.findById(userId);
        return user.getFriends().stream().map(userStorage::findById).collect(Collectors.toList());
    }
}
