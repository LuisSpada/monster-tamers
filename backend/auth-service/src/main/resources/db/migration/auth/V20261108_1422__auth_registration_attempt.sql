ALTER TABLE registration_attempt
    ADD COLUMN failed_attempts INTEGER NOT NULL DEFAULT 0;