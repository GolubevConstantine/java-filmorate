package ru.yandex.practicum.filmorate.exception;

public class ReviewNotFoundException extends DataNotFoundException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
