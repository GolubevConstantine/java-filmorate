package ru.yandex.practicum.filmorate.exception;

import lombok.Getter;

@Getter
public record ErrorResponse(String error) {

}
