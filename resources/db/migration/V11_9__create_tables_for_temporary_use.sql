CREATE TABLE temporary_users (
  id                   BIGSERIAL NOT NULL,
  access_id_hash       VARCHAR NOT NULL,
  expiration_time      TIMESTAMP NOT NULL,
  invalid              BOOLEAN NOT NULL,
  description          TEXT,
  first_name           TEXT,
  last_name            TEXT,
  location             TEXT,
  password_hash        TEXT,
  username             TEXT NOT NULL,
  email_address        TEXT,
  wait_until_completed BOOLEAN,
  PRIMARY KEY (id)
);

CREATE TABLE temporary_community_users (
  id                   BIGSERIAL NOT NULL,
  access_id_hash       VARCHAR NOT NULL,
  community_id         BIGINT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE temporary_email_address (
  id                   BIGSERIAL NOT NULL,
  access_id_hash       VARCHAR NOT NULL,
  expiration_time      TIMESTAMP NOT NULL,
  invalid              BOOLEAN NOT NULL,
  user_id              BIGINT NOT NULL,
  email_address        TEXT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE password_reset_request (
  id                   BIGSERIAL NOT NULL,
  access_id_hash       VARCHAR NOT NULL,
  expiration_time      TIMESTAMP NOT NULL,
  invalid              BOOLEAN NOT NULL,
  user_id              BIGINT NOT NULL,
  PRIMARY KEY (id)
);
