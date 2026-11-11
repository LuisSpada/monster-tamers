CREATE TABLE player
(
    id         UUID PRIMARY KEY,
    account_id UUID         NOT NULL,
    nickname   VARCHAR(255) NOT NULL,
    CONSTRAINT uq_player_account_id UNIQUE (account_id),
    CONSTRAINT uq_player_nickname UNIQUE (nickname)
);
