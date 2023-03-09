\c chat_app

CREATE TABLE develop.user_account_info
(
    user_account_id VARCHAR(255)             NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_account_id)
);

CREATE TABLE develop.user_account_profile
(
    user_account_id VARCHAR(255)             NOT NULL,
    account_name    VARCHAR(55)              NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_account_id)
);

CREATE TABLE develop.user_account_status
(
    user_account_id VARCHAR(255)             NOT NULL,
    account_status  INT                      NOT NULL, -- 1: edit, 2: fixed, 3: closed
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    closed_at       TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (user_account_id, created_at)
);

CREATE TABLE develop.user_account_event
(
    event_id        VARCHAR(255)             NOT NULL,
    user_account_id VARCHAR(255)             NOT NULL,
    event_type      INT                      NOT NULL, -- 1: create, 2: update, 3: close
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    applied_at      TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (event_id),
    UNIQUE (user_account_id, created_at)
);

-- 権限追加
GRANT ALL PRIVILEGES ON develop.user_account_info TO developer;
GRANT ALL PRIVILEGES ON develop.user_account_profile TO developer;
GRANT ALL PRIVILEGES ON develop.user_account_status TO developer;
GRANT ALL PRIVILEGES ON develop.user_account_event TO developer;
