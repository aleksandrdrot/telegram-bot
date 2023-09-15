-- liquibase formatted sql

-- changeset adrot:1
CREATE TABLE notification_task(
                         id INTEGER PRIMARY KEY,
                         dateTime TEXT,
                         comment TEXT
);
