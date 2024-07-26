package ru.yandex.practicum.filmorate.exception;

public class MpaNotFoundException extends DataNotFoundException {
    public MpaNotFoundException(String message) {
        super(message);
    }
}
