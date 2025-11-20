-- V1__initial_table_creation.sql
CREATE TABLE IF NOT EXISTS `instant_personal_scores` ( 
        `id` INTEGER AUTO_INCREMENT , 
        `uuid` VARCHAR(255) NOT NULL , 
        `ign` VARCHAR(255) NOT NULL , 
        `teamId` VARCHAR(255) NOT NULL , 
        `gameSessionId` INTEGER NOT NULL,
        `gameType` VARCHAR(100) NOT NULL , 
        `configFile` VARCHAR(255) NOT NULL , 
        `date` DATETIME NOT NULL , 
        `mode` VARCHAR(255) NOT NULL , 
        `multiplier` DOUBLE PRECISION DEFAULT 1.0 , 
        `points` INTEGER DEFAULT 0 , 
        `description` VARCHAR(255) NOT NULL , 
        PRIMARY KEY (`id`) 
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `instant_team_scores` (
        `id` INTEGER AUTO_INCREMENT , 
        `teamId` VARCHAR(255) NOT NULL , 
        `gameSessionId` INTEGER NOT NULL,
        `gameType` VARCHAR(100) NOT NULL , 
        `configFile` VARCHAR(255) NOT NULL , 
        `date` DATETIME NOT NULL , 
        `mode` VARCHAR(255) NOT NULL , 
        `multiplier` DOUBLE PRECISION DEFAULT 1.0 , 
        `points` INTEGER DEFAULT 0 , 
        `description` VARCHAR(255) NOT NULL , 
        PRIMARY KEY (`id`) 
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `final_personal_scores` ( 
        `id` INTEGER AUTO_INCREMENT , 
        `uuid` VARCHAR(255) NOT NULL , 
        `ign` VARCHAR(255) NOT NULL , 
        `teamId` VARCHAR(255) NOT NULL , 
        `gameSessionId` INTEGER NOT NULL,
        `gameType` VARCHAR(100) NOT NULL , 
        `configFile` VARCHAR(255) NOT NULL , 
        `date` DATETIME NOT NULL , 
        `mode` VARCHAR(255) NOT NULL , 
        `multiplier` DOUBLE PRECISION DEFAULT 1.0 , 
        `points` INTEGER DEFAULT 0 , 
        PRIMARY KEY (`id`) 
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `final_team_scores` (
        `id` INTEGER AUTO_INCREMENT , 
        `teamId` VARCHAR(255) NOT NULL , 
        `gameSessionId` INTEGER NOT NULL,
        `gameType` VARCHAR(100) NOT NULL , 
        `configFile` VARCHAR(255) NOT NULL , 
        `date` DATETIME NOT NULL , 
        `mode` VARCHAR(255) NOT NULL , 
        `multiplier` DOUBLE PRECISION DEFAULT 1.0 , 
        `points` INTEGER DEFAULT 0 , 
        PRIMARY KEY (`id`) 
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `game_sessions` (
        `id` INTEGER AUTO_INCREMENT , 
        `gameType` VARCHAR(100) NOT NULL , 
        `configFile` VARCHAR(255) NOT NULL , 
        `startTime` DATETIME NOT NULL , 
        `endTime` DATETIME , 
        `mode` VARCHAR(255) NOT NULL , PRIMARY KEY (`id`) 
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS `participant_currency` (
        `uuid` VARCHAR(255) NOT NULL ,  
        `ign` VARCHAR(255) NOT NULL , 
        `current` INTEGER DEFAULT 0 , 
        `lifetime` INTEGER DEFAULT 0 , 
        PRIMARY KEY (`uuid`) 
) ENGINE=InnoDB;
