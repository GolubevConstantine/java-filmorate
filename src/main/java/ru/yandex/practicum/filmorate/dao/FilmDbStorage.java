package ru.yandex.practicum.filmorate.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String SUB_QUERY = """
            SELECT f.film_id,
                   f.name,
                   f.description,
                   f.releaseDate,
                   f.duration,
                   f.rating_id,
                   COUNT(l.film_id) AS liked_users_count
            FROM films AS f
            LEFT JOIN likes AS l ON f.film_id = l.film_id
            GROUP BY f.film_id
            """;

    private static final String SELECT_FILMS_WITH_SUB_QUERY = """
            SELECT sub.*,
                   l.user_id AS liked_user_id,
                   g.genre_id,
                   g.name    AS genre_name,
                   mpa.name  AS rating_name,
                   d.director_id,
                   d.name    AS director_name
            FROM (%s) AS sub
                     LEFT JOIN likes AS l ON l.film_id = sub.film_id
                     LEFT JOIN film_genres AS fg ON fg.film_id = sub.film_id
                     LEFT JOIN genres AS g ON g.genre_id = fg.genre_id
                     LEFT JOIN mpa_rating AS mpa ON mpa.rating_id = sub.rating_id
                     LEFT JOIN film_directors AS fd ON fd.film_id = sub.film_id
                     LEFT JOIN directors AS d ON d.director_id = fd.director_id
            """;

    private static final String SELECT_ALL_FILMS = SELECT_FILMS_WITH_SUB_QUERY.formatted(SUB_QUERY);

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, releaseDate, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
                    ps.setString(1, film.getName());
                    ps.setString(2, film.getDescription());
                    ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                    ps.setInt(4, film.getDuration());
                    ps.setInt(5, film.getMpa().getId());
                    return ps;
                }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        updateDirectors(film.getDirectors(), film.getId());
        updateGenres(film.getGenres(), film.getId());

        return findFilmById(film.getId()).orElse(null);
    }

    @Override
    public Film update(Film film) {
        int id = film.getId();
        String sql = "UPDATE films SET name = ?, description = ?, releaseDate = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa().getId(), id);
        updateGenres(film.getGenres(), id);
        updateDirectors(film.getDirectors(), id);

        return findFilmById(id).orElse(null);
    }

    @Override
    public List<Film> findAllFilms() {
        String sql = SELECT_ALL_FILMS + "ORDER BY sub.film_id";
        return jdbcTemplate.query(sql, new FilmListExtractor());
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sql = SELECT_ALL_FILMS + "WHERE sub.film_id = ?";

        List<Film> filmList = jdbcTemplate.query(sql, new FilmListExtractor(), id);
        return filmList != null ? filmList.stream().findFirst() : Optional.empty();
    }

    @Override
    public void deleteFilmById(int id) {
        String sql = "DELETE FROM FILMS WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Film> findPopular(Integer count, Integer genreId, Integer year) {
        String subQuery = SUB_QUERY + "ORDER BY liked_users_count DESC";
        if (count != null) {
            subQuery += " LIMIT %d".formatted(count);
        }
        String sql = SELECT_FILMS_WITH_SUB_QUERY.formatted(subQuery);

        if (genreId != null && year != null) {
            sql += "WHERE sub.film_id IN (SELECT film_id FROM film_genres WHERE genre_id = ?) AND EXTRACT(YEAR FROM sub.releasedate) = ?";
            return jdbcTemplate.query(sql, new FilmListExtractor(), genreId, year);
        } else if (genreId != null) {
            sql += "WHERE sub.film_id IN (SELECT film_id FROM film_genres WHERE genre_id = ?)";
            return jdbcTemplate.query(sql, new FilmListExtractor(), genreId);
        } else if (year != null) {
            sql += "WHERE EXTRACT(YEAR FROM sub.releasedate) = ?";
            return jdbcTemplate.query(sql, new FilmListExtractor(), year);
        }

        return jdbcTemplate.query(sql, new FilmListExtractor());
    }

    @Override
    public List<Film> findFilmsByDirectorID(int id, String sortedBy) {
        String sql = SELECT_ALL_FILMS;
        sql += "WHERE d.director_id = ? ";
        if (!sortedBy.isBlank()) {
            if (sortedBy.equals("year")) {
                sql += "ORDER BY sub.releasedate";
            } else if (sortedBy.equals("likes")) {
                sql += "ORDER BY sub.liked_users_count DESC";
            }
        }

        return jdbcTemplate.query(sql, new FilmListExtractor(), id);
    }

    /**
     * Пересечения по лайкам для фильмов выбранного пользователя
     * с лайками остальных пользователей содежатся в HashMap,
     * где ключ - количесво пересечений id фильмов каждого пользователя,
     * значение - Set из id общих фильмов.
     * При наличии ключа фильмы дополняются новыми.
     * Список id рекомендуемых фильмов соответствует максимальному значению ключа.
     */
    @Override
    public List<Film> findRecommendedFilms(int userId) {
        String sql = "SELECT user_id, film_id FROM likes WHERE user_id IN (" +
                "SELECT user_id FROM likes WHERE film_id IN (" +
                "SELECT film_id FROM likes WHERE user_id = ?))";

        Map<Integer, LinkedHashSet<Integer>> res = jdbcTemplate.query(sql, new UserFilmExtractor(), userId);
        if (res == null || res.get(userId) == null || res.get(userId).isEmpty()) {
            return Collections.emptyList();
        }

        LinkedHashSet<Integer> userLikedFilms = res.get(userId);
        res.remove(userId);

        Map<Integer, HashSet<Integer>> countToIntersections = new HashMap<>();
        int maxCount = 0;
        for (Set<Integer> films : res.values()) {

            Set<Integer> userIntersections = new HashSet<>(userLikedFilms);
            userIntersections.retainAll(films);

            int intersectionsCount = userIntersections.size();
            if (intersectionsCount > maxCount) {
                maxCount = intersectionsCount;
            }

            if (!countToIntersections.containsKey(intersectionsCount)) {
                countToIntersections.put(intersectionsCount, new HashSet<>());
            }
            countToIntersections.get(intersectionsCount).addAll(films);
        }

        if (maxCount == 0) {
            return Collections.emptyList();
        }

        List<Integer> recommendedFilmIds = countToIntersections.get(maxCount).stream()
                .filter(filmId -> !userLikedFilms.contains(filmId))
                .toList();

        if (recommendedFilmIds.isEmpty()) {
            return Collections.emptyList();
        }

        return jdbcTemplate.query(
                SELECT_ALL_FILMS + "WHERE sub.film_id IN (?)",
                new FilmListExtractor(),
                recommendedFilmIds.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
        );
    }

    @Override
    public List<Film> findCommonFilms(int userId, int friendId) {
        String sql = SELECT_ALL_FILMS +
                "WHERE sub.film_id IN (" +
                "SELECT film_id FROM likes WHERE user_id = ? " +
                "INTERSECT " +
                "SELECT film_id FROM likes WHERE user_id = ?)";

        return jdbcTemplate.query(sql, new FilmListExtractor(), userId, friendId);
    }

    @Override
    public List<Film> searchFilmsByDirAndName(String query) {
        String name = "%" + query + "%";
        String sql = SELECT_ALL_FILMS +
                "WHERE UPPER(sub.name) LIKE UPPER(?) " +
                "OR UPPER(d.name) LIKE UPPER(?) " +
                "ORDER BY sub.film_id DESC";

        return jdbcTemplate.query(sql, new FilmListExtractor(), name, name);
    }

    @Override
    public List<Film> searchFilmsByName(String query) {
        String name = "%" + query + "%";
        String sql = SELECT_ALL_FILMS +
                "WHERE UPPER(sub.name) LIKE UPPER(?) " +
                "ORDER BY sub.film_id DESC";

        return jdbcTemplate.query(sql, new FilmListExtractor(), name);
    }

    @Override
    public List<Film> searchFilmsByDir(String query) {
        String name = "%" + query + "%";
        String sql = SELECT_ALL_FILMS +
                "WHERE UPPER(d.name) LIKE UPPER(?) " +
                "ORDER BY sub.film_id DESC";

        return jdbcTemplate.query(sql, new FilmListExtractor(), name);
    }

    private void updateGenres(Set<Genre> genres, int id) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", id);
        if (genres != null && !genres.isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
            Genre[] g = genres.toArray(new Genre[0]);
            jdbcTemplate.batchUpdate(
                    sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, id);
                            ps.setInt(2, g[i].getId());
                        }

                        public int getBatchSize() {
                            return genres.size();
                        }
                    });
        }
    }

    private void updateDirectors(Set<Director> directors, int director_id) {
        jdbcTemplate.update("DELETE FROM FILM_DIRECTORS WHERE film_id = ?", director_id);
        if (directors != null && !directors.isEmpty()) {
            String sql = "INSERT INTO FILM_DIRECTORS (film_id, director_id) VALUES (?, ?)";
            Director[] g = directors.toArray(new Director[0]);
            jdbcTemplate.batchUpdate(
                    sql,
                    new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, director_id);
                            ps.setInt(2, g[i].getId());
                        }

                        public int getBatchSize() {
                            return directors.size();
                        }
                    });
        }
    }

    private static class UserFilmExtractor implements ResultSetExtractor<Map<Integer, LinkedHashSet<Integer>>> {

        @Override
        public Map<Integer, LinkedHashSet<Integer>> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            Map<Integer, LinkedHashSet<Integer>> userIdToFilmIds = new HashMap<>();
            while (resultSet.next()) {
                Integer userId = resultSet.getInt("user_id");
                Integer filmId = resultSet.getInt("film_id");

                if (!userIdToFilmIds.containsKey(userId)) {
                    userIdToFilmIds.put(userId, new LinkedHashSet<>());
                }

                userIdToFilmIds.get(userId).add(filmId);
            }
            return userIdToFilmIds;
        }
    }

    private static class FilmListExtractor implements ResultSetExtractor<List<Film>> {

        @Override
        public List<Film> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
            LinkedHashMap<Integer, Film> idToFilm = new LinkedHashMap<>();

            while (resultSet.next()) {
                Integer filmId = resultSet.getInt("film_id");
                if (!idToFilm.containsKey(filmId)) {
                    Film film = Film.builder()
                            .id(filmId)
                            .name(resultSet.getString("name"))
                            .description(resultSet.getString("description"))
                            .releaseDate(resultSet.getDate("releaseDate").toLocalDate())
                            .duration(resultSet.getInt("duration"))
                            .genres(new LinkedHashSet<>())
                            .build();
                    idToFilm.put(filmId, film);
                }

                Film film = idToFilm.get(filmId);
                int likedUserId = resultSet.getInt("liked_user_id");
                if (likedUserId > 0) {
                    film.getLikes().add(likedUserId);
                }

                int ratingId = resultSet.getInt("rating_id");
                if (ratingId > 0) {
                    Mpa rating = new Mpa(ratingId, resultSet.getString("rating_name"));
                    film.setMpa(rating);
                }

                int genreId = resultSet.getInt("genre_id");
                if (genreId > 0) {
                    Genre genre = new Genre(genreId, resultSet.getString("genre_name"));
                    film.getGenres().add(genre);
                }

                int directorId = resultSet.getInt("director_id");
                if (directorId > 0) {
                    Director director = new Director(directorId, resultSet.getString("director_name"));
                    film.getDirectors().add(director);
                }
            }

            return new ArrayList<>(idToFilm.values());
        }
    }
}
