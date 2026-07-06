CREATE TABLE users (
    id         UUID         PRIMARY KEY,
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(180) NOT NULL,
    password   VARCHAR(100) NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,
    updated_at TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE accounts (
    id         UUID           PRIMARY KEY,
    owner_id   UUID           NOT NULL,
    name       VARCHAR(150)   NOT NULL,
    balance    NUMERIC(19, 2) NOT NULL,
    active     BOOLEAN        NOT NULL DEFAULT TRUE,
    version    BIGINT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ    NOT NULL,
    updated_at TIMESTAMPTZ    NOT NULL,
    CONSTRAINT fk_accounts_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT ck_accounts_balance_non_negative CHECK (balance >= 0)
);

CREATE INDEX idx_accounts_owner_id ON accounts (owner_id);

CREATE TABLE transfers (
    id                     UUID           PRIMARY KEY,
    source_account_id      UUID           NOT NULL,
    destination_account_id UUID           NOT NULL,
    amount                 NUMERIC(19, 2) NOT NULL,
    status                 VARCHAR(20)    NOT NULL,
    idempotency_key        VARCHAR(80),
    created_at             TIMESTAMPTZ    NOT NULL,
    CONSTRAINT fk_transfers_source FOREIGN KEY (source_account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transfers_destination FOREIGN KEY (destination_account_id) REFERENCES accounts (id),
    CONSTRAINT ck_transfers_amount_positive CHECK (amount > 0),
    CONSTRAINT ck_transfers_distinct_accounts CHECK (source_account_id <> destination_account_id),
    CONSTRAINT uq_transfers_idempotency_key UNIQUE (idempotency_key)
);

CREATE INDEX idx_transfers_source ON transfers (source_account_id, created_at DESC);
CREATE INDEX idx_transfers_destination ON transfers (destination_account_id, created_at DESC);
