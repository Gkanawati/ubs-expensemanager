-- Add user_email column to revinfo table to track who made each revision
ALTER TABLE revinfo
ADD COLUMN user_email VARCHAR(255);
