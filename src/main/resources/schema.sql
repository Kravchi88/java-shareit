DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS items;
DROP TABLE IF EXISTS requests;
DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users (
    users_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    users_name VARCHAR(255) NOT NULL,
    users_email VARCHAR(512) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (users_id),
    CONSTRAINT uq_users_email UNIQUE (users_email)
);

CREATE TABLE IF NOT EXISTS requests (
    requests_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    requests_description TEXT NOT NULL,
    requests_requestor_id BIGINT NOT NULL,
    requests_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_requests PRIMARY KEY (requests_id),
    CONSTRAINT fk_requests_requestor FOREIGN KEY (requests_requestor_id) REFERENCES users(users_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS items (
    items_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    items_name VARCHAR(255) NOT NULL,
    items_description TEXT,
    items_is_available BOOLEAN NOT NULL,
    items_owner_id BIGINT NOT NULL,
    items_request_id BIGINT,
    CONSTRAINT pk_items PRIMARY KEY (items_id),
    CONSTRAINT fk_items_owner FOREIGN KEY (items_owner_id) REFERENCES users(users_id) ON DELETE CASCADE,
    CONSTRAINT fk_items_request FOREIGN KEY (items_request_id) REFERENCES requests(requests_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    bookings_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    bookings_start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    bookings_end_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    bookings_item_id BIGINT NOT NULL,
    bookings_booker_id BIGINT NOT NULL,
    bookings_status VARCHAR(10) NOT NULL,
    CONSTRAINT pk_bookings PRIMARY KEY (bookings_id),
    CONSTRAINT fk_bookings_item FOREIGN KEY (bookings_item_id) REFERENCES items(items_id) ON DELETE CASCADE,
    CONSTRAINT fk_bookings_booker FOREIGN KEY (bookings_booker_id) REFERENCES users(users_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS comments (
    comments_id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    comments_text TEXT NOT NULL,
    comments_item_id BIGINT NOT NULL,
    comments_author_id BIGINT NOT NULL,
    comments_created TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_comments PRIMARY KEY (comments_id),
    CONSTRAINT fk_comments_item FOREIGN KEY (comments_item_id) REFERENCES items(items_id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_author FOREIGN KEY (comments_author_id) REFERENCES users(users_id) ON DELETE CASCADE
);