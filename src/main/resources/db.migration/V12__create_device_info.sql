CREATE TABLE device_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(255),
    email VARCHAR(255),
    check_counter INT DEFAULT 0,
    permanently_activated BOOLEAN DEFAULT FALSE,
    temporarily_activated BOOLEAN DEFAULT TRUE,
    country VARCHAR(255),
    subscription_until DATE
);
