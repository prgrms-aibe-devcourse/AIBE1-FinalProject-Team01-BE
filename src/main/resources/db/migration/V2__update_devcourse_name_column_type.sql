-- V2__update_devcourse_name_column_type.sql
-- devcourse_name 컬럼 타입을 ENUM으로 변경

-- 기존 tinyint 컬럼을 ENUM 타입으로 변경
ALTER TABLE users MODIFY COLUMN devcourse_name VARCHAR(50) NOT NULL;