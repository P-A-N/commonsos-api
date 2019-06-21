ALTER TABLE message_threads ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE message_threads
SET deleted = true
WHERE ad_id IS NOT null
AND EXISTS (
    SELECT * FROM ads
    WHERE id = message_threads.ad_id
    AND deleted = true
);