package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import java.util.List;

@Validated
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("films")
public class FilmController {
    private final FilmService filmService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST / film / {}", film.getName());
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("PUT / film / {}", film.getName());
        return filmService.update(film);
    }

    @DeleteMapping("/{id}")
    public void deleteFilm(@PathVariable("id") int id) {
        filmService.deleteFilmById(id);
        log.info("Удален фильм id {}", id);
    }

    @GetMapping
    public List<Film> findAllFilms() {
        log.info("GET / films");
        return filmService.findAllFilms();
    }

    @GetMapping("/{id}")
    public Film findFilmById(@PathVariable("id") int id) {
        log.info("GET / {}", id);
        return filmService.findFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addFilmLike(@PathVariable("id") int id, @PathVariable("userId") int userId) {
        log.info("PUT / {} / like / {}", id, userId);
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeFilmLike(@PathVariable("id") Integer id, @PathVariable("userId") Integer userId) {
        log.info("DELETE / {} / like / {}", id, userId);
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> findPopular(@RequestParam(defaultValue = "10", required = false) Integer count,
                                  @RequestParam(required = false) Integer genreId,
                                  @RequestParam(required = false) Integer year) {
        log.info("GET / popular count={} genreId={} year={}", count, genreId, year);
        return filmService.findPopular(count, genreId, year);
    }

    @GetMapping("/director/{id}")
    public List<Film> findFilmsByDirectorID(@PathVariable("id") Integer id, @RequestParam("sortBy") String sortedBy) {
        log.info("GET / director / {} / sortBy {}", id, sortedBy);
        return filmService.findFilmsByDirectorID(id, sortedBy);
    }

    @GetMapping("/common")
    public List<Film> findCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        log.info("GET / common ? userId={} & friendId={} ", userId, friendId);
        return filmService.findCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    public List<Film> searchFilm(@RequestParam String query, @RequestParam List<String> by) {
        return filmService.searchFilm(query, by);
    }
}
