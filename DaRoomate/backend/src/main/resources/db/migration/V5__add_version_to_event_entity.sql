-- Add version column to event_entity table for optimistic locking
ALTER TABLE event_entity ADD COLUMN version BIGINT DEFAULT 0; 