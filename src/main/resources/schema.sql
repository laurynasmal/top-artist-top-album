CREATE TABLE IF NOT EXISTS artist_ent (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            artist_type VARCHAR(255),
                            artist_name VARCHAR(255),
                            artist_link_url VARCHAR(255),
                            artist_id INT UNIQUE,
                            amg_artist_id INT,
                            primary_genre_name VARCHAR(255),
                            primary_genre_id INT,
                            last_update_time TIMESTAMP,
                            itunes_top5_albums_resp CLOB
);

CREATE TABLE IF NOT EXISTS users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       user_id VARCHAR(255) UNIQUE NOT NULL,
                       artist_id BIGINT,
                       FOREIGN KEY (artist_id) REFERENCES artist_ent(id)
);