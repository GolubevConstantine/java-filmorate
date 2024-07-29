package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DataNotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.*;

import java.util.List;

@RequiredArgsConstructor
@Service
public class DirectorService {
    private final DirectorStorage directorStorage;

    public List<Director> findAllDirectors() {
        return directorStorage.findAllDirectors();
    }

    public Director create(Director director) {
        return directorStorage.create(director);
    }

    public Director update(Director director) {
        if (directorStorage.findDirectorById(director.getId()).isEmpty()) {
            throw new DataNotFoundException(String.format("Не найден режиссер с id=%d", director.getId()));
        }
        return directorStorage.update(director);
    }

    public Director findDirectorById(int id) {
        return directorStorage.findDirectorById(id).orElseThrow(() -> new DataNotFoundException(String.format("Не найден режиссер с id=%d", id)));
    }

    public void removeDirectorById(int id) {
        directorStorage.findDirectorById(id).orElseThrow(() -> new DataNotFoundException(String.format("Не найден режиссер с id=%d", id)));
        directorStorage.removeDirectorById(id);
    }
}