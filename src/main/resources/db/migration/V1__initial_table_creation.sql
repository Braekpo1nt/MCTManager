-- initial table creation

-- Global identity only. Never store scores here.
CREATE TABLE all_players (
    uuid                CHAR(36) PRIMARY KEY,
    ign                 VARCHAR(36) NOT NULL,
    first_seen_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A tournament/day (e.g. "MCT 1B")
CREATE TABLE event_info (
    id                      VARCHAR(64) PRIMARY KEY,  -- user chosen

    plain_display_name      VARCHAR(128) NOT NULL,
    component_display_name  TEXT NOT NULL,

    event_date              DATE NOT NULL,            -- scheduled day

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    started_at              TIMESTAMP NULL,           -- actual runtime
    ended_at                TIMESTAMP NULL,

    winner_team_id          VARCHAR(64) NULL,
    
    -- tells website when the standings have changed, reduces polling traffic
    -- Incremented when the event_*_standings tables are updated
    standings_version       INT NOT NULL DEFAULT 0 
);

-- Holds a single row to tell all clients what the active event_id should be
CREATE TABLE system_state (
    id                  INT PRIMARY KEY CHECK (id = 1),
    active_event_id     VARCHAR(64) NULL, -- the active event_id, or null if there is no active event
    
    FOREIGN KEY (active_event_id) REFERENCES event_info(id)
);

-- the only entry in system_state, updated when there is an active event
INSERT INTO system_state(id, active_event_id) VALUES (1, NULL);

-- ==============
-- Teams
-- ==============
-- The teams which are used in maintenance mode
CREATE TABLE maintenance_teams (
    team_id         VARCHAR(64) PRIMARY KEY,
    display_name    VARCHAR(64) NOT NULL,
    color           VARCHAR(32) NOT NULL,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- The teams which are used in practice mode
CREATE TABLE practice_teams (
    team_id         VARCHAR(64) PRIMARY KEY,
    display_name    VARCHAR(64) NOT NULL,
    color           VARCHAR(32) NOT NULL,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- The teams which are in an event (each team is associated with a specific event)
CREATE TABLE event_teams (
    id              BIGINT ${autoincrement} PRIMARY KEY,
    event_id        VARCHAR(64) NOT NULL,
    team_id         VARCHAR(64) NOT NULL,
    display_name    VARCHAR(64) NOT NULL,
    color           VARCHAR(32) NOT NULL,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE (event_id, team_id),
    
    FOREIGN KEY (event_id) REFERENCES event_info(id) -- makes sure this is a real event
);

-- ================
-- Participants
-- ================
-- Stores the participants in maintenance mode
CREATE TABLE maintenance_participants (
    participant_uuid    CHAR(36) PRIMARY KEY,
    team_id             VARCHAR(64) NOT NULL,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid), -- makes sure this is a real player
    FOREIGN KEY (team_id) REFERENCES maintenance_teams(team_id) -- make sure this is a real team
);

-- Stores the participants in practice mode
CREATE TABLE practice_participants (
    participant_uuid    CHAR(36) PRIMARY KEY,
    team_id             VARCHAR(64) NOT NULL,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid), -- makes sure this is a real player
    FOREIGN KEY (team_id) REFERENCES practice_teams(team_id) -- make sure this is a real team
);

-- Roster membership, who is in each event and on what team
CREATE TABLE event_participants (
    id                  BIGINT ${autoincrement} PRIMARY KEY,
    event_id            VARCHAR(64) NOT NULL,
    participant_uuid    CHAR(36) NOT NULL,
    team_id             VARCHAR(64) NOT NULL,

    UNIQUE (event_id, participant_uuid),
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid), -- makes sure this is a real player
    FOREIGN KEY (event_id) REFERENCES event_info(id), -- makes sure this is a real event
    FOREIGN KEY (event_id, team_id)
        REFERENCES event_teams(event_id, team_id) -- make sure event_id + team_id references an actual entry
);

-- holds a specific session of a specific game
-- normalized to reference event_id
CREATE TABLE game_sessions (
    id              BIGINT ${autoincrement} PRIMARY KEY,
    event_id        VARCHAR(64) NULL,
    game_type       VARCHAR(64) NOT NULL,
    config_file     VARCHAR(128) NOT NULL,
    mode            VARCHAR(32) NOT NULL,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NULL,
    
    FOREIGN KEY (event_id) REFERENCES event_info(id) -- makes sure this is a real event
);

-- stores a running list of changes to the scores, a journal used to rebuild the current standings on a restart
CREATE TABLE score_events (
    id                  BIGINT ${autoincrement} PRIMARY KEY,

    source_type         VARCHAR(16) NOT NULL
    
    -- the id of the game session this score event took place during, 
    -- or null if not from a game
    session_id          BIGINT NULL, 
    event_id            VARCHAR(64) NULL,
    -- event_id when the score_event is tied to an event
    -- NULL otherwise (future ability to add a practice session or test session id)
    
    mode                VARCHAR(32) NOT NULL,
    
    participant_uuid    CHAR(36) NULL,
    team_id             VARCHAR(64) NOT NULL,

    points_base         INT NOT NULL,
    multiplier          DECIMAL(6,3) NOT NULL DEFAULT 1.0,

    reason              VARCHAR(128) NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid),
    FOREIGN KEY (session_id) REFERENCES game_sessions(id)
);

-- =============
-- Projections
-- =============
-- Projections are for the website and maybe the tablist/sidebar 
-- to read from without needing to calculate the sum of all the
-- score_event entries for a specific event. They are used for live events
-- right now.
-- These are transient, erased and rebuilt upon a restart

-- The current score of each team for a given event
CREATE TABLE event_team_standings (
    id          BIGINT ${autoincrement} PRIMARY KEY,
    event_id    VARCHAR(64) NOT NULL,
    team_id     VARCHAR(64) NOT NULL,

    score       INT NOT NULL DEFAULT 0,

    UNIQUE (event_id, team_id)
);

-- The current score of each participant for a given event
CREATE TABLE event_participant_standings (
    id                  BIGINT ${autoincrement} PRIMARY KEY,
    event_id            VARCHAR(64) NOT NULL,
    participant_uuid    CHAR(36) NOT NULL,

    score               INT NOT NULL DEFAULT 0,   -- personal score (no multiplier)

    UNIQUE (event_id, participant_uuid)
);

-- ===============
-- Indexes
-- ===============

-- (These make rebuilds faster)
CREATE INDEX idx_score_event
ON score_events(event_id);

CREATE INDEX idx_score_event_time
ON score_events(event_id, created_at);

CREATE INDEX idx_score_participant
ON score_events(participant_uuid);

CREATE INDEX idx_score_session
ON score_events(session_id);

-- (These make website queries faster)
CREATE INDEX idx_event_teams_event
ON event_teams(event_id);

CREATE INDEX idx_event_participants_event_team
ON event_participants(event_id, team_id);

CREATE INDEX idx_event_participant_standings_event
ON event_participant_standings(event_id);

CREATE INDEX idx_event_team_standings_event
ON event_team_standings(event_id);

CREATE INDEX idx_event_info_version
ON event_info(id, standings_version);

-- ==========
-- Authoritative, not derivable from previous data
-- ==========

-- Metadata about all players, regardless of membership or participant status
CREATE TABLE player_metadata (
    participant_uuid    CHAR(36) PRIMARY KEY,
    
    discord_username    VARCHAR(36) NULL,

    current_tokens      INT NOT NULL DEFAULT 0,
    lifetime_tokens     INT NOT NULL DEFAULT 0,

    percent_rank        INT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid) -- wallets without players should never exist
);
