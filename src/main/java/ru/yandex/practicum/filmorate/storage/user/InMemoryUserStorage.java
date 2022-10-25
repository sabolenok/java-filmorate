package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component("inMemoryUser")
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    @Getter
    private static Integer id = 0;

    private static Integer getNextId() {
        return ++id;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        checkName(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь \"'{}'\" создан", user.getName());

        return user;
    }

    @Override
    public User put(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }
        checkName(user);
        users.put(user.getId(), user);
        log.info("Пользователь \"'{}'\" изменен", user.getName());

        return user;
    }

    private void checkName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    @Override
    public User findById(Integer id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь не найден");
        }
        return users.get(id);
    }
}
