# Step 10: Migrating from Docker Compose

We don't always encounter green field projects.
Maybe you are already invested some time in using Docker Compose to spin up your test environment and are wondering how to get started from here?

Let's look into how Testcontainers can support you on this journey.

## `Dockerfile` and `docker-compose.yml`

Let's assume we did start out with running our application as a Docker container as well, 
using the following, pretty standard, Dockerfile:

```Dockerfile
FROM openjdk:8-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

We also need to make sure the Spring-Boot jar has been built:

```bash
./gradlew bootJar
```

Finally, we have a Docker Compose file, that automatically builds the app image and spins it up, together with all dependencies:

```yaml
version: "2.4"
services:
  app:
    build: .
    environment:
      SPRING_REDIS_HOST: "redis"
      SPRING_REDIS_PORT: "6379"
      SPRING_KAFKA_BOOTSTRAP_SERVERS: "PLAINTEXT://kafka:9093"
      SPRING_DATASOURCE_URL: "jdbc:postgresql://db:5432/workshop"
      SPRING_DATASOURCE_USERNAME: "postgres"
      SPRING_DATASOURCE_PASSWORD: "example"
    ports:
      - "8080:8080"
  db:
    image: "postgres:14-alpine"
    environment:
      POSTGRES_PASSWORD: example
      POSTGRES_DB: workshop
    volumes:
      - "./src/test/resources/talks-schema.sql:/docker-entrypoint-initdb.d/schema.sql"
  redis:
    image: "redis:6-alpine"
  kafka:
    image: "confluentinc/cp-kafka:5.4.3"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: "1"
      KAFKA_OFFSETS_TOPIC_NUM_PARTITIONS: "1"
  zookeeper:
    image: confluentinc/cp-zookeeper:7.1.1
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000

```

We have a traditional JUnit Jupiter test `DockerComposeApplicationTest`, which assumes the application is running at `localhost:8080`:

```java
package com.example.demo;

import com.example.demo.model.Rating;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

public class DockerComposeApplicationTest  {

    protected RequestSpecification requestSpecification;

    @BeforeEach
    public void setUpAbstractIntegrationTest() {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpecification = new RequestSpecBuilder()
                .setPort(8080)
                .addHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    @Test
    public void healthy() {
        given(requestSpecification)
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .log().ifValidationFails(LogDetail.ALL);
    }

    @Test
    public void testRatings() {
        String talkId = "testcontainers-integration-testing";

        given(requestSpecification)
                .body(new Rating(talkId, 5))
                .when()
                .post("/ratings")
                .then()
                .statusCode(202);

        await().untilAsserted(() -> {
            given(requestSpecification)
                    .queryParam("talkId", talkId)
                    .when()
                    .get("/ratings")
                    .then()
                    .body("5", is(1));
        });

        for (int i = 1; i <= 5; i++) {
            given(requestSpecification)
                    .body(new Rating(talkId, i))
                    .when()
                    .post("/ratings");
        }

        await().untilAsserted(() -> {
            given(requestSpecification)
                    .queryParam("talkId", talkId)
                    .when()
                    .get("/ratings")
                    .then()
                    .body("1", is(1))
                    .body("2", is(1))
                    .body("3", is(1))
                    .body("4", is(1))
                    .body("5", is(2));
        });
    }

}
```

To run this rest, make sure the Docker Compose setup is running:
```bash
docker compose up
```

You can run the tests directly from the IDE.

Afterwards, you can stop the Docker Compose services again:
```bash
docker compose down -v
```

## Migrating to `DockerComposeContainer`

In order to tightly integrate the lifecycle of our test environment with the lifecycle of our tests,
we can already integrate Testcontainers and still make use of our existing `docker-compose.yml`:

```java
@Container
static DockerComposeContainer composeContainer = new DockerComposeContainer(new File("docker-compose.yml"))
        .withLocalCompose(true)
        .withExposedService("app_1", 8080)
        .waitingFor("app_1", Wait.forHttp("/actuator/health"));
```

You also need to add the `@Testcontainers` annotation to the test class, if you want the [Testcontainers-JUnit-Jupiter extension](https://www.testcontainers.org/test_framework_integration/junit_5/)
to manage the container lifecycle (similar to how we did in step 8).

Finally, make sure to configure RestAssured to access the dynamic port exposed by Testcontainers:

```java
requestSpecification = new RequestSpecBuilder()
    .setBaseUri(String.format("http://%s:%d", composeContainer.getHost(), composeContainer.getServicePort("app_1", 8080)))
    .addHeader(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_JSON_VALUE
    )
    .build();
```

Run the test from the IDE, it works! 
Note how you don't need to run `docker compose` before the test, or manually clean up the environment after.

## Migrating to individual Testcontainers objects

Instead of defining the necessary services in the `docker-compose.yml` file, we will now declare them as Java objects.
Furthermore, we make use of the Docker networking feature, so that we can hardcode connection URLs and leverage the 
Docker DNS features.

```java
static Network network = Network.newNetwork();

@Container
static final GenericContainer redis = new GenericContainer("redis:6-alpine")
        .withExposedPorts(6379)
        .withNetwork(network)
        .withNetworkAliases("redis");

@Container
static final KafkaContainer kafka = new KafkaContainer (
        DockerImageName.parse("confluentinc/cp-kafka:5.4.3"))
        .withNetwork(network)
        .withNetworkAliases("kafka");


@Container
static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
        .withCopyFileToContainer(MountableFile.forClasspathResource("/talks-schema.sql"), "/docker-entrypoint-initdb.d/")
        .withNetwork(network)
        .withNetworkAliases("db");
```

Testcontainers also allows to build images as part of the test execution and run the corresponding container.
We will use this for our Spring-Boot application:

```java
@Container
static final GenericContainer appContainer = new GenericContainer<>(
        new ImageFromDockerfile()
                .withFileFromPath("Dockerfile", Paths.get("Dockerfile"))
                .withFileFromPath("build/libs/workshop.jar", Paths.get("build/libs/workshop.jar"))
)
    .withExposedPorts(8080)
    .withEnv("SPRING_REDIS_HOST", "redis")
    .withEnv("SPRING_REDIS_PORT", "6379")
    .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "BROKER://kafka:9092")
    .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://db:5432/test")
    .withEnv("SPRING_DATASOURCE_USERNAME", "test")
    .withEnv("SPRING_DATASOURCE_PASSWORD", "test")
    .withNetwork(network)
    .waitingFor(Wait.forHttp("/actuator/health"))
    .dependsOn(redis, kafka, postgres);
```

Notice that we can also use the `dependsOn()` method, to control the startup order of our containers.
As compared to the `dependsOn` config in Docker Compose, this will fully utilize Testcontainers' `WaitStrategy` support,
to ensure the applications in the container are in a ready-to-use state.

Don't forget to configure RestAssured accordingly to use the `appContainer` details: 

```java
requestSpecification = new RequestSpecBuilder()
    .setBaseUri(String.format("http://%s:%d", appContainer.getHost(), appContainer.getFirstMappedPort()))
    .addHeader(
            HttpHeaders.CONTENT_TYPE,
            MediaType.APPLICATION_JSON_VALUE
    )
    .build();
```

Now let's run the test again.

## Moving back to `@SpringBootTest`

From this point, is just a small step to move our setup back to a `@SpringBootTest`.
But why would we want to do this?
Using `@SpringBootTest` bring a couple quality-of-life improvements for us as developers, such as faster feedback cycles
(we don't have to rebuild the whole application and the image) or much easier debugging of the Java process.

So let's make our test a `@SpringBootTest` again, by annotating it:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
```

We will also use the random local port:

```java
@LocalServerPort
protected int localServerPort;
```

And now we can use the `@DynamicPropertySource` method to comfortably configure the Spring-Boot application to use the containerized service dependencies.

```java
@DynamicPropertySource
public static void configureRedis(DynamicPropertyRegistry registry){
    Stream.of(redis,kafka,postgres).parallel().forEach(GenericContainer::start);
    registry.add("spring.redis.host",redis::getHost);
    registry.add("spring.redis.port",redis::getFirstMappedPort);
    registry.add("spring.kafka.bootstrap-servers",kafka::getBootstrapServers);
    registry.add("spring.datasource.url",postgres::getJdbcUrl);
    registry.add("spring.datasource.username",postgres::getUsername);
    registry.add("spring.datasource.password",postgres::getPassword);
}
```

Note that our test now looks very similar to the tests that we created when following the best practices of using Testcontainers from scratch.
