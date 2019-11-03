# Step 7: Test the API

Now let's create a real test for our API which will verify the business logic.

```java
package com.example.demo.api;

import com.example.demo.model.Rating;
import com.example.demo.support.AbstractIntegrationTest;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

public class RatingsControllerTest extends AbstractIntegrationTest {

    @Test
    public void testRatings() {
        String talkId = "welcome-to-junit-5";

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

    @Test
    public void testUnknownTalk() {
        String talkId = "cdi-the-great-parts";

        given(requestSpecification)
                .body(new Rating(talkId, 5))
                .when()
                .post("/ratings")
                .then()
                .statusCode(404);
    }
}
```

Run it, and it will fail. Why? There is no Kafka!

But running Kafka in Docker is challenging and not as simple as doing `new GenericContainer()`. But don't worry, this time there is `KafkaContainer` from Testcontainers.

Just add it the same way as you added Redis and set the `spring.kafka.bootstrap-servers` system property.

## Hint 1:

Some containers expose helper methods. Check if there is one on `KafkaContainer` which might help you.

## Hint 2:

You can start a few containers in parallel by doing:

```java
Stream.of(redis, kafka).parallel().forEach(GenericContainer::start);
```

