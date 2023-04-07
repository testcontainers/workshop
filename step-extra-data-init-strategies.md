# Step 9: Data initialization strategies

Initializing data using Spring is neat, but sometimes you might need alternative solutions. 

In this step we're going to turn off the Spring's database initialization, and explore how we can initialize the database using container specific configuration, followed by switching to using the [Flyway](https://flywaydb.org/) migrations.

## Assert the data is really there

To make the task run or fail faster, add a testcase to `DemoApplicationTest` which checks that the data from `schema.sql` is loaded into the database properly. 
For that you can `@Autowire` the `talks` repository into the test class and use it to verify that a talk with a given ID can be found in the database. 

```java
Assertions.assertTrue(talks.exists("testcontainers-integration-testing"));
```

## Running PostgreSQL explicitly

First of all, we'll remove the Testcontainers "modified JDBC URL" approach and create an explicit PostgreSQL container object to simplify further configuration.

In the `AbstractIntegratonTest` please remove `properties` from `@SpringBootTest(...)` altogether.

Then you can instantiate a PostgreSQL container using:

```java
PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine");
```

Additionally, we need to start `postgres` container similar to the other service dependencies and configure our application to use that containerized database which can be done by setting the following properties in the `@DynamicPropertySource` annotated method:

```text
spring.datasource.url
spring.datasource.username
spring.datasource.password
```

Use the values provided by the `postgres` object to fill the required configuration.

Running the test added in the beginning of this Step should pass now. 

## Initialize the DB without Spring

It might happen that loading `schema.sql` is not enough to fully initialize the database. 
We are going to simulate that by circumventing the Spring's convention. Please rename `schema.sql` to `talks-schema.sql`. 
The test should fail now since the schema isn't initialized in the database container and without it the app cannot function properly.  

Make the database initialization work again (and the test pass) by initializing the DB directly in the container.

### Hint
Most database containers have functionality to initialize the Database from the script files provided in the container.  
The PostgreSQL container happens to run all SQL files from `/docker-entrypoint-initdb.d/` directory, as described in the _Initialization scripts_  chapter of the [Postgres container](https://hub.docker.com/_/postgres/) docs.

Configure the `postgres` object using the `withCopyToContainer` method and `MountableFile.forClasspathResource(String path)` to configure the database schema.
After you initialize the DB correctly, the test should work again (despite _not_ having the `schema.sql` file).

## Migrating the DB with Flyway

Next, we're going to remove the data initialization queries from the `talks-schema.sql` file and use [Flyway](https://flywaydb.org/) for populating the DB with actual data.
Liquibase or other database migration tools would work similarly. 

Please add the Flyway dependency in `build.gradle`:

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

Next, move all the `INSERT ...` statements from the `talks-schema.sql` to `src/main/resources/db/migration/V1_1__talks.sql` file.

Note that the migrations file is not on the **test** classpath, as Flyway is likely to be used for production schema management as well. 

For Flyway not to complain that it can't store its data in the DB, we need to configure it to create its missing database management tables and data.

This can be done in `application.yml` with:

```yaml
  flyway:
    baseline-on-migrate: true
```

Note that `spring.flyway.locations=classpath:db/migration` is the default location for the migration files used by Flyway so we don't need to configure that explicitly. 
For more details on Spring Boot and Flyway integration please refer to [Spring manual](https://docs.spring.io/spring-boot/docs/2.6.7/reference/htmlsingle/#howto.data-initialization.migration-tool.flyway).

The test verifying that the data is correctly initialized in the Database should pass after we configure Flyway to run the migrations correctly.
