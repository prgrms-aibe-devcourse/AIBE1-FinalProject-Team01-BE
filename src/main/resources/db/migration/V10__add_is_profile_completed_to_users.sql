ALTER TABLE users ADD COLUMN is_profile_completed BOOLEAN NOT NULL DEFAULT FALSE;

-- 기존 사용자들은 모두 프로필이 완성된 것으로 간주
UPDATE users SET is_profile_completed = TRUE;