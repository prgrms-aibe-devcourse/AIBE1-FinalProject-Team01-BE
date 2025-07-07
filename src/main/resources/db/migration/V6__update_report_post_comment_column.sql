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
    ADD COLUMN is_blinded BOOLEAN NOT NULL DEFAULT FALSE;