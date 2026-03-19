CREATE TABLE transactions (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    source_wallet_id UUID          NOT NULL REFERENCES wallets(id),
    target_wallet_id UUID          NOT NULL REFERENCES wallets(id),
    amount           NUMERIC(19,2) NOT NULL,
    created_at       TIMESTAMP     NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_amount_positive      CHECK (amount > 0),
    CONSTRAINT chk_different_wallets    CHECK (source_wallet_id != target_wallet_id)
);