-- V1: Create notifications table for in-app notification history
CREATE TABLE IF NOT EXISTS notifications (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT,
    workflow_id BIGINT,
    topic       VARCHAR(100),
    title       VARCHAR(255),
    message     TEXT,
    channel     VARCHAR(20)  NOT NULL DEFAULT 'EMAIL',
    read_at     TIMESTAMP WITH TIME ZONE,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id    ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at);
CREATE INDEX IF NOT EXISTS idx_notifications_unread
    ON notifications(user_id, read_at)
    WHERE read_at IS NULL;
