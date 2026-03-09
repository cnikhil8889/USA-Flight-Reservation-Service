CREATE TABLE IF NOT EXISTS flights (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flight_number VARCHAR(255) NOT NULL UNIQUE,
    origin VARCHAR(255) NOT NULL,
    destination VARCHAR(255) NOT NULL,
    departure_time DATETIME NOT NULL,
    arrival_time DATETIME NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    available_seats INT NOT NULL,
    total_seats INT NOT NULL
);

CREATE TABLE IF NOT EXISTS passengers (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(32),
    passport_number VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_code VARCHAR(255) UNIQUE,
    flight_id BIGINT NOT NULL,
    passenger_id BIGINT NOT NULL,
    booking_date DATETIME NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(32),
    baggage_weight DOUBLE,
    baggage_type VARCHAR(32),
    CONSTRAINT fk_reservation_flight FOREIGN KEY (flight_id) REFERENCES flights (id),
    CONSTRAINT fk_reservation_passenger FOREIGN KEY (passenger_id) REFERENCES passengers (id)
);

CREATE TABLE IF NOT EXISTS baggage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    weight DOUBLE NOT NULL,
    type VARCHAR(32) NOT NULL,
    charge DECIMAL(19,2) NOT NULL,
    description VARCHAR(1024)
);

