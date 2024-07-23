package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEntry;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
    public final FeedStorage feedStorage;
    public final UserStorage userStorage;

    public FeedEntry create(FeedEntry feedEntry) {
        feedEntry.setTimestamp(Instant.now().toEpochMilli());
        return feedStorage.create(feedEntry);
    }

    public List<FeedEntry> getUserFeed(int userId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        return feedStorage.getUserFeed(userId);
    }
}
