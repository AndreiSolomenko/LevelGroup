CREATE TABLE device_info (
    device_id VARCHAR(255) PRIMARY KEY,
    email VARCHAR(255),
    check_counter INT DEFAULT 0,
    permanently_activated BOOLEAN DEFAULT FALSE,
    temporarily_activated BOOLEAN DEFAULT TRUE
);
