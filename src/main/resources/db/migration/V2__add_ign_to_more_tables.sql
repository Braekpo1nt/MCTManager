-- add ign to more tables

-- 1. Add the column
ALTER TABLE player_metadata
ADD COLUMN ign CHAR(36);

-- 2. Populate it from all_players
UPDATE player_metadata
SET ign = (
    SELECT ign
    FROM all_players
    WHERE all_players.uuid = player_metadata.participant_uuid
);

-- 3. enforce NOT NULL afterward
CREATE TABLE player_metadata_new (
    participant_uuid    CHAR(36) PRIMARY KEY,
    
    discord_username    VARCHAR(36) NULL,

    current_tokens      INT NOT NULL DEFAULT 0,
    lifetime_tokens     INT NOT NULL DEFAULT 0,

    percent_rank        INT NOT NULL DEFAULT 0,
    ign                 CHAR(36) NOT NULL,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid) -- wallets without players should never exist
);

INSERT INTO player_metadata_new (participant_uuid, discord_username, current_tokens, lifetime_tokens, percent_rank, ign)
SELECT participant_uuid, discord_username, current_tokens, lifetime_tokens, percent_rank, ign
FROM player_metadata;

DROP TABLE player_metadata;
ALTER TABLE player_metadata_new RENAME TO player_metadata;
