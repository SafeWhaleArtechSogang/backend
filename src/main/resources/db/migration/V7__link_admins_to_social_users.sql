-- Google 계정은 users가 유일한 인증 주체다. 관리자는 그 계정에 연결된 권한 프로필이다.
-- 기존 ID/비밀번호 관리자 행은 서비스 중단 없이 유지되며, 아래 user_id를 채운 뒤 Google 로그인으로 전환한다.
ALTER TABLE admins
    ADD COLUMN user_id BIGINT REFERENCES users(id);

-- Google 전용 관리자에는 로컬 비밀번호를 요구하지 않는다. 기존 로컬 계정은 그대로 동작한다.
ALTER TABLE admins
    ALTER COLUMN login_id DROP NOT NULL,
    ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE admins
    ADD CONSTRAINT uk_admins_user_id UNIQUE (user_id);

CREATE INDEX idx_admins_user_id ON admins(user_id);
