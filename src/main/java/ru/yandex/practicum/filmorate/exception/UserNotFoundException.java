package ru.yandex.practicum.filmorate.exception;

public class UserNotFoundException extends DataNotFoundException {
    public UserNotFoundException(String message) {
        super(message);
    }
}