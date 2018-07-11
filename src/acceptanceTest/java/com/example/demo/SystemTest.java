package com.example.demo;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.util.stream.Stream;

public class SystemTest {

    private static Network net = Network.newNetwork();

    private static PostgreSQLContainer postgres = (PostgreSQLContainer) new PostgreSQLContainer()
            .withDatabaseName("spring")
            .withUsername("user")
            .withPassword("secret")
            .withNetwork(net)
            .withNetworkAliases("postgres");

    private static GenericContainer redis = new GenericContainer("redis:3-alpine")
            .withExposedPorts(6379)
            .withNetwork(net)
            .withNetworkAliases("redis");

    private static GenericContainer sut = new GenericContainer("java:8-jre-alpine")
            .withCopyFileToContainer(MountableFile.forClasspathResource("/app.jar"), "/")
            .withCommand("java", "-jar", "/app.jar")
            .withNetwork(net)
            .withNetworkAliases("sut")
            .withExposedPorts(8080) // needed for WaitStrategy
            .waitingFor(Wait.forHttp("/actuator/health"))
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres/spring")
            .withEnv("SPRING_DATASOURCE_USERNAME", "user")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "secret")
            .withEnv("SPRING_REDIS_HOST", "redis")
            .withEnv("SPRING_REDIS_PORT", "6379");

    @ClassRule
    public static BrowserWebDriverContainer browser = (BrowserWebDriverContainer) new BrowserWebDriverContainer()
            .withDesiredCapabilities(DesiredCapabilities.chrome())
            .withRecordingMode(BrowserWebDriverContainer.VncRecordingMode.RECORD_ALL, new File("build"))
            .withNetwork(net);

    @BeforeClass
    public static void setup() {
        Stream.of(redis, sut, postgres).parallel().forEach(GenericContainer::start);
    }

    @AfterClass
    public static void tearDown() {
        Stream.of(redis, sut, postgres).parallel().forEach(GenericContainer::stop);
    }

    @Test
    public void seleniumTest() {
        RemoteWebDriver webDriver = browser.getWebDriver();

        webDriver.get("http://sut:8080/view/ratings/");
        WebElement content = webDriver.findElementById("content");

        assert content.getText().equals("Foobar");
    }

}
