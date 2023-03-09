-- DB作成
CREATE DATABASE chat_app;

-- 作成したDBへ切り替え
\c chat_app

-- スキーマ作成
CREATE SCHEMA develop;

-- ロールの作成
CREATE ROLE developer WITH LOGIN PASSWORD 'passw0rd';

-- 権限追加
GRANT ALL PRIVILEGES ON SCHEMA develop TO developer;