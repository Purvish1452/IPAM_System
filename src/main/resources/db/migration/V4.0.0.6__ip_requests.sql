CREATE TABLE ip_requests (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             number_of_ips INT NOT NULL,
                             ips JSON NOT NULL,
                             status VARCHAR(50) NOT NULL,
                             subnet_id VARCHAR(255),
                             remark VARCHAR(500),
                             preferred_subnet BOOLEAN,
                             purpose TEXT,
                             created_by VARCHAR(255) NOT NULL,
                             created_date DATETIME NOT NULL,
                             last_modified_by VARCHAR(255),
                             last_modified_date DATETIME
);

INSERT INTO feature (name) VALUES ('IP REQUESTS');

INSERT INTO role_feature_permission (role_id, feature_id, read_permission, write_permission)
SELECT ur.id, f.id,
       CASE
           WHEN ur.role = 'Admin' THEN true
           WHEN ur.role = 'User' THEN true
           ELSE false
           END AS read_permission,
       CASE
           WHEN ur.role = 'Admin' THEN true
           WHEN ur.role = 'User' THEN false
           ELSE false
           END AS write_permission
FROM user_role ur
         CROSS JOIN feature f
WHERE f.name = 'IP REQUESTS'
  AND NOT EXISTS (
    SELECT 1 FROM role_feature_permission rfp
    WHERE rfp.role_id = ur.id AND rfp.feature_id = f.id
);

TRUNCATE TABLE oauth_access_token;

TRUNCATE TABLE oauth_refresh_token;

ALTER TABLE oauth_access_token MODIFY COLUMN token LONGBLOB;

