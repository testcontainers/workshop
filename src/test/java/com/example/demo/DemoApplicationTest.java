package com.example.demo;

import com.example.demo.repository.TalksRepository;
import com.example.demo.support.AbstractIntegrationTest;
import io.restassured.filter.log.LogDetail;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static io.restassured.RestAssured.given;

public class DemoApplicationTest extends AbstractIntegrationTest {

    @Autowired
    TalksRepository talks;

    @Test
    public void contextLoads() {
        Assertions.assertTrue(talks.exists("testcontainers-integration-testing"));
        Assertions.assertTrue(talks.exists("flight-of-the-flux"));

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

}
