-- Add modified_by column to revinfo table to track who made each revision
ALTER TABLE revinfo
ADD COLUMN modified_by VARCHAR(255);
