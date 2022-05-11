INSERT
INTO talks (id, title)
VALUES ('Testcontainers is amazing!', 'Testcontainers is amazing!')
    ON CONFLICT do nothing;