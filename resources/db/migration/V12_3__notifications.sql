CREATE TABLE notifications (
  id             BIGSERIAL,
  community_id   BIGINT NOT NULL,
  title          TEXT,
  url            TEXT,
  created_by     BIGINT,
  created_at     TIMESTAMP,
  deleted        BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (id)
);
