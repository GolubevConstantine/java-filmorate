package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.FilmNotFoundException;
import ru.yandex.practicum.filmorate.exception.GenreNotFoundException;
import ru.yandex.practicum.filmorate.exception.MpaNotFoundException;
import ru.yandex.practicum.filmorate.exception.UserNotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;
    private final UserStorage userStorage;

    public List<Film> findAllFilms() {
        List<Film> films = filmStorage.findAllFilms();
        genreStorage.findAllGenresByFilm(films);
        return films;
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (filmStorage.findFilmById(film.getId()).isEmpty()) {
            throw new FilmNotFoundException("Фильм не найден.");
        }
        return filmStorage.update(film);
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id).orElseThrow(() -> new FilmNotFoundException("Фильм не найден."));
    }

    public void addLike(int id, int userId) {
        User user = userStorage.findUserById(userId).orElseThrow(() -> new FilmNotFoundException("Пользователь не найден."));

        findFilmById(id).getLikes().add(user.getId());
    }

    public void removeLike(int id, int userId) {
        Set<Integer> likes = findFilmById(id).getLikes();
        if (!likes.contains(userId)) {
            throw new UserNotFoundException("Пользователь не найден.");
        }
        likes.remove(userId);
    }

    public List<Film> findPopular(int count) {
        List<Film> films = filmStorage.findPopular(count);
        genreStorage.findAllGenresByFilm(films);
        return films;
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
}