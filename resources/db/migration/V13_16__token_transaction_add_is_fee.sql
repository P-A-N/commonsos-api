-- token_transactions
ALTER TABLE token_transactions ADD COLUMN is_fee_transaction BOOLEAN NOT NULL DEFAULT FALSE;
