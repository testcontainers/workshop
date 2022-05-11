INSERT
INTO talks (id, title)
VALUES ('flight-of-the-flux', 'A look at Reactor execution model')
ON CONFLICT do nothing;

INSERT
INTO talks (id, title)
VALUES ('testcontainers-integration-testing', 'Modern Integration Testing with Testcontainers')
ON CONFLICT do nothing;