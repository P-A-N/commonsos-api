ALTER TABLE ads ADD COLUMN status TEXT;
ALTER TABLE ads ADD COLUMN publish_status TEXT;
UPDATE ads SET publish_status = 'PUBLIC';
