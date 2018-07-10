# Adding some tests

The app doesn't have any tests yet. But before we write our first test, let's create an abstract test class for the common things between the tests.

## Abstract class
Add `com.example.demo.support.AbstractIntegrationTest` class to `src/main/test` sourceset. It should be an abstract class with standard Spring Boot's testing infrastructure annotations on it:
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

## Populate the database
The context starts. However, we need to populate the DB with some data before we can write the tests.

Let's add `src/test/resources/schema.sql` file with the following content:
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
```
Caused by: org.h2.jdbc.JdbcSQLException: Syntax error in SQL statement "INSERT INTO TALKS (ID, TITLE) VALUES ('welcome-to-junit-5', 'JUnit 5.2 actually :)') ON[*] CONFLICT DO NOTHING";
```

It seems that H2 does not support PostgreSQL syntax, at least not by default.
