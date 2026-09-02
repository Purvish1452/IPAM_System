CREATE TABLE gateway (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
gateway VARCHAR(45) NOT NULL,
authentication_password VARCHAR(255),
authentication_protocol VARCHAR(50),
community VARCHAR(255),
privacy_protocol VARCHAR(50),
private_password VARCHAR(255),
security_level VARCHAR(50),
security_user_name VARCHAR(255),
version VARCHAR(10)
);

ALTER TABLE subnet_details ADD COLUMN gateway_id BIGINT;