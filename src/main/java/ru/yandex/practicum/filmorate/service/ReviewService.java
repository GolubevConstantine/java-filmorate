package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.ReviewNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
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

    public Review create(Review review) {
        if (reviewStorage.isAlreadyExists(review)) {
            throw new IllegalArgumentException("Отзыв к этому фильму был добавлен ранее.");
        }
        throwExceptionIfFilmNotFound(review.getFilmId());
        throwExceptionIfUserNotFound(review.getUserId());

        return reviewStorage.create(review);
    }

    public Review update(Review review) {
        throwExceptionIfReviewNotFound(review.getId());
        return reviewStorage.update(review);
    }

    public void delete(int id) {
        throwExceptionIfReviewNotFound(id);
        reviewStorage.delete(id);
    }

    public List<Review> findAll(int limit) {
        return reviewStorage.findAll(limit);
    }

    public List<Review> findByFilmId(int filmId, int limit) {
        return reviewStorage.findByFilmId(filmId, limit);
    }

    public Review findById(int id) {
        return reviewStorage.findById(id).orElseThrow(() -> new ReviewNotFoundException("Отзыв не найден."));
    }

    public Review addLike(int id, int userId) {
        throwExceptionIfReviewNotFound(id);
        throwExceptionIfUserNotFound(userId);

        reviewStorage.addLike(id, userId);
        return findById(id);
    }

    public Review addDislike(int id, int userId) {
        throwExceptionIfReviewNotFound(id);
        throwExceptionIfUserNotFound(userId);

        reviewStorage.addDislike(id, userId);
        return findById(id);
    }

    public Review deleteLike(int id, int userId) {
        throwExceptionIfReviewNotFound(id);
        throwExceptionIfUserNotFound(userId);

        reviewStorage.deleteLike(id, userId);
        return findById(id);
    }

    public Review deleteDislike(int id, int userId) {
        throwExceptionIfReviewNotFound(id);
        throwExceptionIfUserNotFound(userId);

        reviewStorage.deleteDislike(id, userId);
        return findById(id);
    }

    private void throwExceptionIfReviewNotFound(int id) {
        if (reviewStorage.findById(id).isEmpty()) {
            throw new ReviewNotFoundException("Отзыв не найден.");
        }
    }

    private void throwExceptionIfUserNotFound(int userId) {
        if (userStorage.findUserById(userId).isEmpty()) {
            throw new UserNotFoundException("Пользователь не найден.");
        }
    }

    private void throwExceptionIfFilmNotFound(int userId) {
        if (filmStorage.findFilmById(userId).isEmpty()) {
            throw new FilmNotFoundException("Фильм не найден.");
        }
    }
}