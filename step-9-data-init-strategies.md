# Step 9: Data initialization strategies

Initializing data using Spring is neat, only sometimes it doesn't solve issues we have the way we need to be solved.

In this step we're going to turn off Spring's DB initialization, and turn into manual one, followed by switching to Flyway migrations.

## Assert the data is really there

To make the task run (or fail ;-) ) faster, in the `DemoApplicationTest` add a testcase, to check that the data from `schema.sql` is loaded properly. We can `@Autowire` `talks` repository for that for example.

```java
Assertions.assertTrue(talks.exists("testcontainers-integration-testing"));
```

## Run PostgreSQL explicitly

Now we'll make the PostgreSQL container start explicitly.
In the `AbstractIntegratonTest` please remove `properties` from `@SpringBootTest(...)` altogether.

The test should pass after this operation.

### Hint 1

You may wish to instantiate PostgreSQL container using:

```java
PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");
```

### Hint 2

Setting the following properties in registry might help:
```text
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

## Initialize the DB without Spring

It might happen (due to whatever reason) that we might not be able to initialize our DB with Spring's `schema.sql`.

Please rename the `schema.sql` to `talks-schema.sql`.

The test should fail now.

Please make the initialization work again (and the test pass) by initializing the DB directly in the container.

### Hint

PostgreSQL container, when starting, happens to run all SQL files from `/docker-entrypoint-initdb.d` directory, as described in _Initialization scripts_  chapter of [Postgres container](https://hub.docker.com/_/postgres/).

After initializing the DB correctly, the test should work again (despite _not_ having `schema.sql` file).

## Migrating the DB with Flyway

Now we're going to remove data initialization from `talks-schema.sql` file and use Flyway for populating the DB with actual data.

Please add dependency in `build.gradle`:

```text
implementation 'org.flywaydb:flyway-core'
```

or `pom.xml`:
```text
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

Next, move all the `INSERT ...` statements from the `talks-schema.sql` to `src/main/resources/migrations/V1_1__talks.sql` files.

Tune the `@SpringBootTest` to make it Flyway aware:
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.locations=classpath:/migrations",
                "spring.flyway.baselineOnMigrate=true",
        }
)
```
Please refer to [Spring manual](https://docs.spring.io/spring-boot/docs/2.6.2/reference/htmlsingle/#howto.data-initialization.migration-tool.flyway) for more details.

The test should pass after that.
