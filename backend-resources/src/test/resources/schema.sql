CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(100) NOT NULL,
                       first_name VARCHAR(100),
                       last_name VARCHAR(100),
                       email VARCHAR(100) NOT NULL UNIQUE,
                       role VARCHAR(50) NOT NULL
);

CREATE TABLE roles (
                       id SERIAL PRIMARY KEY,
                       role_name VARCHAR(50) NOT NULL
);

CREATE TABLE user_roles (
                            user_id UUID REFERENCES users(id),
                            role_id INTEGER REFERENCES roles(id)
);

INSERT INTO roles (role_name) VALUES ('MODERATOR');
INSERT INTO users (id, username, first_name, last_name, email, role)
VALUES ('b6a5d8e0-d48d-11eb-b8bc-0242ac130003', 'johndoe', 'John', 'Doe', 'john.doe@example.com', 'MODERATOR');