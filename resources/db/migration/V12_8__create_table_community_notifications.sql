CREATE TABLE community_notifications (
  id           BIGSERIAL,
  community_id BIGINT,
  wordpress_id TEXT,
  updated_at   TIMESTAMP,
  PRIMARY KEY (id)
);