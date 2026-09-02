CREATE TABLE custom_column (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                column_name VARCHAR(50) NOT NULL,
                                column_at VARCHAR(255) NOT NULL,
                                description VARCHAR(255),
                                created_by VARCHAR(255) NOT NULL,
                                created_date DATETIME NOT NULL,
                                last_modified_by VARCHAR(255),
                                last_modified_date DATETIME
);

ALTER TABLE subnet_ip_details ADD COLUMN custom_columns LONGTEXT;


