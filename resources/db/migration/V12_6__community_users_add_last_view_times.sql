ALTER TABLE community_users ADD COLUMN wallet_last_view_time TIMESTAMP DEFAULT '1970-01-01 00:00:00';
ALTER TABLE community_users ADD COLUMN ad_last_view_time TIMESTAMP DEFAULT '1970-01-01 00:00:00';
ALTER TABLE community_users ADD COLUMN notification_last_view_time TIMESTAMP DEFAULT '1970-01-01 00:00:00';
