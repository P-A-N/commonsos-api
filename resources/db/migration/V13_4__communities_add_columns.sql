-- communities
ALTER TABLE communities ADD COLUMN status TEXT;
ALTER TABLE communities ADD COLUMN main_wallet TEXT;
ALTER TABLE communities ADD COLUMN main_wallet_address TEXT;
ALTER TABLE communities ADD COLUMN fee_wallet TEXT;
ALTER TABLE communities ADD COLUMN fee_wallet_address TEXT;
ALTER TABLE communities ADD COLUMN fee DECIMAL(8, 5);
ALTER TABLE communities ADD COLUMN admin_page_url TEXT;
ALTER TABLE communities ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE communities SET status = 'PUBLIC';
UPDATE communities SET fee = 0;
