package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> findAllGenres();

    Optional<Genre> findGenreById(int id);
    Optional<Genre> findGenreByIds(List<Integer> ids);

    void findAllGenresByFilm(List<Film> films);

    List<Genre> findAllGenresByFilmID(int id);
}
