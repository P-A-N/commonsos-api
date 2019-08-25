ALTER TABLE token_transactions ADD COLUMN fee DECIMAL(8, 5);
ALTER TABLE token_transactions ADD COLUMN is_from_admin BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE token_transactions ADD COLUMN wallet_division TEXT;
ALTER TABLE token_transactions ADD COLUMN redistributed BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE token_transactions SET redistributed = 'TRUE';
UPDATE token_transactions SET fee = 0;
