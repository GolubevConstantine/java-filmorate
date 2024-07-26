package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEntry;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedOperationType;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FriendStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendStorage friendStorage;
    private final FeedService feedService;

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validate(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);
        userStorage.findUserById(user.getId()).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", user.getId())));
        return userStorage.update(user);
    }

    public User findUserById(int id) {
        return userStorage.findUserById(id).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", id)));
    }

    public void addFriend(int userId, int friendId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", userId)));
        userStorage.findUserById(friendId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден друг с id=%d", friendId)));
        friendStorage.addFriend(userId, friendId);

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(userId)
                .eventType(FeedEventType.FRIEND)
                .operation(FeedOperationType.ADD)
                .entityId(friendId)
                .build();
        feedService.create(feedEntry);

    }

    public List<User> findAllFriends(int id) {
        userStorage.findUserById(id).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", id)));
        return friendStorage.findAllFriends(id);
    }

    public List<User> findCommonFriends(int id, int otherId) {
        return friendStorage.findCommonFriends(id, otherId);
    }

    public void removeFriend(int userId, int friendId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", userId)));
        userStorage.findUserById(friendId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", friendId)));
        friendStorage.removeFriend(userId, friendId);

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(userId)
                .eventType(FeedEventType.FRIEND)
                .operation(FeedOperationType.REMOVE)
                .entityId(friendId)
                .build();
        feedService.create(feedEntry);

    }

    private void validate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public void deleteUserById(int id) {
        userStorage.findUserById(id).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", id)));
        userStorage.deleteUserById(id);
    }
}