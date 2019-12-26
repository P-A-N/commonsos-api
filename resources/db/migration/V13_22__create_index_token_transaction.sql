CREATE INDEX index_for_sum_token_transferred ON token_transactions (community_id, remitter_user_id, blockchain_completed_at);
CREATE INDEX index_for_sum_token_received ON token_transactions (community_id, beneficiary_user_id, blockchain_completed_at);
