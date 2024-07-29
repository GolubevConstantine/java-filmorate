package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film create(Film film);

    Film update(Film film);

    List<Film> findAllFilms();

    List<Film> findPopular(Integer count, Integer genreId, Integer year);

    List<Film> findFilmsByDirectorID(int id, String sortedBy);

    List<Film> findRecommendedFilms(int userId);

    List<Film> findCommonFilms(int userId, int friendId);

    Optional<Film> findFilmById(int id);

    void deleteFilmById(int id);

    List<Film> searchFilmsByDirAndName(String query);

    List<Film> searchFilmsByName(String query);

    List<Film> searchFilmsByDir(String query);
}
