-- V5__update_report_post_comment_entities.sql
-- Report, Post, Comment 엔티티 변경사항 반영

ALTER TABLE reports
    ADD COLUMN report_target VARCHAR(50) NOT NULL DEFAULT 'POST',
    ADD COLUMN report_type VARCHAR(50) NOT NULL DEFAULT 'BAD_WORDS',
    ADD COLUMN processing_started_at TIMESTAMP NULL,
    ADD COLUMN processing_completed_at TIMESTAMP NULL,
    ADD COLUMN is_violation BOOLEAN NULL,
    ADD COLUMN violation_reason VARCHAR(1000) NULL,
    ADD COLUMN confidence_score DOUBLE NULL;

ALTER TABLE posts
    ADD COLUMN is_blinded BOOLEAN NOT NULL DEFAULT FALSE;


ALTER TABLE comments
    ADD COLUMN is_blinded BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN reply_count INT NOT NULL DEFAULT 0;

CREATE INDEX idx_comment_post_parent_deleted_created
    ON comments(post_id, parent_comment_id, is_deleted, created_at);

CREATE INDEX idx_comment_post_parent_deleted_id
    ON comments(post_id, parent_comment_id, is_deleted, id);

CREATE INDEX idx_comment_like_user_comment
    ON post_like(comment_id, user_id);

CREATE INDEX idx_user_id_performance
    ON users(id);