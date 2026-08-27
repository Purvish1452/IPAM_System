CREATE TABLE flags (
    flag VARCHAR(100) NOT NULL UNIQUE,
    value TINYINT(1) NOT NULL DEFAULT 0
);
INSERT INTO flags (flag, value) VALUES ('is_auto_discovered', 0);

ALTER TABLE gateway ADD COLUMN name VARCHAR(255), ADD COLUMN previous_scan DATETIME, ADD COLUMN status VARCHAR(100);

CREATE TABLE discovered_subnet (
    id INT AUTO_INCREMENT PRIMARY KEY,
    subnet VARCHAR(45) NOT NULL,
    subnet_mask VARCHAR(100) NOT NULL,
    gateway VARCHAR(45) NOT NULL,
    gateway_id BIGINT NOT NULL
);
