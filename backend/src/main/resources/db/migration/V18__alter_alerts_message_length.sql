-- Increase message field length to support concatenated budget alerts
ALTER TABLE alerts ALTER COLUMN message TYPE VARCHAR(2000);
