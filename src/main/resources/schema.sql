DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS friendship CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa_rating CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS likes CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS review_actions CASCADE;
DROP TABLE IF EXISTS feed CASCADE;

create table if not exists users
(
    user_id  int generated by default as identity primary key,
    email    varchar      NOT NULL,
    login    varchar(255) NOT NULL,
    name     varchar(255),
    birthday date         NOT NULL
);

create unique index if not exists USER_EMAIL_UINDEX on USERS (email);
create unique index if not exists USER_LOGIN_UINDEX on USERS (login);

create table if not exists friendship
(
    PRIMARY KEY (user_id, friend_id),
    user_id   int,
    friend_id int,
    accepted  boolean,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (friend_id) REFERENCES users (user_id)
);

create table if not exists mpa_rating
(
    rating_id int auto_increment,
    name      varchar(255)
);

create table if not exists PUBLIC.DIRECTORS
(
    DIRECTOR_ID INTEGER auto_increment
        primary key,
    NAME        CHARACTER VARYING(255)
);


create table if not exists films
(
    film_id     int generated by default as identity primary key,
    name        varchar(255) NOT NULL,
    description varchar(200) NOT NULL,
    releaseDate date         NOT NULL,
    duration    int,
    rating_id   int
);

create table if not exists PUBLIC.FILM_DIRECTORS
(
    ID          INTEGER auto_increment,
    FILM_ID     INTEGER
        references PUBLIC.FILMS,
    DIRECTOR_ID INTEGER
        references PUBLIC.DIRECTORS
);

create table if not exists likes
(
    film_id int,
    user_id int,
    PRIMARY KEY (user_id, film_id),
    FOREIGN KEY (film_id) REFERENCES films (film_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

create table if not exists genres
(
    genre_id int auto_increment primary key,
    name     varchar(255)
);

create table if not exists film_genres
(
    id       int auto_increment,
    film_id  int,
    genre_id int,
    FOREIGN KEY (film_id) REFERENCES films (film_id),
    FOREIGN KEY (genre_id) REFERENCES genres (genre_id)
);

create table if not exists reviews
(
    review_id   int auto_increment PRIMARY KEY,
    content     varchar(1000),
    is_positive bool,
    user_id     int,
    film_id     int,
    useful      int,
    FOREIGN KEY (film_id) REFERENCES films (film_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

create unique index if not exists reviews_user_film_idx ON reviews (user_id, film_id);

create table if not exists review_actions
(
    review_id int,
    user_id   int,
    action    varchar(7),
    PRIMARY KEY (review_id, user_id),
    CHECK (action in ('LIKE', 'DISLIKE')),
    FOREIGN KEY (review_id) REFERENCES reviews (review_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS feed(
    event_id int generated by default as identity primary KEY,
    event_timestamp timestamp,
    user_id int,
    event_type varchar(255),
    operation varchar(255),
    entity_id int,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON UPDATE RESTRICT ON DELETE CASCADE
);