-- For the existing local schema (users.id INT, password_hash, nickname, updated_at).
-- Earlier ddl-auto:update added these unused columns with NOT NULL constraints.
-- Keep their data, but allow inserts that use the original column names.
ALTER TABLE users
    MODIFY COLUMN nick_name VARCHAR(30) NULL,
    MODIFY COLUMN password VARCHAR(100) NULL;

-- Match users.id before Hibernate creates the reset-token foreign key.
ALTER TABLE password_reset_tokens MODIFY COLUMN user_id INT NOT NULL;
