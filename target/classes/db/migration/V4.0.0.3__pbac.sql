CREATE TABLE feature (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL
);

CREATE TABLE role_feature_permission (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         role_id BIGINT NOT NULL,
                                         feature_id BIGINT NOT NULL,
                                         read_permission BOOLEAN NOT NULL DEFAULT false,
                                         write_permission BOOLEAN NOT NULL DEFAULT false,
                                         FOREIGN KEY (role_id) REFERENCES user_role(id) ON DELETE CASCADE,
                                         FOREIGN KEY (feature_id) REFERENCES feature(id) ON DELETE CASCADE
);

ALTER TABLE user_role ADD COLUMN description VARCHAR(255) NULL;

INSERT INTO feature (id, name) VALUES
                                   (1, 'ALERTS'),
                                   (2, 'ROGUE DETECTION'),
                                   (3, 'REPORTS'),
                                   (4, 'EVENT NOTIFICATIONS'),
                                   (5, 'SETTINGS'),
                                   (6, 'DASHBOARD');


INSERT INTO role_feature_permission (role_id, feature_id, read_permission, write_permission) VALUES
                                                                                               (1, 1, true, true),
                                                                                               (1, 2, true, true),
                                                                                               (1, 3, true, true),
                                                                                               (1, 4, true, true),
                                                                                               (1, 5, true, true),
                                                                                               (1, 6, true, true);

INSERT INTO role_feature_permission (role_id, feature_id, read_permission, write_permission) VALUES
                                                                                               (2, 1, true, false),
                                                                                               (2, 2, true, false),
                                                                                               (2, 3, true, false),
                                                                                               (2, 4, true, false),
                                                                                               (2, 5, true, false),
                                                                                               (2, 6, true, false);


TRUNCATE TABLE oauth_access_token;

TRUNCATE TABLE oauth_refresh_token;
