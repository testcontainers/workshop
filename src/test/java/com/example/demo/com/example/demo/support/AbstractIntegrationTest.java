package com.example.demo.com.example.demo.support;

import com.github.terma.javaniotcpproxy.StaticTcpProxyConfig;
import com.github.terma.javaniotcpproxy.TcpProxy;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import net.bytebuddy.description.type.TypeList;
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
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.IOException;

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = {

//    "spring.datasource.url=jdbc:tc:postgresql:14-alpine:///"
  }
)
public class AbstractIntegrationTest {

  static Network network = Network.newNetwork();

  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
    "postgres:14-alpine").withReuse(true);
//    .withCopyFileToContainer(MountableFile.forClasspathResource(
//      "/schema.sql"), "/docker-entrypoint-initdb.d/"
//    );

  static GenericContainer redis = new GenericContainer("redis:6-alpine")
    .withExposedPorts(6379).withReuse(true).withNetwork(network).withNetworkAliases("redis");

  static ToxiproxyContainer toxiproxy =
    new ToxiproxyContainer("shopify/toxiproxy:2.1.0").withNetwork(network)
      .withNetworkAliases("toxiproxy");

  static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse(
    "confluentinc/cp-kafka:5.4.6"
  )).withReuse(true);

  static TcpProxy tcpProxy;


  @AfterAll
  public static void stopProxy() {
    tcpProxy.shutdown();
  }


  @DynamicPropertySource
  public static void setupThings(DynamicPropertyRegistry registry) throws IOException {
    Startables.deepStart(redis, kafka, postgres, toxiproxy).join();

    ToxiproxyContainer.ContainerProxy myRedis = toxiproxy.getProxy("redis", 6379);

    registry.add("spring.redis.host", myRedis::getContainerIpAddress);
    registry.add("spring.redis.port", myRedis::getProxyPort);

//    myRedis.toxics().latency("slowdown", ToxicDirection.DOWNSTREAM, 1000).setJitter(400);

    registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);

    StaticTcpProxyConfig config = new StaticTcpProxyConfig(
      5900,
      postgres.getHost(),
      postgres.getFirstMappedPort()
    );
    config.setWorkerCount(1);
    tcpProxy = new TcpProxy(config);
    tcpProxy.start();
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
