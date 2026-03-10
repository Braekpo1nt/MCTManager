-- add ign to more tables

-- 1. Add the column
ALTER TABLE player_metadata
ADD COLUMN ign CHAR(36);

-- 2. Populate it from all_players
UPDATE player_metadata pm
JOIN all_players ap
  ON pm.participant_uuid = ap.uuid
SET pm.ign = ap.ign;

-- 3. enforce NOT NULL afterward
ALTER TABLE player_metadata
MODIFY ign CHAR(36) NOT NULL;
