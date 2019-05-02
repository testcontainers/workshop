# Step 9: Selenium Tests

Let's do a complete blackbox system test this time, interacting with our application using a real Browser instrumented by Selenium.

Again Testcontainers has you covered, by providing the `BrowserWebDriverContainer` class.

This time, we'll create a test class `com.example.demo.SystemTest` in the `acceptanceTest` source directory. Here we'll setup our complete system and also our application in a container. We'll also be able to use Testcontainer's networking feature, so all containers are able to speak to each other using the Docker network and their DNS names.

The `BrowserWebDriverContainer` has a nice feature, allowing you to save everything thas happens a a video.

```java
package com.example.demo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.*;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.util.stream.Stream;

public class SystemTest {

    private static Network net = Network.newNetwork();

    private static PostgreSQLContainer postgres = new PostgreSQLContainer<>("postgres:10-alpine")
            .withDatabaseName("spring")
            .withUsername("user")
            .withPassword("secret")
            .withNetwork(net)
            .withNetworkAliases("postgres");

    private static GenericContainer redis = new GenericContainer("redis:3-alpine")
            .withExposedPorts(6379)
            .withNetwork(net)
            .withNetworkAliases("redis");

    private static GenericContainer sut = new GenericContainer("openjdk:8-jre-alpine")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/app.jar"), "/")
            .withCommand("java", "-jar", "/app.jar")
            .withNetwork(net)
            .withNetworkAliases("sut")
            .withExposedPorts(8080) // needed for WaitStrategy
            .waitingFor(Wait.forHttp("/actuator/health"))
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres/spring")
            .withEnv("SPRING_DATASOURCE_USERNAME", "user")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "secret")
            .withEnv("SPRING_KAFKA_BOOTSTRAP-SERVERS", "kafka:9092")
            .withEnv("SPRING_REDIS_HOST", "redis")
            .withEnv("SPRING_REDIS_PORT", "6379");

    private static KafkaContainer kafka = new KafkaContainer()
            .withEmbeddedZookeeper()
            .withNetwork(net)
            .withNetworkAliases("kafka");

    @ClassRule
    private static BrowserWebDriverContainer browser = new BrowserWebDriverContainer<>()
            .withCapabilities(DesiredCapabilities.chrome())
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, new File("build"))
            .withNetwork(net);

    @BeforeClass
    public static void setup() {
        Stream.of(redis, sut, postgres, kafka).parallel().forEach(GenericContainer::start);
    }

    @AfterClass
    public static void tearDown() {
        Stream.of(redis, sut, postgres, kafka).parallel().forEach(GenericContainer::stop);
    }

    @Test
    public void seleniumTest() {
        RemoteWebDriver webDriver = browser.getWebDriver();

        webDriver.get("http://sut:8080/view/ratings/");
        WebElement content = webDriver.findElementById("content");

        assert content.getText().equals("Foobar");
    }

}
```

