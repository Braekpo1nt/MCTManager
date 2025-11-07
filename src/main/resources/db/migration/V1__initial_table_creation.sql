-- V1__initial_table_creation.sql
CREATE TABLE IF NOT EXISTS `instant_personal_scores` ( 
        `id` INTEGER AUTO_INCREMENT , 
        `uuid` VARCHAR(255) NOT NULL , 
        `teamId` VARCHAR(255) NOT NULL , 
        `gameType` VARCHAR(100) NOT NULL , 
        `configFile` VARCHAR(255) NOT NULL , 
        `date` DATETIME NOT NULL , 
        `mode` VARCHAR(255) NOT NULL , 
        `multiplier` DOUBLE PRECISION DEFAULT 1.0 , 
        `points` INTEGER DEFAULT 0 , 
        `description` VARCHAR(255) NOT NULL , 
        PRIMARY KEY (`id`) 
) ENGINE=InnoDB;
