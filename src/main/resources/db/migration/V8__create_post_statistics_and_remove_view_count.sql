-- V8__create_post_statistics_and_remove_view_count.sql

-- PostStatistics 테이블 생성
CREATE TABLE post_statistics (
                                 post_id BIGINT PRIMARY KEY,
                                 view_count INT NOT NULL DEFAULT 0,
                                 FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- posts 테이블에서 view_count 컬럼 제거
ALTER TABLE posts DROP COLUMN view_count;