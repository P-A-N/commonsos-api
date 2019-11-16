CREATE TABLE eth_balance_history (
  id                          BIGSERIAL,
  community_id                BIGINT,
  base_date                   DATE,
  eth_balance                 DECIMAL(19, 2),
  created_by                  TEXT,
  updated_by                  TEXT,
  created_at                  TIMESTAMP,
  updated_at                  TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE INDEX ON eth_balance_history (community_id, base_date);
