-- V7__add_project_performance_indexes.sql
-- ProjectHub 관련 성능 최적화를 위한 인덱스 추가

CREATE INDEX idx_users_course_batch ON users(devcourse_name, devcourse_batch);

CREATE INDEX idx_posts_created_at_desc ON posts(created_at DESC);
CREATE INDEX idx_posts_like_count_desc ON posts(like_count DESC);
CREATE INDEX idx_posts_view_count_desc ON posts(view_count DESC);
CREATE INDEX idx_posts_title ON posts(title);
CREATE INDEX idx_posts_content_prefix ON posts(content(100));
CREATE INDEX idx_posts_user_created ON posts(user_id, created_at DESC);
CREATE INDEX idx_projects_date_range ON projects(started_at, ended_at);
CREATE INDEX idx_posts_board_type_created ON posts(board_type, created_at DESC);
CREATE INDEX idx_bookmarks_post_id ON bookmarks(post_id);
CREATE INDEX idx_bookmarks_post_user ON bookmarks(post_id, user_id);
CREATE INDEX idx_post_like_post_user ON post_like(post_id, user_id);
CREATE INDEX idx_post_images_post_created ON post_images(post_id, created_at);