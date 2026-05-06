-- V__make_ign_case_sensitive.sql

ALTER TABLE all_players
MODIFY ign VARCHAR(36)
CHARACTER SET utf8mb4
COLLATE utf8mb4_bin
NOT NULL;
