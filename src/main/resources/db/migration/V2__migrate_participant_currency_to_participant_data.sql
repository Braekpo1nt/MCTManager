-- 1. Create the new table
CREATE TABLE IF NOT EXISTS `participant_data` (
        `uuid` VARCHAR(255) NOT NULL, 
        `ign` VARCHAR(255) NOT NULL , 
        `percentRank` DOUBLE PRECISION NOT NULL , 
        `averageScore` DOUBLE PRECISION NOT NULL , 
        `totalEvents` INTEGER NOT NULL , 
        `currentTokens` INTEGER NOT NULL , 
        `lifetimeTokens` INTEGER NOT NULL , 
        PRIMARY KEY (`uuid`) 
) ENGINE=InnoDB;

-- 2. Migrate existing data
INSERT INTO `participant_data` (
    uuid,
    ign,
    percentRank,
    averageScore,
    totalEvents,
    currentTokens,
    lifetimeTokens
)
SELECT
    uuid,
    ign,
    0.0 AS percentRank,
    0.0 AS averageScore,
    0    AS totalEvents,
    `current`  AS currentTokens,
    `lifetime` AS lifetimeTokens
FROM participant_currency;
