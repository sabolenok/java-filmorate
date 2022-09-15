package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(Integer userId, Integer friendId) {
        User user = userStorage.findById(userId);
        Set<Integer> friends = user.getFriends();
        friends.add(friendId);
        user.setFriends(friends);

        User friend = userStorage.findById(friendId);
        friends = friend.getFriends();
        friends.add(userId);
        friend.setFriends(friends);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = userStorage.findById(userId);
        Set<Integer> friends = user.getFriends();
        friends.remove(friendId);
        user.setFriends(friends);

        User friend = userStorage.findById(friendId);
        friends = friend.getFriends();
        friends.remove(userId);
        friend.setFriends(friends);
    }

    public Set<Integer> commonFriends(Integer userId, Integer otherId) {
        User user = userStorage.findById(userId);
        User other = userStorage.findById(otherId);
        Set<Integer> otherFriends = other.getFriends();
        return user.getFriends()
                .stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toSet());
    }
}
