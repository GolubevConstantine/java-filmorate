package ru.yandex.practicum.filmorate.exception;

public class GenreNotFoundException extends DataNotFoundException {
    public GenreNotFoundException(String message) {
        super(message);
    }
}
