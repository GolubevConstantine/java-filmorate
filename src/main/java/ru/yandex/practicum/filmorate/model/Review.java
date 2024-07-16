package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {
    @JsonProperty("reviewId")
    private int id;

    @NotBlank(message = "Cодержание отзыва не может быть пустым.")
    @Size(max = 1000, message = "Слишком длинное содержание.")
    private String content;

    @NotNull
    private boolean isPositive;

    @Positive
    private int userId;

    @Positive
    private int filmId;

    private int useful;
}
