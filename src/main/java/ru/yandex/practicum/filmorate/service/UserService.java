package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserService {
    private final UserStorage userStorage;

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        validate(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validate(user);
        return userStorage.update(user);
    }

    public User findUserByIdService(int id) {
        return userStorage.findUserById(id).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
    }

    public void addFriend(Integer id, Integer friendId) {
        if (id < 0 || friendId < 0) {
            throw new UserNotFoundException("Пользователь не найден.");
        }
        userStorage.findUserById(id).getFriends().add(friendId);
        userStorage.findUserById(friendId).getFriends().add(id);
    }

    public List<User> findAllFriends(Integer id) {
        List<User> friendsList = new ArrayList<>();
        Set<Integer> friends = userStorage.findUserById(id).getFriends();
        if (friends.isEmpty()) {
            return friendsList;
        }
        return friends.stream()
                .map(userStorage::findUserById)
                .collect(Collectors.toList());
    }

    public List<User> findCommonFriends(int id, int otherId) {
        List<User> commonFriends = findAllFriends(id);
        List<User> commonFriendsSecond = findAllFriends(otherId);
        commonFriends.retainAll(commonFriendsSecond);
        return commonFriends;
    }

    public void removeFriend(Integer id, Integer friendId) {
        userStorage.findUserById(id).getFriends().remove(friendId);
        userStorage.findUserById(friendId).getFriends().remove(id);
    }

    private void validate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}