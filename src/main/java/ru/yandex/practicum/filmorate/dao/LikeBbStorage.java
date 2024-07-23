package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.storage.LikeStorage;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class LikeBbStorage implements LikeStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addLike(int id, int userId) {
        List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(
                "SELECT FROM likes WHERE film_id = ? AND user_id = ?",
                id,
                userId);
        if (queryForList.isEmpty()) {
            String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
            jdbcTemplate.update(sql, id, userId);
        }
    }

    @Override
    public void removeLike(int id, int userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, id, userId);
    }
}
