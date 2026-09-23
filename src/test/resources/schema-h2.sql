-- H2（MySQL 兼容模式）下的赛鸽表结构，仅用于无 Docker 环境的端到端验证。
-- 列定义与 doc/schema/pigeon.sql 保持一一对应（H2 不认 ENGINE/CHARSET 子句，故去掉）。

CREATE TABLE t_band (
    id          BIGINT       NOT NULL PRIMARY KEY,
    band_code   VARCHAR(32)  NOT NULL,
    band_year   INT          NOT NULL,
    owner_name  VARCHAR(64)  NOT NULL,
    loft_city   VARCHAR(64)  DEFAULT NULL,
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE',
    del_flag    TINYINT      NOT NULL DEFAULT 0,
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    CONSTRAINT uk_band_code UNIQUE (band_code)
);

CREATE TABLE t_race (
    id           BIGINT        NOT NULL PRIMARY KEY,
    race_code    VARCHAR(32)   NOT NULL,
    title        VARCHAR(128)  NOT NULL,
    release_site VARCHAR(128)  NOT NULL,
    release_at   DATETIME      NOT NULL,
    close_at     DATETIME      DEFAULT NULL,
    distance_km  DECIMAL(8,3)  NOT NULL,
    status       VARCHAR(16)   NOT NULL DEFAULT 'DRAFT',
    del_flag     TINYINT       NOT NULL DEFAULT 0,
    create_by    VARCHAR(64)   DEFAULT NULL,
    create_time  DATETIME      DEFAULT NULL,
    update_by    VARCHAR(64)   DEFAULT NULL,
    update_time  DATETIME      DEFAULT NULL,
    CONSTRAINT uk_race_code UNIQUE (race_code)
);

CREATE TABLE t_entry (
    id          BIGINT       NOT NULL PRIMARY KEY,
    race_id     BIGINT       NOT NULL,
    band_id     BIGINT       NOT NULL,
    basket_no   VARCHAR(16)  DEFAULT NULL,
    entry_time  DATETIME     DEFAULT NULL,
    del_flag    TINYINT      NOT NULL DEFAULT 0,
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    CONSTRAINT uk_race_band UNIQUE (race_id, band_id)
);

CREATE TABLE t_clocking (
    id          BIGINT       NOT NULL PRIMARY KEY,
    entry_id    BIGINT       NOT NULL,
    race_id     BIGINT       NOT NULL,
    clock_at    DATETIME     NOT NULL,
    source      VARCHAR(16)  NOT NULL DEFAULT 'SCAN',
    del_flag    TINYINT      NOT NULL DEFAULT 0,
    create_by   VARCHAR(64)  DEFAULT NULL,
    create_time DATETIME     DEFAULT NULL,
    update_by   VARCHAR(64)  DEFAULT NULL,
    update_time DATETIME     DEFAULT NULL,
    CONSTRAINT uk_entry UNIQUE (entry_id)
);

CREATE TABLE t_result (
    id          BIGINT        NOT NULL PRIMARY KEY,
    race_id     BIGINT        NOT NULL,
    entry_id    BIGINT        NOT NULL,
    speed_mpm   DECIMAL(12,2) NOT NULL,
    rank_no     INT           DEFAULT NULL,
    del_flag    TINYINT       NOT NULL DEFAULT 0,
    create_by   VARCHAR(64)   DEFAULT NULL,
    create_time DATETIME      DEFAULT NULL,
    update_by   VARCHAR(64)   DEFAULT NULL,
    update_time DATETIME      DEFAULT NULL,
    CONSTRAINT uk_race_entry UNIQUE (race_id, entry_id)
);
