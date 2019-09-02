CREATE TABLE eth_transactions (
  id                          BIGSERIAL,
  community_id                BIGINT,
  blockchain_transaction_hash TEXT,
  amount                      DECIMAL(19, 2),
  description                 TEXT,
  blockchain_completed_at     TIMESTAMP,
  created_at                  TIMESTAMP,
  updated_at                  TIMESTAMP,
  PRIMARY KEY (id)
);