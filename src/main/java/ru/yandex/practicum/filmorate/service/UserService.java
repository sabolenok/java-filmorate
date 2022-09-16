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
        Set<Integer> commonFriends = user.getFriends()
                .stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toSet());
        List<User> common = new ArrayList<>();
        for (Integer id : commonFriends) {
            common.add(userStorage.findById(id));
        }
        return common;
    }

    public Collection<User> getUsersFriends(Integer userId) {
        User user = userStorage.findById(userId);
        List<User> friends = new ArrayList<>();
        for (Integer id : user.getFriends()) {
            friends.add(userStorage.findById(id));
        }
        return friends;
    }
}
