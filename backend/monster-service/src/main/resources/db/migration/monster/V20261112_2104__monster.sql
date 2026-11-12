CREATE TABLE monster
(
    id            UUID PRIMARY KEY,
    account_id    UUID         NOT NULL,
    name          VARCHAR(255) NOT NULL,
    monster_class VARCHAR(32)  NOT NULL,
    obtained_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_monster_account_id ON monster (account_id);
