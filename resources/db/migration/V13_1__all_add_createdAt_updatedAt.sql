-- users
ALTER TABLE users ADD COLUMN created_at TIMESTAMP;
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP;
UPDATE users SET created_at = now() WHERE created_at IS NULL;
UPDATE users SET updated_at = now() WHERE updated_at IS NULL;

-- ads
ALTER TABLE ads ADD COLUMN updated_at TIMESTAMP;
UPDATE ads SET updated_at = now() WHERE updated_at IS NULL;

-- messages
ALTER TABLE messages ADD COLUMN updated_at TIMESTAMP;
UPDATE messages SET updated_at = now() WHERE updated_at IS NULL;

-- message_threads
ALTER TABLE message_threads ALTER COLUMN created_at DROP DEFAULT;
ALTER TABLE message_threads ADD COLUMN updated_at TIMESTAMP;
UPDATE message_threads SET updated_at = now() WHERE updated_at IS NULL;

-- message_thread_parties
ALTER TABLE message_thread_parties ADD COLUMN created_at TIMESTAMP;
ALTER TABLE message_thread_parties ADD COLUMN updated_at TIMESTAMP;
UPDATE message_thread_parties SET created_at = now() WHERE created_at IS NULL;
UPDATE message_thread_parties SET updated_at = now() WHERE updated_at IS NULL;

-- transactions
ALTER TABLE transactions ADD COLUMN updated_at TIMESTAMP;
UPDATE transactions SET updated_at = now() WHERE updated_at IS NULL;

-- communities
ALTER TABLE communities ADD COLUMN created_at TIMESTAMP;
ALTER TABLE communities ADD COLUMN updated_at TIMESTAMP;
UPDATE communities SET created_at = now() WHERE created_at IS NULL;
UPDATE communities SET updated_at = now() WHERE updated_at IS NULL;

-- community_users
ALTER TABLE community_users ADD COLUMN created_at TIMESTAMP;
ALTER TABLE community_users ADD COLUMN updated_at TIMESTAMP;
UPDATE community_users SET created_at = now() WHERE created_at IS NULL;
UPDATE community_users SET updated_at = now() WHERE updated_at IS NULL;

-- community_notifications
ALTER TABLE community_notifications RENAME COLUMN updated_at TO updated_notification_at;
ALTER TABLE community_notifications ADD COLUMN created_at TIMESTAMP;
ALTER TABLE community_notifications ADD COLUMN updated_at TIMESTAMP;
UPDATE community_notifications SET created_at = now() WHERE created_at IS NULL;
UPDATE community_notifications SET updated_at = now() WHERE updated_at IS NULL;

-- temporary_users
ALTER TABLE temporary_users ADD COLUMN created_at TIMESTAMP;
ALTER TABLE temporary_users ADD COLUMN updated_at TIMESTAMP;
UPDATE temporary_users SET created_at = now() WHERE created_at IS NULL;
UPDATE temporary_users SET updated_at = now() WHERE updated_at IS NULL;

-- temporary_community_users
ALTER TABLE temporary_community_users ADD COLUMN created_at TIMESTAMP;
ALTER TABLE temporary_community_users ADD COLUMN updated_at TIMESTAMP;
UPDATE temporary_community_users SET created_at = now() WHERE created_at IS NULL;
UPDATE temporary_community_users SET updated_at = now() WHERE updated_at IS NULL;

-- temporary_email_address
ALTER TABLE temporary_email_address ADD COLUMN created_at TIMESTAMP;
ALTER TABLE temporary_email_address ADD COLUMN updated_at TIMESTAMP;
UPDATE temporary_email_address SET created_at = now() WHERE created_at IS NULL;
UPDATE temporary_email_address SET updated_at = now() WHERE updated_at IS NULL;

-- password_reset_request
ALTER TABLE password_reset_request ADD COLUMN created_at TIMESTAMP;
ALTER TABLE password_reset_request ADD COLUMN updated_at TIMESTAMP;
UPDATE password_reset_request SET created_at = now() WHERE created_at IS NULL;
UPDATE password_reset_request SET updated_at = now() WHERE updated_at IS NULL;
