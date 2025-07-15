-- 비밀번호 재설정 토큰 테이블 생성

CREATE TABLE password_reset_tokens (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       token VARCHAR(255) NOT NULL UNIQUE,
                                       email VARCHAR(255) NOT NULL,
                                       expires_at TIMESTAMP NOT NULL,
                                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                       INDEX idx_token (token),
                                       INDEX idx_email (email),
                                       INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;