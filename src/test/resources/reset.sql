-- reset.sql
SET REFERENTIAL_INTEGRITY FALSE;

-- 모든 테이블 데이터 삭제 (테이블이 없어도 에러 안남)
DELETE FROM post_like;
DELETE FROM post_images;
DELETE FROM user_topics;
DELETE FROM bookmarks;
DELETE FROM comments;
DELETE FROM reports;
DELETE FROM recommended_posts;
DELETE FROM community_posts;
DELETE FROM gathering_posts;
DELETE FROM it_posts;
DELETE FROM market_items;
DELETE FROM matching_posts;
DELETE FROM posts;
DELETE FROM projects;
DELETE FROM ai_profiles;
DELETE FROM tokens;
DELETE FROM verifications;
DELETE FROM users;

SET REFERENTIAL_INTEGRITY TRUE;