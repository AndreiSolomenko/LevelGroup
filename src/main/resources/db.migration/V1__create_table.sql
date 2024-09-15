-- Створення таблиці LGObjects
CREATE TABLE LGObject (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           internalId VARCHAR(255),
                           latitude VARCHAR(255),
                           longitude VARCHAR(255),
                           salesAgentName VARCHAR(255),
                           phones VARCHAR(255),
                           salesAgentEmail VARCHAR(255),
                           bathroomUnit VARCHAR(255)
);

-- Створення таблиці RiaLGObjects
CREATE TABLE RiaLGObject (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              localRealtyId VARCHAR(255),
                              realtyType VARCHAR(255),
                              advertType VARCHAR(255),
                              district VARCHAR(255),
                              metro VARCHAR(255),
                              street VARCHAR(255),
                              buildingNumber VARCHAR(255),
                              roomsCount VARCHAR(255),
                              totalArea DOUBLE,
                              kitchenArea DOUBLE,
                              floor VARCHAR(255),
                              floors VARCHAR(255),
                              price DOUBLE,
                              currency VARCHAR(255),
                              offerType VARCHAR(255),
                              newBuildingName VARCHAR(255),
                              description TEXT,
                              loc TEXT
);