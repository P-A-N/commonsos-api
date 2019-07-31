-- users
ALTER TABLE users ADD COLUMN tel_no TEXT;
ALTER TABLE users ADD COLUMN loggedin_at TIMESTAMP;

-- temporary_users
ALTER TABLE temporary_users ADD COLUMN tel_no TEXT;

