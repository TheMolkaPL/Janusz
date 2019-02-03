CREATE TABLE IF NOT EXISTS `janusz_chats` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                           `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           `season_id` BIGINT(20) NOT NULL,
                                           `profile_id` BIGINT(20) NOT NULL,
                                           `session_id` BIGINT(20) NOT NULL,
                                           `world` VARCHAR(64) NOT NULL,
                                           `text` TEXT NOT NULL,
                                           `sent` TINYINT(1) NOT NULL DEFAULT '1',
                                           `recipient_count` MEDIUMINT(9) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_clans` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                           `season_id` BIGINT(20) NOT NULL,
                                           `team` VARCHAR(16) NOT NULL,
                                           `title` VARCHAR(32) NOT NULL,
                                           `color` VARCHAR(32) NOT NULL,
                                           `world` VARCHAR(64) NOT NULL,
                                           `home_x` DOUBLE NOT NULL,
                                           `home_y` DOUBLE NOT NULL,
                                           `home_z` DOUBLE NOT NULL,
                                           `home_yaw` FLOAT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_clan_members` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                                  `season_id` BIGINT(20) NOT NULL,
                                                  `clan_id` BIGINT(20) NOT NULL,
                                                  `member_profile_id` BIGINT(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_deaths` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                            `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            `season_id` BIGINT(20) NOT NULL,
                                            `unfair` TINYINT(1) NOT NULL DEFAULT '0',
                                            `victim_profile_id` BIGINT(20) NOT NULL,
                                            `victim_session_id` BIGINT(20) NOT NULL,
                                            `world` VARCHAR(64) NOT NULL,
                                            `x` DOUBLE NOT NULL,
                                            `y` DOUBLE NOT NULL,
                                            `z` DOUBLE NOT NULL,
                                            `cause` VARCHAR(32) NULL DEFAULT NULL,
                                            `fall_distance` FLOAT NOT NULL DEFAULT '0',
                                            `killer` VARCHAR(32) NULL DEFAULT NULL,
                                            `killer_profile_id` BIGINT(20) NULL DEFAULT NULL,
                                            `killer_session_id` BIGINT(20) NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_match_results` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                                   `season_id` BIGINT(20) NOT NULL,
                                                   `arena` VARCHAR(32) NOT NULL,
                                                   `winner_profile_id` BIGINT(20) NOT NULL,
                                                   `winner_session_id` BIGINT(20) NOT NULL,
                                                   `began_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                   `duration` TIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_match_result_losers` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                                         `season_id` BIGINT(20) NOT NULL,
                                                         `match_result_id` BIGINT(20) NOT NULL,
                                                         `loser_profile_id` BIGINT(20) NOT NULL,
                                                         `loser_session_id` BIGINT(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_motds` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                           `from` TIMESTAMP NULL DEFAULT NULL,
                                           `to` TIMESTAMP NULL DEFAULT NULL,
                                           `text_primary` VARCHAR(255) NOT NULL,
                                           `text_secondary` VARCHAR(255) NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_profiles` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                              `uuid` CHAR(36) NOT NULL UNIQUE,
                                              `sex` VARCHAR(16) NOT NULL DEFAULT 'unisex'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_seasons` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                             `from` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             `to` TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_sessions` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                              `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              `destroyed_at` TIMESTAMP NULL DEFAULT NULL,
                                              `season_id` BIGINT(20) NOT NULL,
                                              `profile_id` BIGINT(20) NOT NULL,
                                              `username` VARCHAR(16) NOT NULL,
                                              `username_lower` VARCHAR(16) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE `janusz_chats` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_chats` ADD FOREIGN KEY (`profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_chats` ADD FOREIGN KEY (`session_id`) REFERENCES `janusz_sessions`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `janusz_clans` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `janusz_clan_members` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_clan_members` ADD FOREIGN KEY (`clan_id`) REFERENCES `janusz_clans`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_clan_members` ADD FOREIGN KEY (`member_profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `janusz_deaths` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_deaths` ADD FOREIGN KEY (`victim_profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_deaths` ADD FOREIGN KEY (`victim_session_id`) REFERENCES `janusz_sessions`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_deaths` ADD FOREIGN KEY (`killer_profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_deaths` ADD FOREIGN KEY (`killer_session_id`) REFERENCES `janusz_sessions`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `janusz_match_results` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_match_results` ADD FOREIGN KEY (`winner_profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_match_results` ADD FOREIGN KEY (`winner_session_id`) REFERENCES `janusz_sessions`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `janusz_match_result_losers` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_match_result_losers` ADD FOREIGN KEY (`match_result_id`) REFERENCES `janusz_match_results`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_match_result_losers` ADD FOREIGN KEY (`loser_profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_match_result_losers` ADD FOREIGN KEY (`loser_session_id`) REFERENCES `janusz_sessions`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE `janusz_sessions` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_sessions` ADD FOREIGN KEY (`profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Insert the first season, so the plugin can actually run for the first time.
INSERT INTO `janusz_seasons` (`from`, `to`) VALUES (CURRENT_TIMESTAMP, NULL);
