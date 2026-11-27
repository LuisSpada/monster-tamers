CREATE TABLE battle (
    id UUID PRIMARY KEY,
    account_id UUID NOT NULL,
    encounter_id UUID NOT NULL REFERENCES encounter(id),
    outcome VARCHAR(16) NOT NULL,
    rounds INTEGER NOT NULL,
    played_at TIMESTAMP WITH TIME ZONE NOT NULL,
    participants JSONB NOT NULL
);

CREATE INDEX ix_battle_account_played_at ON battle (account_id, played_at DESC);

CREATE TABLE battle_log (
    battle_id UUID PRIMARY KEY REFERENCES battle(id),
    events JSONB NOT NULL
);
