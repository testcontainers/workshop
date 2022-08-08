CREATE TABLE IF NOT EXISTS talks(
                                    id    VARCHAR(64)  NOT NULL,
                                    title VARCHAR(255) NOT NULL,
                                    PRIMARY KEY (id)
);

INSERT
INTO talks (id, title)
VALUES ('testcontainers-integration-testing', 'Modern Integration Testing with Testcontainers')
ON CONFLICT do nothing;

INSERT
INTO talks (id, title)
VALUES ('testcontainers-is-amazing', 'It really is!')
ON CONFLICT do nothing;