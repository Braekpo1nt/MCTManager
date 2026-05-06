-- add substitute flag to event_participants

-- 1. add the column
ALTER TABLE event_participants
ADD COLUMN substitute BOOLEAN NOT NULL DEFAULT FALSE;
