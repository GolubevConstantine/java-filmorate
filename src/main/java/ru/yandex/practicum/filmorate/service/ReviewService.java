package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.FeedEntry;
import ru.yandex.practicum.filmorate.model.FeedEventType;
import ru.yandex.practicum.filmorate.model.FeedOperationType;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewStorage reviewStorage;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final FeedService feedService;

    public Review create(Review review) {
        if (reviewStorage.isAlreadyExists(review)) {
            throw new IllegalArgumentException(String.format("Отзыв к фильму с id=%d уже был добавлен ранее.", review.getFilmId()));
        }
        checkUserExists(review.getUserId());
        checkFilmExists(review.getFilmId());

        Review createdReview = reviewStorage.create(review);

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(review.getUserId())
                .eventType(FeedEventType.REVIEW)
                .operation(FeedOperationType.ADD)
                .entityId(createdReview.getId())
                .build();
        feedService.create(feedEntry);

        return createdReview;
    }

    public Review update(Review review) {
        checkReviewExists(review.getId());
        Review updatedReview = reviewStorage.update(review);

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(updatedReview.getUserId())
                .eventType(FeedEventType.REVIEW)
                .operation(FeedOperationType.UPDATE)
                .entityId(updatedReview.getId())
                .build();
        feedService.create(feedEntry);

        return updatedReview;
    }

    public void delete(int id) {
        checkReviewExists(id);

        Review review = findById(id);
        FeedEntry feedEntry = FeedEntry.builder()
                .userId(review.getUserId())
                .eventType(FeedEventType.REVIEW)
                .operation(FeedOperationType.REMOVE)
                .entityId(review.getId())
                .build();
        feedService.create(feedEntry);

        reviewStorage.delete(id);
    }

    public List<Review> findAll(int limit) {
        return reviewStorage.findAll(limit);
    }

    public List<Review> findByFilmId(int filmId, int limit) {
        return reviewStorage.findByFilmId(filmId, limit);
    }

    public Review findById(int id) {
        return reviewStorage.findById(id).orElseThrow(() -> new DataNotFoundException(String.format("Не найден отзыв с id=%d", id)));
    }

    public Review addLike(int id, int userId) {
        checkReviewExists(id);
        checkUserExists(userId);

        reviewStorage.addLike(id, userId);
        return findById(id);
    }

    public Review addDislike(int id, int userId) {
        checkReviewExists(id);
        checkUserExists(userId);

        reviewStorage.addDislike(id, userId);
        return findById(id);
    }

    public Review deleteLike(int id, int userId) {
        checkReviewExists(id);
        checkUserExists(userId);

        reviewStorage.deleteLike(id, userId);
        return findById(id);
    }

    public Review deleteDislike(int id, int userId) {
        checkReviewExists(id);
        checkUserExists(userId);

        reviewStorage.deleteDislike(id, userId);
        return findById(id);
    }

    private void checkReviewExists(int id) {
        if (reviewStorage.findById(id).isEmpty()) {
            throw new DataNotFoundException(String.format("Не найден отзыв с id=%d", id));
        }
    }

    private void checkUserExists(int userId) {
        if (userStorage.findUserById(userId).isEmpty()) {
            throw new DataNotFoundException(String.format("Не найден пользователь с id=%d", userId));
        }
    }

    private void checkFilmExists(int userId) {
        if (filmStorage.findFilmById(userId).isEmpty()) {
            throw new DataNotFoundException(String.format("Не найден фильм для пользователя с id=%d", userId));
        }
    }
}
