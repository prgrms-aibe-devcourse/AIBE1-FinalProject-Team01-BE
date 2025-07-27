CREATE TABLE `users` (
                         `is_deleted` bit(1) NOT NULL,
                         `is_profile_completed` bit(1) NOT NULL,
                         `created_at` datetime(6) DEFAULT NULL,
                         `deleted_at` datetime(6) DEFAULT NULL,
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `updated_at` datetime(6) DEFAULT NULL,
                         `devcourse_batch` varchar(255) DEFAULT NULL,
                         `email` varchar(255) NOT NULL,
                         `image_url` varchar(255) DEFAULT NULL,
                         `name` varchar(255) NOT NULL,
                         `nickname` varchar(255) NOT NULL,
                         `password` varchar(255) DEFAULT NULL,
                         `provider_id` varchar(255) DEFAULT NULL,
                         `devcourse_name` enum('AI_BACKEND','BACKEND','DATA_ENGINEERING','DATA_SCIENCE','FRONTEND','FULL_STACK') DEFAULT NULL,
                         `provider_type` enum('GITHUB','GOOGLE','KAKAO','LOCAL') DEFAULT NULL,
                         `role` enum('ADMIN','ANONYMOUS','GUEST','STUDENT') NOT NULL,
                         PRIMARY KEY (`id`),
                         UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`),
                         UNIQUE KEY `UK2ty1xmrrgtn89xt7kyxx6ta7h` (`nickname`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_topics` (
                               `created_at` datetime(6) DEFAULT NULL,
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `updated_at` datetime(6) DEFAULT NULL,
                               `user_id` bigint NOT NULL,
                               `topic` enum('AI','ALGORITHM','ANDROID','BACKEND','BUILD_SEC','CLOUD','DATA','DATABASE','DESIGN','DEVOPS','FRONTEND','FULLSTACK','GAME_DEV','IOS','LLM','MOBILE','SECURITY','WEB') NOT NULL,
                               PRIMARY KEY (`id`),
                               KEY `FKqu8wvgdxo8kbdf35h77yahhie` (`user_id`),
                               CONSTRAINT `FKqu8wvgdxo8kbdf35h77yahhie` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `posts` (
                         `is_blinded` bit(1) NOT NULL,
                         `is_deleted` bit(1) NOT NULL,
                         `like_count` int NOT NULL,
                         `created_at` datetime(6) DEFAULT NULL,
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `updated_at` datetime(6) DEFAULT NULL,
                         `user_id` bigint NOT NULL,
                         `tag` varchar(255) DEFAULT NULL,
                         `title` varchar(255) NOT NULL,
                         `board_type` enum('FREE','GATHER','INFO','MARKET','MATCH','NEWS','PROJECT_HUB','QNA','RETROSPECT','REVIEW') NOT NULL,
                         `content` text NOT NULL,
                         PRIMARY KEY (`id`),
                         KEY `idx_post_board_type` (`board_type`),
                         KEY `idx_post_user_id` (`user_id`),
                         KEY `idx_post_board_created` (`board_type`,`created_at`),
                         KEY `idx_post_deleted_blinded` (`is_deleted`,`is_blinded`),
                         CONSTRAINT `FK5lidm6cqbc7u4xhqpxm898qme` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `comments` (
                            `is_blinded` bit(1) NOT NULL,
                            `is_deleted` bit(1) NOT NULL,
                            `like_count` int NOT NULL,
                            `reply_count` int NOT NULL,
                            `created_at` datetime(6) DEFAULT NULL,
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `parent_comment_id` bigint DEFAULT NULL,
                            `post_id` bigint NOT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `user_id` bigint NOT NULL,
                            `content` text NOT NULL,
                            PRIMARY KEY (`id`),
                            KEY `idx_comment_post_parent_deleted` (`post_id`,`parent_comment_id`,`is_deleted`),
                            KEY `idx_comment_parent_deleted` (`parent_comment_id`,`is_deleted`),
                            KEY `idx_comment_created_at` (`created_at`),
                            KEY `idx_comment_cursor_paging` (`post_id`,`parent_comment_id`,`is_deleted`,`id`,`created_at`),
                            KEY `FK8omq0tc18jd43bu5tjh6jvraq` (`user_id`),
                            CONSTRAINT `FK8omq0tc18jd43bu5tjh6jvraq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `post_images` (
                               `created_at` datetime(6) DEFAULT NULL,
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `post_id` bigint NOT NULL,
                               `updated_at` datetime(6) DEFAULT NULL,
                               `image_url` varchar(255) NOT NULL,
                               PRIMARY KEY (`id`),
                               KEY `FKo1i5va2d8de9mwq727vxh0s05` (`post_id`),
                               CONSTRAINT `FKo1i5va2d8de9mwq727vxh0s05` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `post_like` (
                             `comment_id` bigint DEFAULT NULL,
                             `created_at` datetime(6) DEFAULT NULL,
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `post_id` bigint DEFAULT NULL,
                             `updated_at` datetime(6) DEFAULT NULL,
                             `user_id` bigint NOT NULL,
                             PRIMARY KEY (`id`),
                             KEY `idx_like_post_id` (`post_id`),
                             KEY `idx_like_user_id` (`user_id`),
                             KEY `idx_like_post_user` (`post_id`,`user_id`),
                             KEY `idx_like_comment_user` (`comment_id`,`user_id`),
                             KEY `idx_like_post_comment` (`post_id`,`comment_id`),
                             CONSTRAINT `FK6e45kleuk0275o7c5m2dag12h` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`),
                             CONSTRAINT `FKcf8kqsucxsmplv3xw9gubrql0` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
                             CONSTRAINT `FKijnjmw0imnatadr3agtk0udip` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `post_statistics` (
                                   `view_count` int NOT NULL,
                                   `post_id` bigint NOT NULL,
                                   PRIMARY KEY (`post_id`),
                                   KEY `idx_post_statistics_view_count` (`view_count`),
                                   KEY `idx_post_statistics_post_view` (`post_id`,`view_count`),
                                   CONSTRAINT `FK69n3kv4oskyufyclelj76620u` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `ai_profiles` (
                               `created_at` datetime(6) DEFAULT NULL,
                               `id` bigint NOT NULL AUTO_INCREMENT,
                               `updated_at` datetime(6) DEFAULT NULL,
                               `user_id` bigint NOT NULL,
                               `interest_keywords` varchar(1024) NOT NULL,
                               `persona_description` text NOT NULL,
                               PRIMARY KEY (`id`),
                               UNIQUE KEY `UKi8kxr78dvy71nhjnu7bf733j9` (`user_id`),
                               CONSTRAINT `FKtmd0e39tdqa4759iy6und9dfy` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `bookmarks` (
                             `created_at` datetime(6) DEFAULT NULL,
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `post_id` bigint NOT NULL,
                             `updated_at` datetime(6) DEFAULT NULL,
                             `user_id` bigint NOT NULL,
                             PRIMARY KEY (`id`),
                             KEY `idx_bookmark_post_id` (`post_id`),
                             KEY `idx_bookmark_user_id` (`user_id`),
                             KEY `idx_bookmark_post_user` (`post_id`,`user_id`),
                             KEY `idx_bookmark_user_created` (`user_id`,`created_at`),
                             CONSTRAINT `FK7nbb4ldgek7ux7y6lu0y4g826` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
                             CONSTRAINT `FKdbsho2e05w5r13fkjqfjmge5f` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `community_posts` (
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `post_id` bigint NOT NULL,
                                   `updated_at` datetime(6) DEFAULT NULL,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `UKao0ulnr3wk9o4j84k4yv6vhug` (`post_id`),
                                   CONSTRAINT `FKoh8j77t481dxkq8e6jla1pjse` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `follows` (
                           `created_at` datetime(6) DEFAULT NULL,
                           `from_user_id` bigint NOT NULL,
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `to_user_id` bigint NOT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           PRIMARY KEY (`id`),
                           KEY `FKq3p7bn58m0d429tt9y998posw` (`from_user_id`),
                           KEY `FKeu9xok1bsf64gftb3jct86v2j` (`to_user_id`),
                           CONSTRAINT `FKeu9xok1bsf64gftb3jct86v2j` FOREIGN KEY (`to_user_id`) REFERENCES `users` (`id`),
                           CONSTRAINT `FKq3p7bn58m0d429tt9y998posw` FOREIGN KEY (`from_user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `gathering_posts` (
                                   `head_count` int DEFAULT NULL,
                                   `created_at` datetime(6) DEFAULT NULL,
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `post_id` bigint NOT NULL,
                                   `updated_at` datetime(6) DEFAULT NULL,
                                   `period` varchar(255) DEFAULT NULL,
                                   `place` varchar(255) DEFAULT NULL,
                                   `schedule` varchar(255) DEFAULT NULL,
                                   `gathering_type` enum('SIDE_PROJECT','STUDY') NOT NULL,
                                   `status` enum('COMPLETED','RECRUITING') NOT NULL,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `UKkvkqbh6djrl6owj8minmacxua` (`post_id`),
                                   CONSTRAINT `FK8v8bugccpqvsl87rkxby8lfq1` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `it_posts` (
                            `created_at` datetime(6) DEFAULT NULL,
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `post_id` bigint NOT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UKif2eff302na1gvupbdaqmj2ok` (`post_id`),
                            CONSTRAINT `FK64gq1cwqracyseram4w2e0qlk` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `market_items` (
                                `price` int NOT NULL,
                                `created_at` datetime(6) DEFAULT NULL,
                                `id` bigint NOT NULL AUTO_INCREMENT,
                                `post_id` bigint NOT NULL,
                                `updated_at` datetime(6) DEFAULT NULL,
                                `place` varchar(255) DEFAULT NULL,
                                `status` enum('RESERVED','SELLING','SOLD_OUT') NOT NULL,
                                PRIMARY KEY (`id`),
                                UNIQUE KEY `UKiuo6pm4rudf1jbpx96cardm4h` (`post_id`),
                                CONSTRAINT `FKa51qd90411dbidy5c44rcq6gk` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `matching_posts` (
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `post_id` bigint NOT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  `expertise_areas` varchar(255) DEFAULT NULL,
                                  `matching_type` enum('COFFEE_CHAT','MENTORING') NOT NULL,
                                  `status` enum('MATCHED','OPEN') NOT NULL,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `UK3nuuo3v0mq01kmy80rc7yq678` (`post_id`),
                                  CONSTRAINT `FKk6p8ih339gy1wo68vp1t8jnoa` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `password_reset_tokens` (
                                         `id` bigint NOT NULL AUTO_INCREMENT,
                                         `token` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                                         `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
                                         `expires_at` timestamp NOT NULL,
                                         `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                         `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         PRIMARY KEY (`id`),
                                         UNIQUE KEY `token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `popular_posts` (
                                 `calculated_date` date NOT NULL,
                                 `popularity_score` double NOT NULL,
                                 `board_id` bigint NOT NULL,
                                 `created_at` datetime(6) DEFAULT NULL,
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `post_id` bigint NOT NULL,
                                 `updated_at` datetime(6) DEFAULT NULL,
                                 `board_type` enum('FREE','GATHER','INFO','MARKET','MATCH','NEWS','PROJECT_HUB','QNA','RETROSPECT','REVIEW') NOT NULL,
                                 PRIMARY KEY (`id`),
                                 KEY `FK1ai70alvj1bvaaeceogk8qd38` (`post_id`),
                                 CONSTRAINT `FK1ai70alvj1bvaaeceogk8qd38` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `projects` (
                            `created_at` datetime(6) DEFAULT NULL,
                            `ended_at` datetime(6) NOT NULL,
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `post_id` bigint NOT NULL,
                            `started_at` datetime(6) NOT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `demo_url` varchar(255) DEFAULT NULL,
                            `github_url` varchar(255) NOT NULL,
                            `simple_content` varchar(255) DEFAULT NULL,
                            `project_members` json DEFAULT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UK37gxqamt3xdbjc9sc80g0wyf3` (`post_id`),
                            CONSTRAINT `FK4dpbqd7od0bnaneteh5fgiapf` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `recommended_posts` (
                                     `board_id` bigint NOT NULL,
                                     `created_at` datetime(6) DEFAULT NULL,
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `post_id` bigint NOT NULL,
                                     `updated_at` datetime(6) DEFAULT NULL,
                                     `user_id` bigint NOT NULL,
                                     `board_type` enum('FREE','GATHER','INFO','MARKET','MATCH','NEWS','PROJECT_HUB','QNA','RETROSPECT','REVIEW') NOT NULL,
                                     PRIMARY KEY (`id`),
                                     KEY `FKqds38i7kvul7myypf29tblwqp` (`post_id`),
                                     KEY `FKs9jpsthudmb66sk2r6ex7f9s9` (`user_id`),
                                     CONSTRAINT `FKqds38i7kvul7myypf29tblwqp` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`),
                                     CONSTRAINT `FKs9jpsthudmb66sk2r6ex7f9s9` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reports` (
                           `confidence_score` double DEFAULT NULL,
                           `is_violation` bit(1) DEFAULT NULL,
                           `comment_id` bigint DEFAULT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `post_id` bigint DEFAULT NULL,
                           `processing_completed_at` datetime(6) DEFAULT NULL,
                           `processing_started_at` datetime(6) DEFAULT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `user_id` bigint NOT NULL,
                           `violation_reason` varchar(1000) DEFAULT NULL,
                           `description` varchar(255) NOT NULL,
                           `report_target` enum('COMMENT','POST') NOT NULL,
                           `report_type` enum('BAD_WORDS','FLOODING','OTHER','PERSONAL_INFO','SEXUAL_CONTENT','SPAM') NOT NULL,
                           `status` enum('ERROR','MANUAL_REVIEW','PENDING','PROCESSING','REJECTED','RESOLVED') NOT NULL,
                           PRIMARY KEY (`id`),
                           KEY `FK3x8ylsypiesh2gkwdy5ug7qe7` (`comment_id`),
                           KEY `FKneu1viyp671jjiwukyfv6dsy` (`post_id`),
                           KEY `FK2o32rer9hfweeylg7x8ut8rj2` (`user_id`),
                           CONSTRAINT `FK2o32rer9hfweeylg7x8ut8rj2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
                           CONSTRAINT `FK3x8ylsypiesh2gkwdy5ug7qe7` FOREIGN KEY (`comment_id`) REFERENCES `comments` (`id`),
                           CONSTRAINT `FKneu1viyp671jjiwukyfv6dsy` FOREIGN KEY (`post_id`) REFERENCES `posts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tokens` (
                          `created_at` datetime(6) DEFAULT NULL,
                          `expired_at` datetime(6) NOT NULL,
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `updated_at` datetime(6) DEFAULT NULL,
                          `user_id` bigint NOT NULL,
                          `token` varchar(255) NOT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `UKna3v9f8s7ucnj16tylrs822qj` (`token`),
                          KEY `FK2dylsfo39lgjyqml2tbe0b0ss` (`user_id`),
                          CONSTRAINT `FK2dylsfo39lgjyqml2tbe0b0ss` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `verifies` (
                            `layout_score` int NOT NULL,
                            `ocr_score` int NOT NULL,
                            `total_score` int NOT NULL,
                            `completed_at` datetime(6) DEFAULT NULL,
                            `created_at` datetime(6) DEFAULT NULL,
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `user_id` bigint NOT NULL,
                            `verified_at` datetime(6) NOT NULL,
                            `image_url` varchar(500) NOT NULL,
                            `detail_message` text,
                            `extracted_text` text,
                            `status` enum('COMPLETED','FAILED','PENDING','PROCESSING') NOT NULL,
                            PRIMARY KEY (`id`),
                            KEY `FK5ijwsvv6tjkmmdwen62kufo4y` (`user_id`),
                            CONSTRAINT `FK5ijwsvv6tjkmmdwen62kufo4y` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

