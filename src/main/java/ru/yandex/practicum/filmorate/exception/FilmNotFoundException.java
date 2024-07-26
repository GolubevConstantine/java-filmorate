package ru.yandex.practicum.filmorate.exception;

public class FilmNotFoundException extends DataNotFoundException {
    public FilmNotFoundException(String message) {
        super(message);
    }
}