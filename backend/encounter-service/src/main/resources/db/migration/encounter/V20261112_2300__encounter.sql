CREATE TABLE encounter (
    id UUID PRIMARY KEY,
    encounter_order INTEGER NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    egg_chance INTEGER NOT NULL,
    is_final_boss BOOLEAN NOT NULL,
    CONSTRAINT ck_encounter_egg_chance CHECK (egg_chance BETWEEN 0 AND 100)
);

CREATE TABLE encounter_opponent (
    id UUID PRIMARY KEY,
    encounter_id UUID NOT NULL REFERENCES encounter(id),
    name VARCHAR(255) NOT NULL,
    monster_class VARCHAR(32) NOT NULL,
    level INTEGER NOT NULL,
    avatar_variant INTEGER NOT NULL
);

CREATE TABLE encounter_reward_class (
    encounter_id UUID NOT NULL REFERENCES encounter(id),
    reward_class VARCHAR(32) NOT NULL,
    PRIMARY KEY (encounter_id, reward_class)
);

CREATE TABLE account_encounter_progress (
    account_id UUID NOT NULL,
    encounter_id UUID NOT NULL REFERENCES encounter(id),
    status VARCHAR(16) NOT NULL,
    PRIMARY KEY (account_id, encounter_id)
);

INSERT INTO encounter (id, encounter_order, name, description, egg_chance, is_final_boss)
VALUES
    ('00000000-0000-0000-0000-000000000001', 1, 'Meadow Scouts',
     'A pack of curious scouts patrols the sunny meadow. A gentle first test for new tamers.', 20, false),
    ('00000000-0000-0000-0000-000000000002', 2, 'Old Quarry',
     'Echoing stone walls hide tough defenders who have claimed the quarry as their home.', 25, false),
    ('00000000-0000-0000-0000-000000000003', 3, 'Mossy Ruins',
     'Ancient ruins overgrown with moss. Spirits and tricksters lurk between the broken pillars.', 30, false),
    ('00000000-0000-0000-0000-000000000004', 4, 'Misty Forest',
     'A thick fog hangs between the trees. Travellers say the whole forest whispers at night.', 35, false),
    ('00000000-0000-0000-0000-000000000005', 5, 'Frostpeak Summit',
     'At the top of the frozen mountain waits the final boss. Defeat it to complete your journey.', 0, true);

INSERT INTO encounter_reward_class (encounter_id, reward_class)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Fighter'),
    ('00000000-0000-0000-0000-000000000001', 'Trickster'),
    ('00000000-0000-0000-0000-000000000002', 'Guardian'),
    ('00000000-0000-0000-0000-000000000002', 'Fighter'),
    ('00000000-0000-0000-0000-000000000003', 'Shaman'),
    ('00000000-0000-0000-0000-000000000003', 'Trickster'),
    ('00000000-0000-0000-0000-000000000004', 'Fighter'),
    ('00000000-0000-0000-0000-000000000004', 'Guardian'),
    ('00000000-0000-0000-0000-000000000004', 'Shaman');

INSERT INTO encounter_opponent (id, encounter_id, name, monster_class, level, avatar_variant)
VALUES
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Meadow Pup', 'Fighter', 1, 0),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'Field Mouse', 'Trickster', 1, 0),
    ('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', 'Rock Cub', 'Guardian', 2, 0),
    ('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000002', 'Quarry Boar', 'Fighter', 2, 2),
    ('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000003', 'Ruin Wisp', 'Shaman', 3, 0),
    ('10000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000003', 'Moss Gecko', 'Trickster', 3, 2),
    ('10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000003', 'Stone Newt', 'Guardian', 3, 2),
    ('10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000004', 'Fog Wolf', 'Fighter', 3, 1),
    ('10000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000004', 'Forest Scout', 'Trickster', 3, 1),
    ('10000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000004', 'Mist Owl', 'Shaman', 4, 1),
    ('10000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000005', 'Frost Titan', 'Guardian', 10, 0);