-- token_transactions
ALTER TABLE token_transactions ADD COLUMN remitter_admin_id BIGINT;
ALTER TABLE token_transactions RENAME COLUMN remitter_id TO remitter_user_id;
ALTER TABLE token_transactions RENAME COLUMN beneficiary_id TO beneficiary_user_id;

-- eth_transactions
ALTER TABLE eth_transactions ADD COLUMN remitter_admin_id BIGINT;
