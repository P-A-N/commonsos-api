-- users
ALTER TABLE users ADD COLUMN created_by TEXT;
ALTER TABLE users ADD COLUMN updated_by TEXT;
UPDATE users SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- ads
ALTER TABLE ads ADD COLUMN created_by TEXT;
ALTER TABLE ads ADD COLUMN updated_by TEXT;
UPDATE ads SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- messages
ALTER TABLE messages ADD COLUMN created_by TEXT;
ALTER TABLE messages ADD COLUMN updated_by TEXT;
UPDATE messages SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- message_threads
ALTER TABLE message_threads ADD COLUMN created_by TEXT;
ALTER TABLE message_threads ADD COLUMN updated_by TEXT;
UPDATE message_threads SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- message_thread_parties
ALTER TABLE message_thread_parties ADD COLUMN created_by TEXT;
ALTER TABLE message_thread_parties ADD COLUMN updated_by TEXT;
UPDATE message_thread_parties SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- token_transactions
ALTER TABLE token_transactions ADD COLUMN created_by TEXT;
ALTER TABLE token_transactions ADD COLUMN updated_by TEXT;
UPDATE token_transactions SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- eth_transactions
ALTER TABLE eth_transactions ADD COLUMN created_by TEXT;
ALTER TABLE eth_transactions ADD COLUMN updated_by TEXT;
UPDATE eth_transactions SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- communities
ALTER TABLE communities ADD COLUMN created_by TEXT;
ALTER TABLE communities ADD COLUMN updated_by TEXT;
UPDATE communities SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- community_users
ALTER TABLE community_users ADD COLUMN created_by TEXT;
ALTER TABLE community_users ADD COLUMN updated_by TEXT;
UPDATE community_users set created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- community_notifications
ALTER TABLE community_notifications ADD COLUMN created_by TEXT;
ALTER TABLE community_notifications ADD COLUMN updated_by TEXT;
UPDATE community_notifications SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- redistributions
ALTER TABLE redistributions ADD COLUMN created_by TEXT;
ALTER TABLE redistributions ADD COLUMN updated_by TEXT;
UPDATE redistributions SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- temporary_users
ALTER TABLE temporary_users ADD COLUMN created_by TEXT;
ALTER TABLE temporary_users ADD COLUMN updated_by TEXT;
UPDATE temporary_users SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- temporary_community_users
ALTER TABLE temporary_community_users ADD COLUMN created_by TEXT;
ALTER TABLE temporary_community_users ADD COLUMN updated_by TEXT;
UPDATE temporary_community_users SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- temporary_email_address
ALTER TABLE temporary_email_address ADD COLUMN created_by TEXT;
ALTER TABLE temporary_email_address ADD COLUMN updated_by TEXT;
UPDATE temporary_email_address SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- password_reset_request
ALTER TABLE password_reset_request ADD COLUMN created_by TEXT;
ALTER TABLE password_reset_request ADD COLUMN updated_by TEXT;
UPDATE password_reset_request SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- admins
ALTER TABLE admins ADD COLUMN created_by TEXT;
ALTER TABLE admins ADD COLUMN updated_by TEXT;
UPDATE admins SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- roles
ALTER TABLE roles ADD COLUMN created_by TEXT;
ALTER TABLE roles ADD COLUMN updated_by TEXT;
UPDATE roles SET created_by = 'SYSTEM', updated_by = 'SYSTEM';

-- temporary_admins
ALTER TABLE temporary_admins ADD COLUMN created_by TEXT;
ALTER TABLE temporary_admins ADD COLUMN updated_by TEXT;
UPDATE temporary_admins SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';

-- temporary_admin_email_address
ALTER TABLE temporary_admin_email_address ADD COLUMN created_by TEXT;
ALTER TABLE temporary_admin_email_address ADD COLUMN updated_by TEXT;
UPDATE temporary_admin_email_address SET created_by = 'UNKNOWN. Created before this column was created.', updated_by = 'UNKNOWN. There is no clue whether this column was updated or not.';
