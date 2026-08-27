ALTER TABLE database_maintainence
ADD COLUMN status VARCHAR(255),
ADD COLUMN backup_path VARCHAR(255),
ADD COLUMN duration VARCHAR(255),
ADD COLUMN schedule_status BOOLEAN DEFAULT FALSE,
ADD COLUMN schedule_hour INT DEFAULT 0;

CREATE TABLE ip_change_log (
id BIGINT AUTO_INCREMENT,
ip_address_id BIGINT NOT NULL,
subnet_id BIGINT NOT NULL,
timestamp DATETIME NOT NULL,
user VARCHAR(255) NOT NULL,
ip VARCHAR(45) NOT NULL,
changelog VARCHAR(255) NOT NULL,
INDEX idx_ip_address_id (ip_address_id),
PRIMARY KEY (id, timestamp)
)
PARTITION BY HASH (MONTH(timestamp)) PARTITIONS 12;