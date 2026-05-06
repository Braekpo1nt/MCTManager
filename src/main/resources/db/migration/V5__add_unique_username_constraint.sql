-- add unique username constraint

-- require ign to be unique
ALTER TABLE all_players
ADD CONSTRAINT unique_ign UNIQUE (ign);
