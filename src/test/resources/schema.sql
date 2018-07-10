CREATE TABLE IF NOT EXISTS talks(
  id    VARCHAR(32)  NOT NULL,
  title VARCHAR(255) NOT NULL,
  PRIMARY KEY (id)
);

INSERT
  INTO talks (id, title)
  VALUES ('welcome-to-junit-5', 'JUnit 5.2 actually :)')
  ON CONFLICT do nothing;

INSERT
  INTO talks (id, title)
  VALUES ('flight-of-the-flux', 'A look at Reactor execution model')
  ON CONFLICT do nothing;