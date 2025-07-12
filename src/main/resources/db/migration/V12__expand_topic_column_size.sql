-- Topic enum 확장으로 인한 컬럼 크기 증가

ALTER TABLE user_topics MODIFY COLUMN topic VARCHAR(100) NOT NULL;