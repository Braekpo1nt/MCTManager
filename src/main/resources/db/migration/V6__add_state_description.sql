-- add state description

ALTER TABLE system_state
ADD COLUMN state_description VARCHAR(128) NOT NULL DEFAULT '';
