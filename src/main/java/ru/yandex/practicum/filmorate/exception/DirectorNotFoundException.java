package ru.yandex.practicum.filmorate.exception;

public class DirectorNotFoundException extends DataNotFoundException {
    public DirectorNotFoundException(String message) {
        super(message);
    }
}
