package com.example.demo.com.example.demo.support;


import com.example.demo.model.Rating;
import com.example.demo.repository.TalksRepository;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.testcontainers.containers.ToxiproxyContainer;

import java.util.Scanner;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;

public class DemoApplicationTest extends AbstractIntegrationTest {

  @Autowired
  TalksRepository talksRepository;

  @Test
  public void contextLoads() throws Exception {
    assertThat(talksRepository.exists("testcontainers-is-amazing")).isTrue();
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
    String talkId = "testcontainers-is-amazing";

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
        .body("5", greaterThanOrEqualTo(1));
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
        .body("1", greaterThanOrEqualTo(1))
        .body("2", greaterThanOrEqualTo(1))
        .body("3", greaterThanOrEqualTo(1))
        .body("4", greaterThanOrEqualTo(1))
        .body("5", greaterThanOrEqualTo(1));
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
