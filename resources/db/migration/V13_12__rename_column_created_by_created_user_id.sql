ALTER TABLE ads RENAME COLUMN created_by TO created_user_id;
ALTER TABLE messages RENAME COLUMN created_by TO created_user_id;
ALTER TABLE message_threads RENAME COLUMN created_by TO created_user_id;
