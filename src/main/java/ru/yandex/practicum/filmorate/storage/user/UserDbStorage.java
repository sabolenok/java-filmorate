package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

@Component("inDbUser")
@Slf4j
public class UserDbStorage implements UserStorage {
    @Override
    public Collection<User> findAll() {
        return null;
    }

    @Override
    public User create(User user) {
        return null;
    }

    @Override
    public User put(User user) {
        return null;
    }

    @Override
    public User findById(Integer id) {
        return null;
    }
}
