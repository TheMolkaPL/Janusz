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

CREATE TABLE IF NOT EXISTS `janusz_motds` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                           `from` TIMESTAMP NULL DEFAULT NULL,
                                           `to` TIMESTAMP NULL DEFAULT NULL,
                                           `text_primary` VARCHAR(255) NOT NULL,
                                           `text_secondary` VARCHAR(255) NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `janusz_profiles` (`id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT,
                                              `uuid` CHAR(36) NOT NULL UNIQUE,
                                              `offline_uuid` CHAR(36) NOT NULL UNIQUE,
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

ALTER TABLE `janusz_sessions` ADD FOREIGN KEY (`season_id`) REFERENCES `janusz_seasons`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE `janusz_sessions` ADD FOREIGN KEY (`profile_id`) REFERENCES `janusz_profiles`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT;

-- Insert the first season, so the plugin can actually run for the first time.
INSERT INTO `janusz_seasons` (`from`, `to`) VALUES (CURRENT_TIMESTAMP, NULL);
