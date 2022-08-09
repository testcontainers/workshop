package com.example.demo.com.example.demo.support;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.PushResponseItem;
import com.github.terma.javaniotcpproxy.StaticTcpProxyConfig;
import com.github.terma.javaniotcpproxy.TcpProxy;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import net.bytebuddy.description.type.TypeList;
import org.checkerframework.checker.units.qual.K;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.*;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {

  }
)
public class AbstractIntegrationTest {

  static Network network = Network.newNetwork();

  static GenericContainer<?> redis = new GenericContainer<>("redis:6-alpine")
    .withExposedPorts(6379).withNetwork(network).withNetworkAliases("redis");

  static PostgreSQLContainer<?> postgreSQLContainer =
    new PostgreSQLContainer<>("postgres:14-alpine")
      .withCopyFileToContainer(MountableFile.forClasspathResource("schema.sql"), "");

  static KafkaContainer kafka = new KafkaContainer(
    DockerImageName.parse("confluentinc/cp-kafka:5.4.6"));

  static ToxiproxyContainer toxyproxy = new ToxiproxyContainer(
    DockerImageName.parse("shopify/toxiproxy:2.1.0")).withNetwork(network);

  @DynamicPropertySource
  public static void setupThings(DynamicPropertyRegistry registry) throws IOException {
    Startables.deepStart(redis, kafka, postgreSQLContainer).join();

    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);

    registry.add("spring.redis.host", redis::getHost);
    registry.add("spring.redis.port", redis::getFirstMappedPort);

    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  static  TcpProxy tcpProxy;

  public static void createProxy() {
    StaticTcpProxyConfig config = new StaticTcpProxyConfig(
      5900,
      postgreSQLContainer.getHost(),
      postgreSQLContainer.getFirstMappedPort()
    );
    config.setWorkerCount(1);
    tcpProxy = new TcpProxy(config);
    tcpProxy.start();
  }

  @AfterAll
  public static void stopProxy() {
    tcpProxy.shutdown();
  }

  protected RequestSpecification requestSpecification;

  @LocalServerPort
  protected int localServerPort;

  @BeforeEach
  public void setUpAbstractIntegrationTest() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    requestSpecification = new RequestSpecBuilder()
      .setPort(localServerPort)
      .addHeader(
        HttpHeaders.CONTENT_TYPE,
        MediaType.APPLICATION_JSON_VALUE
      )
      .build();
  }


}
