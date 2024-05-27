package ru.yandex.practicum.catsgram.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.catsgram.exception.ConditionsNotMetException;
import ru.yandex.practicum.catsgram.exception.DuplicatedDataException;
import ru.yandex.practicum.catsgram.exception.NotFoundException;
import ru.yandex.practicum.catsgram.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }

        if (findUserByEmail(user).isPresent()) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }

        user.setId(getNextId());
        user.setRegistrationDate(Instant.now());

        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());

            if (findUserByEmail(newUser).isPresent()) {
                throw new DuplicatedDataException("Этот имейл уже используется");
            }

            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }

            if (newUser.getUsername() != null) {
                oldUser.setUsername(newUser.getUsername());
            }

            if (newUser.getPassword() != null) {
                oldUser.setPassword(newUser.getPassword());
            }
        }

        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    Optional<User> findUserByEmail(User user) {
        return users.values()
                .stream()
                .filter(existingUser -> existingUser.getEmail().equalsIgnoreCase(user.getEmail()))
                .findFirst();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }


}
