-- Apply after entity mappings use password_hash, nickname and updated_at.
-- Verified that the legacy columns contain no differing passwords/nicknames
-- or newer update timestamps before applying to the local database.
ALTER TABLE users
    DROP COLUMN password,
    DROP COLUMN nick_name,
    DROP COLUMN update_at;
