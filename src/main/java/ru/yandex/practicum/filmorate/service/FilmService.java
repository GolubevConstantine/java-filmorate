package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.*;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;
    private final UserStorage userStorage;
    private final FeedService feedService;
    private final DirectorStorage directorStorage;

    public List<Film> findAllFilms() {
        return filmStorage.findAllFilms();
    }

    public Film create(Film film) {
        Optional<Mpa> mpa = mpaStorage.findMpaById(film.getMpa().getId());
        if (mpa.isEmpty()) {
            throw new ValidationException(String.format("Не найден рейтинг mpa с id=%d", film.getMpa().getId()));
        }

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreStorage.findGenreById(genre.getId()).orElseThrow(() -> new ValidationException(String.format("Не найден жанр с id=%d", genre.getId()))));
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.findFilmById(film.getId()).orElseThrow(() -> new FilmNotFoundException(String.format("Не найден фильм с id=%d", film.getId())));
        return filmStorage.update(film);
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id).orElseThrow(() -> new FilmNotFoundException(String.format("Не найден фильм с id=%d", id)));
    }

    public void addLike(int filmId, int userId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", userId)));
        filmStorage.findFilmById(filmId).orElseThrow(() -> new FilmNotFoundException(String.format("Не найден фильм с id=%d", filmId)));

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(userId)
                .eventType(FeedEventType.LIKE)
                .operation(FeedOperationType.ADD)
                .entityId(filmId)
                .build();
        feedService.create(feedEntry);
        likeStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", userId)));
        filmStorage.findFilmById(filmId).orElseThrow(() -> new FilmNotFoundException(String.format("Не найден фильм с id=%d", filmId)));
        likeStorage.removeLike(filmId, userId);

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(userId)
                .eventType(FeedEventType.LIKE)
                .operation(FeedOperationType.REMOVE)
                .entityId(filmId)
                .build();
        feedService.create(feedEntry);
    }

    public List<Film> findPopular(Integer count, Integer genreId, Integer year) {
        return filmStorage.findPopular(count, genreId, year);
    }

    public List<Film> findFilmsByDirectorID(int id, String sortedBy) {
        directorStorage.findDirectorById(id).orElseThrow(() -> new DirectorNotFoundException(String.format("Не найден директор с id=%d", id)));
        return filmStorage.findFilmsByDirectorID(id, sortedBy);
    }

    public List<Mpa> findAllMpa() {
        return mpaStorage.findAllMpa();
    }

    public Mpa findMpaById(int id) {
        return mpaStorage.findMpaById(id).orElseThrow(() -> new MpaNotFoundException(String.format("Не найден рейтинг MPA с id=%d", id)));
    }

    public List<Genre> findAllGenres() {
        return genreStorage.findAllGenres();
    }

    public Genre findGenreById(int id) {
        return genreStorage.findGenreById(id).orElseThrow(() -> new GenreNotFoundException(String.format("Не найден жанр с id=%d", id)));
    }

    public void deleteFilmById(int id) {
        filmStorage.findFilmById(id).orElseThrow(() -> new FilmNotFoundException(String.format("Не найден фильм с id=%d", id)));
        filmStorage.deleteFilmById(id);
    }

    public List<Film> findRecommendedFilms(int userId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", userId)));
        return filmStorage.findRecommendedFilms(userId);
    }

    public List<Film> findCommonFilms(int userId, int friendId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден пользователь с id=%d", userId)));
        userStorage.findUserById(friendId).orElseThrow(() -> new UserNotFoundException(String.format("Не найден друг с id=%d", friendId)));

        return filmStorage.findCommonFilms(userId, friendId);
    }

    public List<Film> searchFilm(String query, List<String> by) {
        if (by.size() == 1 && by.contains("title")) {
            return filmStorage.searchFilmsByName(query);
        }
        if (by.size() == 1 && by.contains("director")) {
            return filmStorage.searchFilmsByDir(query);
        }
        return filmStorage.searchFilmsByDirAndName(query);
    }
}
