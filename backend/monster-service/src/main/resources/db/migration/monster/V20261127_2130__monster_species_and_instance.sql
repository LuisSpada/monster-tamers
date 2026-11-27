CREATE TABLE monster_species
(
    id                UUID PRIMARY KEY,
    name              VARCHAR(255) NOT NULL UNIQUE,
    monster_class     VARCHAR(32)  NOT NULL,
    base_strength     INTEGER      NOT NULL,
    base_hp           INTEGER      NOT NULL,
    base_accuracy     INTEGER      NOT NULL,
    base_agility      INTEGER      NOT NULL,
    skill_name        VARCHAR(255) NOT NULL,
    skill_description TEXT         NOT NULL,
    trait_name        VARCHAR(255) NOT NULL,
    trait_description TEXT         NOT NULL,
    avatar_variant    INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT ck_monster_species_class
        CHECK (monster_class IN ('FIGHTER', 'GUARDIAN', 'SHAMAN', 'TRICKSTER')),
    CONSTRAINT ck_monster_species_stats
        CHECK (base_strength > 0 AND base_hp > 0 AND base_accuracy > 0 AND base_agility > 0)
);

INSERT INTO monster_species
(id, name, monster_class, base_strength, base_hp, base_accuracy, base_agility,
 skill_name, skill_description, trait_name, trait_description, avatar_variant)
VALUES
    -- 8 monsters from frontend fixtures (lines 102-148)
    ('20000000-0000-0000-0000-000000000001', 'Fierce Fox', 'FIGHTER', 15, 100, 62, 42,
     'Vampiric Strike', 'A strong attack that restores a little health.',
     'Hot-headed', 'Hits harder while at full health.', 0),
    ('20000000-0000-0000-0000-000000000002', 'Calm Drake', 'SHAMAN', 6, 92, 65, 42,
     'Healing Aura', 'Restores health to an ally.',
     'Unshakable', 'Takes slightly less damage.', 0),
    ('20000000-0000-0000-0000-000000000003', 'Sturdy Cub', 'GUARDIAN', 9, 140, 55, 30,
     'Shield Wall', 'Reduces damage taken by the team for a short time.',
     'Quick Learner', 'Notices openings faster than others.', 0),
    ('20000000-0000-0000-0000-000000000004', 'Sly Raccoon', 'TRICKSTER', 8, 85, 70, 60,
     'Smoke Bomb', 'Lowers the accuracy of an enemy.',
     'Lucky', 'Sometimes dodges an attack completely.', 0),
    ('20000000-0000-0000-0000-000000000005', 'Brave Lynx', 'FIGHTER', 16, 95, 60, 44,
     'Cleaving Blow', 'Hits the target with extra force.',
     'Hot-headed', 'Hits harder while at full health.', 1),
    ('20000000-0000-0000-0000-000000000006', 'Gentle Newt', 'SHAMAN', 6, 95, 66, 40,
     'Spirit Boost', 'Strengthens the next attack of an ally.',
     'Unshakable', 'Takes slightly less damage.', 2),
    ('20000000-0000-0000-0000-000000000007', 'Wise Owl', 'GUARDIAN', 8, 135, 60, 32,
     'Taunt', 'Draws the next enemy attacks towards itself.',
     'Quick Learner', 'Notices openings faster than others.', 1),
    ('20000000-0000-0000-0000-000000000008', 'Wily Weasel', 'TRICKSTER', 9, 82, 72, 62,
     'Trip Up', 'Makes an enemy lose its next action.',
     'Lucky', 'Sometimes dodges an attack completely.', 1),
    -- 11 encounter opponents from frontend fixtures (lines 154-185)
    ('20000000-0000-0000-0000-000000000009', 'Meadow Pup', 'FIGHTER', 13, 92, 58, 40,
     'Cleaving Blow', 'Hits the target with extra force.',
     'Hot-headed', 'Hits harder while at full health.', 0),
    ('20000000-0000-0000-0000-000000000010', 'Field Mouse', 'TRICKSTER', 7, 80, 68, 64,
     'Trip Up', 'Makes an enemy lose its next action.',
     'Lucky', 'Sometimes dodges an attack completely.', 0),
    ('20000000-0000-0000-0000-000000000011', 'Rock Cub', 'GUARDIAN', 9, 145, 54, 28,
     'Shield Wall', 'Reduces damage taken by the team for a short time.',
     'Unshakable', 'Takes slightly less damage.', 0),
    ('20000000-0000-0000-0000-000000000012', 'Quarry Boar', 'FIGHTER', 17, 108, 56, 36,
     'Cleaving Blow', 'Hits the target with extra force.',
     'Unshakable', 'Takes slightly less damage.', 2),
    ('20000000-0000-0000-0000-000000000013', 'Ruin Wisp', 'SHAMAN', 7, 86, 68, 45,
     'Spirit Boost', 'Strengthens the next attack of an ally.',
     'Quick Learner', 'Notices openings faster than others.', 0),
    ('20000000-0000-0000-0000-000000000014', 'Moss Gecko', 'TRICKSTER', 8, 86, 71, 61,
     'Trip Up', 'Makes an enemy lose its next action.',
     'Lucky', 'Sometimes dodges an attack completely.', 2),
    ('20000000-0000-0000-0000-000000000015', 'Stone Newt', 'GUARDIAN', 8, 142, 56, 30,
     'Taunt', 'Draws the next enemy attacks towards itself.',
     'Unshakable', 'Takes slightly less damage.', 2),
    ('20000000-0000-0000-0000-000000000016', 'Fog Wolf', 'FIGHTER', 16, 102, 62, 43,
     'Vampiric Strike', 'A strong attack that restores a little health.',
     'Hot-headed', 'Hits harder while at full health.', 1),
    ('20000000-0000-0000-0000-000000000017', 'Forest Scout', 'TRICKSTER', 9, 84, 73, 63,
     'Smoke Bomb', 'Lowers the accuracy of an enemy.',
     'Quick Learner', 'Notices openings faster than others.', 1),
    ('20000000-0000-0000-0000-000000000018', 'Mist Owl', 'SHAMAN', 6, 94, 67, 43,
     'Healing Aura', 'Restores health to an ally.',
     'Lucky', 'Sometimes dodges an attack completely.', 1),
    ('20000000-0000-0000-0000-000000000019', 'Frost Titan', 'GUARDIAN', 12, 175, 58, 26,
     'Shield Wall', 'Reduces damage taken by the team for a short time.',
     'Unshakable', 'Takes slightly less damage.', 0);

ALTER TABLE monster
    ADD COLUMN species_id     UUID REFERENCES monster_species (id),
    ADD COLUMN nickname       VARCHAR(255),
    ADD COLUMN level          INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN xp             INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN talent         INTEGER NOT NULL DEFAULT 100,
    ADD COLUMN pending_points INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN strength       INTEGER NOT NULL DEFAULT 10,
    ADD COLUMN hp             INTEGER NOT NULL DEFAULT 100,
    ADD COLUMN accuracy       INTEGER NOT NULL DEFAULT 60,
    ADD COLUMN agility        INTEGER NOT NULL DEFAULT 40,
    ADD COLUMN avatar_variant INTEGER NOT NULL DEFAULT 0;

-- Backfill any existing rows from the old schema so nothing breaks
UPDATE monster m
SET species_id     = s.id,
    strength       = s.base_strength,
    hp             = s.base_hp,
    accuracy       = s.base_accuracy,
    agility        = s.base_agility,
    avatar_variant = s.avatar_variant
FROM monster_species s
WHERE m.species_id IS NULL
  AND s.id = CASE m.monster_class
                 WHEN 'FIGHTER' THEN '20000000-0000-0000-0000-000000000001'::uuid
                 WHEN 'SHAMAN' THEN '20000000-0000-0000-0000-000000000002'::uuid
                 WHEN 'GUARDIAN' THEN '20000000-0000-0000-0000-000000000003'::uuid
                 WHEN 'TRICKSTER' THEN '20000000-0000-0000-0000-000000000004'::uuid
                 ELSE '20000000-0000-0000-0000-000000000001'::uuid
             END;

ALTER TABLE monster
    ALTER COLUMN species_id SET NOT NULL,
    DROP COLUMN name,
    DROP COLUMN monster_class,
    ADD CONSTRAINT ck_monster_level CHECK (level BETWEEN 1 AND 10),
    ADD CONSTRAINT ck_monster_xp CHECK (xp >= 0),
    ADD CONSTRAINT ck_monster_talent CHECK (talent BETWEEN 80 AND 120),
    ADD CONSTRAINT ck_monster_pending_points CHECK (pending_points >= 0);
CREATE INDEX idx_monster_species_id ON monster (species_id);