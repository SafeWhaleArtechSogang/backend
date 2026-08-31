ALTER TABLE notifications
    ALTER COLUMN user_id DROP NOT NULL;

ALTER TABLE notifications
    ADD COLUMN admin_id BIGINT REFERENCES admins(id);

ALTER TABLE notifications
    ADD CONSTRAINT chk_notifications_single_recipient
    CHECK (
        (user_id IS NOT NULL AND admin_id IS NULL)
        OR (user_id IS NULL AND admin_id IS NOT NULL)
    );

CREATE INDEX idx_notifications_admin_read ON notifications(admin_id, is_read);
