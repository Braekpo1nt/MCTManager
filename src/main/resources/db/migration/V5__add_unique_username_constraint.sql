-- add unique username constraint

-- require ign to be unique
CREATE TABLE all_players_new (
    uuid                CHAR(36) PRIMARY KEY,
    ign                 VARCHAR(36) NOT NULL,
    first_seen_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT unique_ign UNIQUE (ign)
);

INSERT INTO all_players_new (uuid, ign, first_seen_at)
SELECT uuid, ign, first_seen_at
FROM all_players;

DROP TABLE all_players;
ALTER TABLE all_players_new RENAME TO all_players;
