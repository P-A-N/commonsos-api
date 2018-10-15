-- create table
CREATE TABLE community_users (
  id             BIGSERIAL,
  community_id   BIGINT NOT NULL,
  user_id        BIGINT NOT NULL,
  PRIMARY KEY (id)
);

-- migration
INSERT INTO community_users (
  community_id,
  user_id
)
SELECT
  community_id,
  id
FROM
  users
;

-- drop column
ALTER TABLE users DROP COLUMN community_id;
