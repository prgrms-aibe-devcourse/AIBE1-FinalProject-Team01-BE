CREATE TABLE follows (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         from_user_id BIGINT       NOT NULL,
                         to_user_id   BIGINT       NOT NULL,
                         created_at   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at   DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,

    -- 외래키 제약조건
                         CONSTRAINT fk_follows_from_user
                             FOREIGN KEY (from_user_id) REFERENCES users(id) ON DELETE CASCADE,
                         CONSTRAINT fk_follows_to_user
                             FOREIGN KEY (to_user_id)   REFERENCES users(id) ON DELETE CASCADE,

    -- 인덱스
                         INDEX idx_follows_from_user_id (from_user_id),
                         INDEX idx_follows_to_user_id   (to_user_id),

    -- 팔로우 중복이 발생하지 않도록 UNIQUE 설정
                         UNIQUE KEY uk_from_user_id_to_user_id (from_user_id, to_user_id)
)