CREATE TABLE team
(
    id         UUID PRIMARY KEY,
    account_id UUID NOT NULL UNIQUE
);

CREATE TABLE team_monster
(
    team_id    UUID    NOT NULL REFERENCES team (id) ON DELETE CASCADE,
    position   INTEGER NOT NULL,
    monster_id UUID    NOT NULL REFERENCES monster (id),
    PRIMARY KEY (team_id, position),
    CONSTRAINT uq_team_monster UNIQUE (team_id, monster_id) DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT ck_team_monster_position CHECK (position BETWEEN 1 AND 3)
);
