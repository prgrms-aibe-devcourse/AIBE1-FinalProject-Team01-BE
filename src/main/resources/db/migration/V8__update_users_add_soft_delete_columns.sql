-- 회원 탈퇴 기능을 위한 Soft Delete 컬럼 추가

ALTER TABLE users
    ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN deleted_at TIMESTAMP NULL;