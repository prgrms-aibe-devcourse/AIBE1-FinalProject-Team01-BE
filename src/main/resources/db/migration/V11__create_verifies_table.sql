CREATE TABLE verifies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    ocr_score INT NOT NULL,
    layout_score INT NOT NULL,
    total_score INT NOT NULL,
    extracted_text TEXT,
    detail_message TEXT,
    image_url VARCHAR(500) NOT NULL,
    verified_at DATETIME NOT NULL,
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);