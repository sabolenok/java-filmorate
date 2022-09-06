package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();

    @GetMapping("/users")
    public Collection<User> findAll() {
        return users.values();
    }

    @RequestMapping(value = "/users", method = {RequestMethod.POST, RequestMethod.PUT})
    public User create(@RequestBody User user) {
        users.put(user.getId(), user);
        return user;
    }
}
