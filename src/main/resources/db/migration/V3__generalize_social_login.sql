ALTER TABLE users RENAME COLUMN kakao_id TO provider_id;
ALTER TABLE users ALTER COLUMN provider_id TYPE VARCHAR(128);
ALTER TABLE users ADD COLUMN provider VARCHAR(20) NOT NULL DEFAULT 'GOOGLE';
ALTER TABLE users DROP CONSTRAINT users_kakao_id_key;
ALTER TABLE users ADD CONSTRAINT uk_users_provider_id UNIQUE (provider, provider_id);
