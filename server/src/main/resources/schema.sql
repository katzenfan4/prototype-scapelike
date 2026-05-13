CREATE TABLE IF NOT EXISTS level_threshold (
    level       INT PRIMARY KEY,
    xp_required BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS skill (
    id        INT PRIMARY KEY AUTO_INCREMENT,
    name      VARCHAR(64) NOT NULL UNIQUE,
    max_level INT NOT NULL DEFAULT 99
);

CREATE TABLE IF NOT EXISTS player (
    id       BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(32) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS player_session (
    token      CHAR(36) PRIMARY KEY,
    player_id  BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS player_skill (
    player_id BIGINT NOT NULL,
    skill_id  INT NOT NULL,
    xp        BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (player_id, skill_id),
    FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id)  REFERENCES skill(id)
);

CREATE TABLE IF NOT EXISTS xp_event (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    player_id  BIGINT NOT NULL,
    skill_id   INT NOT NULL,
    xp_gained  INT NOT NULL,
    source     VARCHAR(64) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id)  REFERENCES skill(id)
);

CREATE TABLE IF NOT EXISTS item (
    id             INT PRIMARY KEY AUTO_INCREMENT,
    name           VARCHAR(64) NOT NULL UNIQUE,
    type           VARCHAR(32) NOT NULL,
    stackable      BOOLEAN NOT NULL DEFAULT FALSE,
    max_stack_size INT NOT NULL DEFAULT 1,
    properties     JSON
);

CREATE TABLE IF NOT EXISTS inventory_slot (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    player_id  BIGINT NOT NULL,
    item_id    INT NOT NULL,
    quantity   INT NOT NULL DEFAULT 1,
    slot_index INT NOT NULL,
    FOREIGN KEY (player_id) REFERENCES player(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id)   REFERENCES item(id)
);
