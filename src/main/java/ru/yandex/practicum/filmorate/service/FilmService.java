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
            throw new ValidationException("Mpa not found");
        }

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreStorage.findGenreById(genre.getId()).orElseThrow(() -> new ValidationException("Жанр " + genre.getId() + " не найден.")));
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        filmStorage.findFilmById(film.getId()).orElseThrow(() -> new FilmNotFoundException("Фильм не найден."));
        return filmStorage.update(film);
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id).orElseThrow(() -> new FilmNotFoundException("Фильм не найден."));
    }

    public void addLike(int id, int userId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден."));
        filmStorage.findFilmById(id).orElseThrow(() -> new FilmNotFoundException("Фильм не найден."));

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(userId)
                .eventType(FeedEventType.LIKE)
                .operation(FeedOperationType.ADD)
                .entityId(id)
                .build();
        feedService.create(feedEntry);
        likeStorage.addLike(id, userId);
    }

    public void removeLike(int id, int userId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден."));
        filmStorage.findFilmById(id).orElseThrow(() -> new FilmNotFoundException("Фильм не найден."));
        likeStorage.removeLike(id, userId);

        FeedEntry feedEntry = FeedEntry.builder()
                .userId(userId)
                .eventType(FeedEventType.LIKE)
                .operation(FeedOperationType.REMOVE)
                .entityId(id)
                .build();
        feedService.create(feedEntry);
    }

    public List<Film> findPopular(Integer count, Integer genreId, Integer year) {
        return filmStorage.findPopular(count, genreId, year);
    }

    public List<Film> findFilmsByDirectorID(int id, String sortedBy) {
        directorStorage.findDirectorById(id).orElseThrow(() -> new DirectorNotFoundException("Директор не найден."));
        return filmStorage.findFilmsByDirectorID(id, sortedBy);
    }

    public List<Mpa> findAllMpa() {
        return mpaStorage.findAllMpa();
    }

    public Mpa findMpaById(int id) {
        return mpaStorage.findMpaById(id).orElseThrow(() -> new MpaNotFoundException("Рейтинг MPA не найден."));
    }

    public List<Genre> findAllGenres() {
        return genreStorage.findAllGenres();
    }

    public Genre findGenreById(int id) {
        return genreStorage.findGenreById(id).orElseThrow(() -> new GenreNotFoundException("Жанр не найден."));
    }

    public void deleteFilmById(int id) {
        filmStorage.findFilmById(id).orElseThrow(() -> new FilmNotFoundException("Фильм не найден."));
        filmStorage.deleteFilmById(id);
    }

    public List<Film> findRecommendedFilms(int userId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден."));
        return filmStorage.findRecommendedFilms(userId);
    }

    public List<Film> findCommonFilms(int userId, int friendId) {
        userStorage.findUserById(userId).orElseThrow(() -> new UserNotFoundException("Пользователь не найден."));
        userStorage.findUserById(friendId).orElseThrow(() -> new UserNotFoundException("Друг среди пользователей не найден."));

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
