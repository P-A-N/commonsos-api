CREATE TABLE admins (
  id             BIGSERIAL,
  email_address  TEXT NOT NULL,
  adminname      TEXT,
  password_hash  TEXT,
  community_id   BIGINT,
  role_id        BIGINT,
  tel_no         TEXT,
  department     TEXT,
  photo_url      TEXT,
  loggedin_at    TIMESTAMP,
  deleted        BOOLEAN NOT NULL,
  created_at     TIMESTAMP,
  updated_at     TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE roles (
  id             BIGSERIAL,
  rolename       TEXT NOT NULL,
  created_at     TIMESTAMP,
  updated_at     TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE temporary_admins (
  id              BIGSERIAL,
  access_id_hash  TEXT NOT NULL,
  expiration_time TIMESTAMP NOT NULL,
  invalid         BOOLEAN NOT NULL,
  email_address   TEXT NOT NULL,
  adminname       TEXT,
  password_hash   TEXT,
  community_id    BIGINT,
  role_id         BIGINT,
  tel_no          TEXT,
  department      TEXT,
  photo_url       TEXT,
  created_at      TIMESTAMP,
  updated_at      TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE temporary_admin_email_address (
  id              BIGSERIAL,
  access_id_hash  TEXT NOT NULL,
  expiration_time TIMESTAMP NOT NULL,
  invalid         BOOLEAN NOT NULL,
  admin_id        BIGINT,
  email_address   TEXT NOT NULL,
  created_at      TIMESTAMP,
  updated_at      TIMESTAMP,
  PRIMARY KEY (id)
);

-- insert roles
INSERT INTO roles (rolename, created_at, updated_at)
VALUES ('NCL運営者', now(), now()),
       ('コミュニティ管理者', now(), now()),
       ('窓口担当者', now(), now());
