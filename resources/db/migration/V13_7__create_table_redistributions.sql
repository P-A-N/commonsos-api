CREATE TABLE redistributions (
  id             BIGSERIAL,
  community_id   BIGINT,
  is_all         BOOLEAN NOT NULL,
  user_id        BIGINT,
  rate           DECIMAL(8, 5),
  deleted        BOOLEAN NOT NULL,
  created_at     TIMESTAMP,
  updated_at     TIMESTAMP,
  PRIMARY KEY (id)
);
