CREATE TABLE supernet_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL
);

CREATE TABLE supernet_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subnet_id VARCHAR(255) NOT NULL,
    category_id BIGINT,
    FOREIGN KEY (category_id) REFERENCES supernet_category(id) ON DELETE CASCADE
);