package com.example.demo;

import com.example.demo.model.Rating;
import com.example.demo.repository.TalksRepository;
import io.restassured.filter.log.LogDetail;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;

public class DemoApplicationTest extends AbstractIntegrationTest {

  @Autowired
  TalksRepository talksRepository;

  @Test
  public void contextLoads() {
    Assertions.assertThat(talksRepository.exists("testcontainers-integration-testing")).isTrue();
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
