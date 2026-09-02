CREATE TABLE rogue_detection_details (
id BIGINT AUTO_INCREMENT,
mac_address VARCHAR(255) NOT NULL,
ip_address VARCHAR(255) NOT NULL,
discovered_at datetime NOT NULL,
nic_type VARCHAR(255),
authenticity VARCHAR(255) NOT NULL,
INDEX idx_mac_address (mac_address),
PRIMARY KEY (id, mac_address)
);

INSERT INTO rogue_detection_details (mac_address, ip_address, discovered_at, nic_type, authenticity)
SELECT
    mac_address,
    ip_address,
    last_alive_time AS discovered_at,
    device_type AS nic_type,
    CASE
        WHEN rogue_status = b'1' THEN 'rogue'
        ELSE 'discovered'
        END AS authenticity
FROM subnet_ip_details
WHERE mac_address IS NOT NULL
  AND status = 'Used';

ALTER TABLE subnet_ip_details CHANGE rogue_status authenticity VARCHAR(20);

UPDATE subnet_ip_details
SET authenticity = CASE
        WHEN authenticity = b'1' THEN 'rogue'
        WHEN status = 'Used' AND mac_address IS NOT NULL THEN 'discovered'
        ELSE '-'
END;