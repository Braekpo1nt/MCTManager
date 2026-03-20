-- add mode admin tables
RENAME TABLE admins TO active_admins;

CREATE TABLE maintenance_admins (
    uuid    CHAR(36) PRIMARY KEY,
    
    FOREIGN KEY (uuid) REFERENCES all_players(uuid) -- makes sure this is a real player
);

CREATE TABLE practice_admins (
    uuid    CHAR(36) PRIMARY KEY,
    
    FOREIGN KEY (uuid) REFERENCES all_players(uuid) -- makes sure this is a real player
);

CREATE TABLE event_admins (
    id          BIGINT ${autoincrement} PRIMARY KEY,
    uuid        CHAR(36) NOT NULL,
    event_id    VARCHAR(64) NOT NULL,
    
    UNIQUE (event_id, uuid),
    
    FOREIGN KEY (uuid) REFERENCES all_players(uuid), -- makes sure this is a real player
    FOREIGN KEY (event_id) REFERENCES event_info(id) -- makes sure this is a real event
);
