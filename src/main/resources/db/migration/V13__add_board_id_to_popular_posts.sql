ALTER TABLE popular_posts
ADD COLUMN board_id BIGINT NOT NULL DEFAULT 0,
ADD COLUMN is_blinded BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- 성능을 위한 인덱스 추가
CREATE INDEX idx_popular_posts_board_type_id ON popular_posts(board_type, board_id);
CREATE INDEX idx_popular_posts_status ON popular_posts(is_blinded, is_deleted);