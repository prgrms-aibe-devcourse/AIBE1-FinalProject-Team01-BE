ALTER TABLE popular_posts
ADD COLUMN board_id BIGINT NOT NULL DEFAULT 0;

-- 성능을 위한 인덱스 추가
CREATE INDEX idx_popular_posts_board_type_id ON popular_posts(board_type, board_id);