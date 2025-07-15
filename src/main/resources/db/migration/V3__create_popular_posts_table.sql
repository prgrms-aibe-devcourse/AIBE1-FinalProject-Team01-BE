CREATE TABLE popular_posts (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               post_id BIGINT NOT NULL,
                               popularity_score DOUBLE NOT NULL,
                               calculated_date DATE NOT NULL,
                               board_type VARCHAR(50) NOT NULL,
                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 외래키 제약조건
                               CONSTRAINT fk_popular_posts_post_id
                                   FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,

    -- 인덱스
                               INDEX idx_popular_posts_calculated_date (calculated_date),
                               INDEX idx_popular_posts_popularity_score (popularity_score DESC),

    -- 같은 날짜에 같은 게시글이 중복 저장되지 않도록
                               UNIQUE KEY uk_popular_posts_post_date (post_id, calculated_date)
);