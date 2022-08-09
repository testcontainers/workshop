//package com.example.demo.com.example.demo.support;
//
//import com.example.demo.model.Rating;
//import io.restassured.RestAssured;
//import io.restassured.builder.RequestSpecBuilder;
//import io.restassured.filter.log.LogDetail;
//import io.restassured.specification.RequestSpecification;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.testcontainers.containers.DockerComposeContainer;
//import org.testcontainers.containers.wait.strategy.Wait;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.io.File;
//
//import static io.restassured.RestAssured.given;
//import static org.awaitility.Awaitility.await;
//import static org.hamcrest.Matchers.is;
//
//@Testcontainers
//public class DockerComposeApplicationTest  {
//
//  @Container
//  public static DockerComposeContainer composeContainer = new DockerComposeContainer(new File("compose.yaml"))
//    .withLocalCompose(true)
//    .withExposedService("app_1", 8080)
//    .waitingFor("app_1", Wait.forHttp("/actuator/health"));
//
//
//  protected RequestSpecification requestSpecification;
//
//  @BeforeEach
//  public void setUpAbstractIntegrationTest() {
//    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
//    requestSpecification = new RequestSpecBuilder()
//      .setBaseUri(String.format("http://%s:%d",
//        composeContainer.getServiceHost("app_1", 8080),
//        composeContainer.getServicePort("app_1", 8080)))
//      .addHeader(
//        HttpHeaders.CONTENT_TYPE,
//        MediaType.APPLICATION_JSON_VALUE
//      )
//      .build();
//  }
//
//  @Test
//  public void healthy() {
//    given(requestSpecification)
//      .when()
//      .get("/actuator/health")
//      .then()
//      .statusCode(200)
//      .log().ifValidationFails(LogDetail.ALL);
//  }
//
//  @Test
//  public void testRatings() {
//    String talkId = "testcontainers-integration-testing";
//
//    given(requestSpecification)
//      .body(new Rating(talkId, 5))
//      .when()
//      .post("/ratings")
//      .then()
//      .statusCode(202);
//
//    await().untilAsserted(() -> {
//      given(requestSpecification)
//        .queryParam("talkId", talkId)
//        .when()
//        .get("/ratings")
//        .then()
//        .body("5", is(1));
//    });
//
//    for (int i = 1; i <= 5; i++) {
//      given(requestSpecification)
//        .body(new Rating(talkId, i))
//        .when()
//        .post("/ratings");
//    }
//
//    await().untilAsserted(() -> {
//      given(requestSpecification)
//        .queryParam("talkId", talkId)
//        .when()
//        .get("/ratings")
//        .then()
//        .body("1", is(1))
//        .body("2", is(1))
//        .body("3", is(1))
//        .body("4", is(1))
//        .body("5", is(2));
//    });
//  }
//
//}