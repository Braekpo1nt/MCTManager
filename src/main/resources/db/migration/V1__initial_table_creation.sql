-- initial table creation

-- Global identity only. Never store scores here.
CREATE TABLE IF NOT EXISTS all_players (
    uuid                CHAR(36) PRIMARY KEY,
    ign                 VARCHAR(36) NOT NULL,
    discord_username    VARCHAR(36) NULL,
    first_seen_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- A tournament/day (e.g. "MCT 1B")
CREATE TABLE IF NOT EXISTS event_info (
    id                      VARCHAR(64) PRIMARY KEY,  -- user chosen

    plain_display_name      VARCHAR(128) NOT NULL,
    component_display_name  TEXT NOT NULL,

    event_date              DATE NOT NULL,            -- scheduled day

    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modified_at             TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    started_at              TIMESTAMP NULL,           -- actual runtime
    ended_at                TIMESTAMP NULL,

    winner_team_id          VARCHAR(64) NULL
);

-- ==============
-- Teams
-- ==============
-- The teams which are used in maintenance mode
CREATE TABLE IF NOT EXISTS maintenance_teams (
    team_id         VARCHAR(64) PRIMARY KEY,
    display_name    VARCHAR(64) NOT NULL,
    color           VARCHAR(32) NOT NULL,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- The teams which are used in practice mode
CREATE TABLE IF NOT EXISTS practice_teams (
    team_id         VARCHAR(64) PRIMARY KEY,
    display_name    VARCHAR(64) NOT NULL,
    color           VARCHAR(32) NOT NULL,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- The teams which are in an event (each team is associated with a specific event)
CREATE TABLE IF NOT EXISTS event_teams (
    id              BIGINT ${autoincrement} PRIMARY KEY,
    event_id        VARCHAR(64) NOT NULL,
    team_id         VARCHAR(64) NOT NULL,
    display_name    VARCHAR(64) NOT NULL,
    color           VARCHAR(32) NOT NULL,
    modified_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE (event_id, team_id)
);

-- ================
-- Participants
-- ================
-- Stores the participants in maintenance mode
CREATE TABLE IF NOT EXISTS maintenance_participants (
    participant_uuid    CHAR(36) PRIMARY KEY,
    team_id             VARCHAR(64) NULL,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid) -- makes sure this is a real player
);

-- Stores the participants in practice mode
CREATE TABLE IF NOT EXISTS practice_participants (
    participant_uuid    CHAR(36) PRIMARY KEY,
    team_id             VARCHAR(64) NULL,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid) -- makes sure this is a real player
);

-- Roster membership, who is in each event and on what team
CREATE TABLE IF NOT EXISTS event_participants (
    id                  BIGINT ${autoincrement} PRIMARY KEY,
    event_id            VARCHAR(64) NOT NULL,
    participant_uuid    CHAR(36) NOT NULL,
    team_id             VARCHAR(64) NULL,

    UNIQUE (event_id, participant_uuid),
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid), -- makes sure this is a real player
    FOREIGN KEY (event_id) REFERENCES event_info(id) -- makes sure this is a real event
);

-- holds a specific session of a specific game
-- normalized to reference event_id
CREATE TABLE IF NOT EXISTS game_sessions (
    id              BIGINT ${autoincrement} PRIMARY KEY,
    event_id        VARCHAR(64) NOT NULL,
    game_type       VARCHAR(64) NOT NULL,
    config_file     VARCHAR(128) NOT NULL,
    mode            VARCHAR(32) NOT NULL,
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NULL,
    
    FOREIGN KEY (event_id) REFERENCES event_info(id) -- makes sure this is a real event
);

-- stores a running list of changes to the scores, a journal used to rebuild the current standings on a restart
CREATE TABLE IF NOT EXISTS score_events (
    id                  BIGINT ${autoincrement} PRIMARY KEY,

    source_type         VARCHAR(16) NOT NULL
        CHECK (source_type IN ('GAME','ADMIN','SYSTEM','MIGRATION')),
    
    session_id          BIGINT NULL, 
    event_id          VARCHAR(64) NULL,
    -- event_id when the score_event is tied to an event
    -- NULL otherwise (future ability to add a practice session or test session id)

    participant_uuid    CHAR(36) NULL,
    team_id             VARCHAR(64) NULL,

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
CREATE TABLE IF NOT EXISTS event_team_standings (
    id          BIGINT ${autoincrement} PRIMARY KEY,
    event_id    VARCHAR(64) NOT NULL,
    team_id     VARCHAR(64) NOT NULL,

    score       INT NOT NULL DEFAULT 0,

    UNIQUE (event_id, team_id)
);

-- The current score of each participant for a given event
CREATE TABLE IF NOT EXISTS event_participant_standings (
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

-- ==========
-- Authoritative, not derivable from previous data
-- ==========

-- Participant wallets
CREATE TABLE IF NOT EXISTS participant_wallets (
    participant_uuid     CHAR(36) PRIMARY KEY,

    current_tokens  INT NOT NULL DEFAULT 0,
    lifetime_tokens INT NOT NULL DEFAULT 0,

    percent_rank    INT NOT NULL DEFAULT 0,
    
    FOREIGN KEY (participant_uuid) REFERENCES all_players(uuid) -- wallets without players should never exist
);
