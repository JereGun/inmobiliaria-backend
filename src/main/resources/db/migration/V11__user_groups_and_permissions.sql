-- Grupos de usuarios con permisos configurables.
CREATE TABLE IF NOT EXISTS user_group (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_user_group_name ON user_group (LOWER(name));

CREATE TABLE IF NOT EXISTS user_group_permission (
    group_id   BIGINT      NOT NULL REFERENCES user_group (id) ON DELETE CASCADE,
    permission VARCHAR(50) NOT NULL,
    PRIMARY KEY (group_id, permission)
);

CREATE TABLE IF NOT EXISTS user_group_member (
    user_id  BIGINT NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES user_group (id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, group_id)
);

CREATE INDEX IF NOT EXISTS idx_user_group_member_group ON user_group_member (group_id);
