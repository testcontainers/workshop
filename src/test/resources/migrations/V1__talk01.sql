INSERT
INTO talks (id, title)
VALUES ('testcontainers-integration-testing', 'Modern Integration Testing with Testcontainers')
    ON CONFLICT do nothing;