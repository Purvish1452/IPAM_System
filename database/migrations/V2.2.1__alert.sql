CREATE TABLE alert (
alert_key VARCHAR(255) NOT NULL PRIMARY KEY,
alert_value VARCHAR(255)
);

CREATE TABLE alert_stream (
id BIGINT AUTO_INCREMENT,
subnet_id BIGINT,
alert_type VARCHAR(100) NOT NULL,
message VARCHAR(255),
subnet VARCHAR(45),
timestamp DATETIME NOT NULL,
status BOOLEAN,
INDEX idx_subnet_id (subnet_id),
INDEX idx_status (status),
PRIMARY KEY (id, timestamp)
)
PARTITION BY HASH (MONTH(timestamp)) PARTITIONS 12;

