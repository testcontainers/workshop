# Step 3: Adding some tests

The app doesn't have any tests yet. But before we write our first test, let's create an abstract test class for the things which are common between the tests.

## Abstract class

Add `com.example.demo.support.AbstractIntegrationTest` class to `src/test/java` sourceset. It should be an abstract class with standard Spring Boot's testing framework annotations on it:

```java
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
```

## Our very first test

Now we need to test that the context starts.  
Add `com.example.demo.DemoApplicationTest`, extend it from your base class you just created and add a dummy test:

```java
@Test
public void contextLoads() {
}
```

Run it and verify that it starts. Spring will detect H2 on classpath and use an embedded DB.

This is actually already a useful smoke tests since it ensures, that Spring Boot is able to initialize the application context successfully.

## Populate the database

The context starts. However, we need to populate the DB with some data before we can write the tests.

Let's add a `src/test/resources/schema.sql` file with the following content:

```sql
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
```

Now run the test again. Oh no, it fails!

```text
Caused by: org.h2.jdbc.JdbcSQLException: Syntax error in SQL statement "INSERT INTO TALKS (ID, TITLE) VALUES ('welcome-to-junit-5', 'JUnit 5.2 actually :)') ON[*] CONFLICT DO NOTHING";
```

It seems that H2 does not support the PostgreSQL SQL syntax, at least not by default.

