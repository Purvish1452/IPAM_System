-- Motadata IPAM PostgreSQL Database Initialization Schema

-- Drop tables in reverse dependency order if recreating
DROP TABLE IF EXISTS role_feature_permission CASCADE;
DROP TABLE IF EXISTS feature CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS ip_change_log CASCADE;
DROP TABLE IF EXISTS subnet_ip_details CASCADE;
DROP TABLE IF EXISTS subnet_details CASCADE;
DROP TABLE IF EXISTS supernet_details CASCADE;
DROP TABLE IF EXISTS supernet_category CASCADE;
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS gateway CASCADE;
DROP TABLE IF EXISTS dhcp_utilization CASCADE;
DROP TABLE IF EXISTS dhcp_credential_details CASCADE;
DROP TABLE IF EXISTS alert_stream CASCADE;
DROP TABLE IF EXISTS alert CASCADE;
DROP TABLE IF EXISTS event CASCADE;
DROP TABLE IF EXISTS rogue_detection_details CASCADE;
DROP TABLE IF EXISTS ip_requests CASCADE;
DROP TABLE IF EXISTS global_setting CASCADE;
DROP TABLE IF EXISTS brand CASCADE;
DROP TABLE IF EXISTS mail_server CASCADE;
DROP TABLE IF EXISTS custom_column CASCADE;
DROP TABLE IF EXISTS discovered_subnet CASCADE;
DROP TABLE IF EXISTS flags CASCADE;
DROP TABLE IF EXISTS database_maintainence CASCADE;
DROP TABLE IF EXISTS report CASCADE;
DROP TABLE IF EXISTS vendor CASCADE;

-- 1. User Roles
CREATE TABLE user_role (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- 2. Users (users table replacing user)
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    user_name VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    status BOOLEAN DEFAULT true,
    description VARCHAR(255),
    user_role_id BIGINT REFERENCES user_role(id) ON DELETE SET NULL,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Features
CREATE TABLE feature (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);

-- 4. Role Feature Permissions (PBAC)
CREATE TABLE role_feature_permission (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL REFERENCES user_role(id) ON DELETE CASCADE,
    feature_id BIGINT NOT NULL REFERENCES feature(id) ON DELETE CASCADE,
    read_permission BOOLEAN NOT NULL DEFAULT false,
    write_permission BOOLEAN NOT NULL DEFAULT false
);

-- 5. Categories
CREATE TABLE category (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description VARCHAR(255)
);

-- 6. Supernet Category
CREATE TABLE supernet_category (
    id BIGSERIAL PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL
);

-- 7. Gateways
CREATE TABLE gateway (
    id BIGSERIAL PRIMARY KEY,
    gateway VARCHAR(100) NOT NULL,
    authentication_password VARCHAR(255),
    authentication_protocol VARCHAR(50),
    community VARCHAR(255),
    privacy_protocol VARCHAR(50),
    private_password VARCHAR(255),
    security_level VARCHAR(50),
    security_user_name VARCHAR(255),
    version VARCHAR(20),
    description VARCHAR(255)
);

-- 8. Supernet Details
CREATE TABLE supernet_details (
    id BIGSERIAL PRIMARY KEY,
    supernet_address VARCHAR(100) NOT NULL,
    supernet_mask VARCHAR(50),
    supernet_cidr INTEGER,
    description VARCHAR(255),
    location VARCHAR(100),
    category_id BIGINT REFERENCES category(id) ON DELETE SET NULL
);

-- 9. DHCP Credentials
CREATE TABLE dhcp_credential_details (
    id BIGSERIAL PRIMARY KEY,
    credential_name VARCHAR(100) NOT NULL,
    server_ip VARCHAR(100),
    host_address VARCHAR(100),
    server_type VARCHAR(50),
    type VARCHAR(50),
    user_name VARCHAR(100),
    password VARCHAR(255),
    domain_name VARCHAR(100),
    version VARCHAR(50),
    status VARCHAR(50) DEFAULT 'Active'
);

-- 10. DHCP Scope Utilization
CREATE TABLE dhcp_utilization (
    id BIGSERIAL PRIMARY KEY,
    scope_name VARCHAR(100),
    start_ip VARCHAR(100),
    end_ip VARCHAR(100),
    total_ip BIGINT DEFAULT 0,
    used_ip BIGINT DEFAULT 0,
    available_ip BIGINT DEFAULT 0,
    used_ip_percentage NUMERIC(5,2) DEFAULT 0.00,
    credential_id BIGINT REFERENCES dhcp_credential_details(id) ON DELETE CASCADE
);

-- 11. Subnet Details
CREATE TABLE subnet_details (
    id BIGSERIAL PRIMARY KEY,
    subnet_name VARCHAR(100),
    subnet_address VARCHAR(100) NOT NULL,
    subnet_cidr INTEGER,
    subnet_mask VARCHAR(50),
    description VARCHAR(255),
    location VARCHAR(100),
    is_local_subnet BOOLEAN DEFAULT true,
    snmp_community VARCHAR(100),
    gateway_ip VARCHAR(100),
    gateway_id BIGINT,
    schedule_status BOOLEAN DEFAULT false,
    schedule_hour INTEGER DEFAULT 0,
    total_ip BIGINT DEFAULT 0,
    used_ip BIGINT DEFAULT 0,
    available_ip BIGINT DEFAULT 0,
    transient_ip BIGINT DEFAULT 0,
    last_scan_time TIMESTAMP,
    vlan_name VARCHAR(100),
    dns_address VARCHAR(100),
    allow_icmp BOOLEAN DEFAULT true,
    allow_dns BOOLEAN DEFAULT true,
    category_id BIGINT REFERENCES category(id) ON DELETE SET NULL,
    duration VARCHAR(100),
    type VARCHAR(50),
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_ipv6 BOOLEAN DEFAULT false
);

-- 12. Subnet IP Details
CREATE TABLE subnet_ip_details (
    id BIGSERIAL PRIMARY KEY,
    ip_address VARCHAR(100) NOT NULL,
    mac_address VARCHAR(100),
    host_name VARCHAR(255),
    status VARCHAR(50) DEFAULT 'AVAILABLE',
    device_type VARCHAR(100),
    vendor VARCHAR(100),
    location VARCHAR(100),
    system_description VARCHAR(255),
    dns_status VARCHAR(50) DEFAULT 'Forward & Reverse OK',
    ip_reserved BOOLEAN DEFAULT false,
    alias_name VARCHAR(100),
    subnet_id BIGINT NOT NULL REFERENCES subnet_details(id) ON DELETE CASCADE,
    last_scan_time TIMESTAMP,
    created_by VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS subnet_ip_details_ip_address_uq
    ON subnet_ip_details (ip_address);

-- 13. IP Change Log
CREATE TABLE ip_change_log (
    id BIGSERIAL PRIMARY KEY,
    ip_address_id BIGINT,
    subnet_id BIGINT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_name VARCHAR(100),
    ip VARCHAR(100),
    changelog VARCHAR(255)
);

-- 14. Alert Configuration
CREATE TABLE alert (
    alert_key VARCHAR(100) PRIMARY KEY,
    alert_value VARCHAR(255)
);

-- 15. Alert Stream
CREATE TABLE alert_stream (
    id BIGSERIAL PRIMARY KEY,
    subnet_id BIGINT,
    alert_type VARCHAR(100) NOT NULL,
    message VARCHAR(255),
    subnet VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status BOOLEAN DEFAULT true
);

-- 16. Event Logs
CREATE TABLE event (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(100) NOT NULL,
    event_context VARCHAR(100),
    message VARCHAR(500),
    user_name VARCHAR(100),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 17. Rogue Detection Details
CREATE TABLE rogue_detection_details (
    id BIGSERIAL PRIMARY KEY,
    mac_address VARCHAR(100) NOT NULL,
    ip_address VARCHAR(100),
    discovered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    nic_type VARCHAR(100),
    authenticity VARCHAR(50) DEFAULT 'UNAUTHORIZED',
    subnet_id BIGINT,
    host_name VARCHAR(255)
);

-- 18. IP Requests
CREATE TABLE ip_requests (
    id BIGSERIAL PRIMARY KEY,
    created_by VARCHAR(100),
    requested_by VARCHAR(100),
    number_of_ips INTEGER DEFAULT 1,
    subnet_id VARCHAR(100),
    subnet_address VARCHAR(100),
    status VARCHAR(50) DEFAULT 'PENDING',
    purpose VARCHAR(255),
    remark VARCHAR(255),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 19. Global Settings
CREATE TABLE global_setting (
    id BIGSERIAL PRIMARY KEY,
    logging_level INTEGER DEFAULT 1,
    css_mode INTEGER DEFAULT 1,
    session_timeout INTEGER DEFAULT 1800
);

-- 20. Brand Settings
CREATE TABLE brand (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(100) DEFAULT 'IP Address Manager',
    product_img VARCHAR(255) DEFAULT '/images/logo.png'
);

-- 21. Mail Server Configuration
CREATE TABLE mail_server (
    id BIGSERIAL PRIMARY KEY,
    smtp_host VARCHAR(255) DEFAULT 'smtp.gmail.com',
    smtp_port INTEGER DEFAULT 587,
    smtp_user VARCHAR(100),
    smtp_password VARCHAR(255),
    from_address VARCHAR(255) DEFAULT 'admin@motadata.com',
    ssl_enable BOOLEAN DEFAULT false,
    tls_enable BOOLEAN DEFAULT true
);

-- 22. Custom Columns
CREATE TABLE custom_column (
    id BIGSERIAL PRIMARY KEY,
    column_name VARCHAR(100) NOT NULL,
    column_type VARCHAR(50) DEFAULT 'STRING',
    description VARCHAR(255)
);

-- 23. Discovered Subnets
CREATE TABLE discovered_subnet (
    id BIGSERIAL PRIMARY KEY,
    subnet_address VARCHAR(100) NOT NULL,
    subnet_mask VARCHAR(50),
    discovered_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    gateway_id BIGINT,
    status VARCHAR(50) DEFAULT 'Active'
);

-- 24. System Flags
CREATE TABLE flags (
    id BIGSERIAL PRIMARY KEY,
    flag_key VARCHAR(100) UNIQUE NOT NULL,
    flag_value VARCHAR(255)
);

-- 25. Database Maintenance
CREATE TABLE database_maintainence (
    id BIGSERIAL PRIMARY KEY,
    status VARCHAR(100) DEFAULT 'Healthy',
    backup_path VARCHAR(255) DEFAULT '/opt/motadata/backup',
    duration VARCHAR(100) DEFAULT 'Daily',
    schedule_status BOOLEAN DEFAULT true,
    schedule_hour INTEGER DEFAULT 2,
    auto_backup BOOLEAN DEFAULT true,
    retention_days INTEGER DEFAULT 30
);

-- 26. Reports Scheduling
CREATE TABLE report (
    id BIGSERIAL PRIMARY KEY,
    schedule_name VARCHAR(100) NOT NULL,
    report_type VARCHAR(50) DEFAULT 'PDF',
    schedule_time VARCHAR(50) DEFAULT '09:00',
    schedule_status BOOLEAN DEFAULT true,
    recipients VARCHAR(255) DEFAULT 'admin@motadata.com'
);

-- 27. Vendor MAC OUI
CREATE TABLE vendor (
    id BIGSERIAL PRIMARY KEY,
    vendor_name VARCHAR(100) NOT NULL,
    mac_prefix VARCHAR(50),
    count INTEGER DEFAULT 0
);

-- Seed Initial Data

-- Roles
INSERT INTO user_role (id, role, description) VALUES
(1, 'ROLE_ADMIN', 'Administrator Role'),
(2, 'ROLE_USER', 'Standard User Role');

-- Users: admin & purvish (pass: admin123 hashed with BCrypt)
INSERT INTO users (id, user_name, password, email, status, description, user_role_id, created_by) VALUES
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@motadata.com', true, 'System Administrator', 1, 'system'),
(2, 'purvish', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'purvishpanchal2005@gmail.com', true, 'Purvish User', 2, 'admin');

-- Features
INSERT INTO feature (id, name) VALUES
(1, 'ALERTS'),
(2, 'ROGUE DETECTION'),
(3, 'REPORTS'),
(4, 'EVENT NOTIFICATIONS'),
(5, 'SETTINGS'),
(6, 'DASHBOARD'),
(7, 'IP REQUESTS');

-- Role Feature Permissions
INSERT INTO role_feature_permission (role_id, feature_id, read_permission, write_permission) VALUES
(1, 1, true, true),
(1, 2, true, true),
(1, 3, true, true),
(1, 4, true, true),
(1, 5, true, true),
(1, 6, true, true),
(1, 7, true, true),
(2, 1, true, false),
(2, 2, true, false),
(2, 3, true, false),
(2, 4, true, false),
(2, 5, true, false),
(2, 6, true, false),
(2, 7, true, false);

-- Categories
INSERT INTO category (id, category_name, description) VALUES
(1, 'Default Category', 'Default Category for Unassigned Subnets'),
(2, 'Production Subnets', 'Core Production Network Infrastructure'),
(3, 'Development Subnets', 'Staging and Development Environments');

INSERT INTO supernet_category (id, category_name) VALUES
(1, 'Enterprise Supernet');

-- Gateways
INSERT INTO gateway (id, gateway, description, version) VALUES
(1, '192.168.1.1', 'Default Core Gateway Router', 'v2c'),
(2, '10.0.0.1', 'Corporate Backbone Gateway', 'v3');

-- Supernet Details
INSERT INTO supernet_details (id, supernet_address, supernet_mask, supernet_cidr, description, location, category_id) VALUES
(1, '10.0.0.0', '255.0.0.0', 8, 'Corporate Class A Supernet', 'Headquarters', 1);

-- DHCP Credentials
INSERT INTO dhcp_credential_details (id, credential_name, server_ip, host_address, server_type, type, user_name, status) VALUES
(1, 'WinDHCP-Primary', '192.168.1.1', '192.168.1.1', 'WINDOWS', 'WINDOWS', 'Administrator', 'Active'),
(2, 'CiscoDHCP-Core', '192.168.1.2', '192.168.1.2', 'CISCO', 'CISCO', 'admin', 'Active');

-- DHCP Scope Utilization
INSERT INTO dhcp_utilization (id, scope_name, start_ip, end_ip, total_ip, used_ip, available_ip, used_ip_percentage, credential_id) VALUES
(1, 'Office-DHCP-Pool', '192.168.1.100', '192.168.1.250', 151, 35, 116, 23.18, 1),
(2, 'Core-Cisco-Pool', '10.0.1.1', '10.0.1.254', 254, 80, 174, 31.50, 2);

-- Subnet Details
INSERT INTO subnet_details (id, subnet_name, subnet_address, subnet_cidr, subnet_mask, description, location, is_local_subnet, total_ip, used_ip, available_ip, transient_ip, last_scan_time, vlan_name, dns_address, category_id, type) VALUES
(1, '192.168.10.0/24', '192.168.10.0', 24, '255.255.255.0', 'Primary Office LAN Subnet', 'Headquarters Data Center', true, 256, 45, 206, 5, CURRENT_TIMESTAMP, 'Default VLAN', '8.8.8.8', 1, 'DHCP'),
(2, '10.0.0.0/16', '10.0.0.0', 16, '255.255.0.0', 'Production Server Infrastructure', 'East DC', true, 65536, 120, 65416, 0, CURRENT_TIMESTAMP, 'Servers VLAN', '1.1.1.1', 2, 'STATIC');

-- Subnet IP Details
INSERT INTO subnet_ip_details (id, ip_address, mac_address, host_name, status, device_type, vendor, location, system_description, dns_status, subnet_id, last_scan_time) VALUES
(1, '192.168.10.1', '00:50:56:FE:10:01', 'gateway-01.motadata.local', 'USED', 'ROUTER', 'Cisco Systems', 'HQ DC', 'Cisco 2960 Gateway', 'Forward & Reverse OK', 1, CURRENT_TIMESTAMP),
(2, '192.168.10.10', '00:50:56:FE:10:02', 'db-primary.motadata.local', 'USED', 'SERVER', 'VMware Inc', 'HQ DC', 'PostgreSQL Database Server', 'Forward & Reverse OK', 1, CURRENT_TIMESTAMP),
(3, '192.168.10.11', '00:50:56:FE:10:03', 'web-app.motadata.local', 'USED', 'SERVER', 'VMware Inc', 'HQ DC', 'Vert.x Application Server', 'Forward & Reverse OK', 1, CURRENT_TIMESTAMP),
(4, '192.168.10.12', '00:50:56:FE:10:04', 'cache-01.motadata.local', 'TRANSIENT', 'SERVER', 'Dell Inc', 'HQ DC', 'Redis Cache Node', 'Forward Only', 1, CURRENT_TIMESTAMP),
(5, '192.168.10.15', NULL, NULL, 'AVAILABLE', NULL, NULL, 'HQ DC', NULL, 'Forward & Reverse OK', 1, CURRENT_TIMESTAMP);

-- Alert Configuration
INSERT INTO alert (alert_key, alert_value) VALUES
('ipUtilizationBelowFlag', 'true'),
('ipUtilizationFlag', 'true'),
('macIpChangeFlag', 'true'),
('rogueDetection', 'true'),
('ipStateChange', 'true'),
('reverseLookupFailed', 'true'),
('forwardLookupFailed', 'false'),
('forwardLookupMismatch', 'false'),
('ipReservationChange', 'true'),
('ipConflict', 'true'),
('newSubnetsDiscovered', 'true'),
('ipUtilizationBelow', '20'),
('ipUtilization', '80'),
('macIpChange', '00:50:56:FE:DC:BA');

-- Alert Stream
INSERT INTO alert_stream (id, subnet_id, alert_type, message, subnet, timestamp, status) VALUES
(1, 1, 'CRITICAL', 'Subnet 192.168.10.0 utilization exceeded 80%', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '2 hours', true),
(2, 1, 'MAJOR', 'Rogue IP 192.168.1.99 detected with MAC 00:50:56:FE:DC:BA', '192.168.10.0', CURRENT_TIMESTAMP - INTERVAL '1 hour', true);

-- Event Logs
INSERT INTO event (id, event_type, event_context, message, user_name, timestamp) VALUES
(1, 'Information', 'Subnet Management', 'Subnet 192.168.10.0 is added in IP Address Manager by admin', 'admin', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(2, 'Information', 'DHCP Management', 'DHCP Server WinDHCP-Primary synced', 'admin', CURRENT_TIMESTAMP - INTERVAL '5 hours'),
(3, 'Warning', 'IP Conflict', 'IP Address 192.168.10.12 status changed to TRANSIENT', 'system', CURRENT_TIMESTAMP - INTERVAL '30 minutes');

-- Rogue Detection Details
INSERT INTO rogue_detection_details (id, mac_address, ip_address, nic_type, authenticity, subnet_id, host_name) VALUES
(1, '00:50:56:FE:DC:BA', '192.168.1.99', 'VMware Virtual NIC', 'UNAUTHORIZED', 1, 'rogue-node-01');

-- IP Requests
INSERT INTO ip_requests (id, created_by, requested_by, number_of_ips, subnet_id, subnet_address, status, purpose, remark) VALUES
(1, 'purvish', 'purvish', 5, '1', '192.168.10.0/24', 'PENDING', 'Development Server Cluster', 'Need 5 static IPs for new microservices deployment');

-- Global Settings
INSERT INTO global_setting (id, logging_level, css_mode, session_timeout) VALUES
(1, 1, 1, 1800);

-- Brand
INSERT INTO brand (id, product_name, product_img) VALUES
(1, 'IP Address Manager', '/images/logo.png');

-- Mail Server
INSERT INTO mail_server (id, smtp_host, smtp_port, smtp_user, from_address, ssl_enable, tls_enable) VALUES
(1, 'smtp.gmail.com', 587, 'admin@motadata.com', 'admin@motadata.com', false, true);

-- Custom Columns
INSERT INTO custom_column (id, column_name, column_type, description) VALUES
(1, 'Asset Tag', 'STRING', 'Hardware Asset Identifier'),
(2, 'Owner Department', 'STRING', 'Department responsible for IP allocation');

-- Discovered Subnets
INSERT INTO discovered_subnet (id, subnet_address, subnet_mask, gateway_id, status) VALUES
(1, '192.168.50.0', '255.255.255.0', 1, 'Active');

-- Database Maintenance
INSERT INTO database_maintainence (id, status, backup_path, duration, schedule_status, schedule_hour, auto_backup, retention_days) VALUES
(1, 'Healthy', '/opt/motadata/backup', 'Daily', true, 2, true, 30);

-- Reports
INSERT INTO report (id, schedule_name, report_type, schedule_time, schedule_status, recipients) VALUES
(1, 'Weekly Subnet Summary', 'PDF', '09:00', true, 'admin@motadata.com');

-- Vendors
INSERT INTO vendor (id, vendor_name, mac_prefix, count) VALUES
(1, 'Cisco Systems', '00:50:56', 120),
(2, 'VMware Inc', '00:0C:29', 45),
(3, 'Intel Corp', '00:1B:21', 30),
(4, 'Dell Inc', '00:14:22', 25);

-- Update Sequences
SELECT setval('user_role_id_seq', (SELECT MAX(id) FROM user_role));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('feature_id_seq', (SELECT MAX(id) FROM feature));
SELECT setval('role_feature_permission_id_seq', (SELECT MAX(id) FROM role_feature_permission));
SELECT setval('category_id_seq', (SELECT MAX(id) FROM category));
SELECT setval('supernet_category_id_seq', (SELECT MAX(id) FROM supernet_category));
SELECT setval('gateway_id_seq', (SELECT MAX(id) FROM gateway));
SELECT setval('supernet_details_id_seq', (SELECT MAX(id) FROM supernet_details));
SELECT setval('dhcp_credential_details_id_seq', (SELECT MAX(id) FROM dhcp_credential_details));
SELECT setval('dhcp_utilization_id_seq', (SELECT MAX(id) FROM dhcp_utilization));
SELECT setval('subnet_details_id_seq', (SELECT MAX(id) FROM subnet_details));
SELECT setval('subnet_ip_details_id_seq', (SELECT MAX(id) FROM subnet_ip_details));
SELECT setval('alert_stream_id_seq', (SELECT MAX(id) FROM alert_stream));
SELECT setval('event_id_seq', (SELECT MAX(id) FROM event));
SELECT setval('rogue_detection_details_id_seq', (SELECT MAX(id) FROM rogue_detection_details));
SELECT setval('ip_requests_id_seq', (SELECT MAX(id) FROM ip_requests));
SELECT setval('global_setting_id_seq', (SELECT MAX(id) FROM global_setting));
SELECT setval('brand_id_seq', (SELECT MAX(id) FROM brand));
SELECT setval('mail_server_id_seq', (SELECT MAX(id) FROM mail_server));
SELECT setval('custom_column_id_seq', (SELECT MAX(id) FROM custom_column));
SELECT setval('discovered_subnet_id_seq', (SELECT MAX(id) FROM discovered_subnet));
SELECT setval('database_maintainence_id_seq', (SELECT MAX(id) FROM database_maintainence));
SELECT setval('report_id_seq', (SELECT MAX(id) FROM report));
SELECT setval('vendor_id_seq', (SELECT MAX(id) FROM vendor));
