CREATE TABLE device_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    device_id VARCHAR(255),
    registration_time TIMESTAMP,
    email VARCHAR(255),
    check_counter INT DEFAULT 0,
    permanently_activated BOOLEAN DEFAULT FALSE,
    temporarily_activated BOOLEAN,
    blocked_at TIMESTAMP,
    country VARCHAR(255),
    subscription_until DATE,
    activated_at TIMESTAMP,
    country_allowed BOOLEAN DEFAULT FALSE
);
