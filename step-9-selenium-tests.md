# Step 9: Selenium Tests

Let's do a complete end-to-end test this time, interacting with our application using a real Browser instrumented by Selenium.

Again Testcontainers has you covered, by providing the `BrowserWebDriverContainer` class. This class acts as a facade for a  container that includes a dockerized browser Chrome/Firefox, plus a Selenium and VNC server. 
We can control this container via the standard Selenium `RemoteWebDriver` API.

## Getting started

This time, we'll create a test class `com.example.demo.EndToEndTest` in the `acceptanceTest` source directory:


```java
public class EndToEndTest extends AbstractIntegrationTest {

    private BrowserWebDriverContainer browser;

    @Before
    public void setUp() {
        // TODO: add something here

        browser = new BrowserWebDriverContainer<>()
                .withCapabilities(new ChromeOptions());
        browser.start();
    }

    @Test
    public void seleniumTest() {
        RemoteWebDriver webDriver = browser.getWebDriver();

        webDriver.get("http://" + /* TODO: replace me */ + "/view/ratings/");
        WebElement content = webDriver.findElementById("content");

        assertEquals(content.getText(), "Foobar");
    }

    @After
    public void tearDown() {
        browser.stop();
    }
}
```

## Component structure

With the addition of this test code on top of `AbstractIntegrationTest`, we now have the following arrangement of components in our test:


![Component structure diagram](https://kroki.io/graphviz/svg/eNp9UU1rAkEMvfdXhL1WD_VaVBBBUISitheRMjvGdXA6kSTbHor_vbt1dUddzW1e8j6SWbuMzX4LI_h9gqIkT4-A9bko8udL1SjLmxR9Nxl_TJPXM5jM9-xCBgMihTnyt7MYt8fvwSkoikoFH5qdOrdOi4JlKahxAVlgSHaHDDUS-byRaMYoNTIxm52pnzNcO4ljo8fg8q_nAdOPxLIXMRvXg3bvyvDuWJTi7kwU7eJcZe9RTlhWdyohJp-sTkYPOP-aTUGWljxxN_U5tk4f0D9L3uZq1DjyRA2rFNTDH-PTt5w=)
<!--
Regenerate if necessary at https://kroki.io/
Source:

digraph G {
    subgraph cluster_1 {
        label="JVM";
        "Spring Boot Service";
        "JUnit tests";
    }
    subgraph cluster_2 {
        label="Testcontainers Docker containers";
        Postgres;
        Kafka;
        Redis;
        "Selenium+Browser container";
    }
    "Spring Boot Service" -> Postgres;
    "Spring Boot Service" -> Kafka;
    "Spring Boot Service" -> Redis;
    "JUnit tests" -> "Selenium+Browser container" [label="control"];
    "Selenium+Browser container" -> "Spring Boot Service" [color=blue, label="?"];
    "JUnit tests" -> "Spring Boot Service" [label=starts];
}
-->

## Talking to our service

One final piece of the puzzle is missing, though: how can we get our dockerized web browser to communicate with the Spring Boot server running inside our local JVM? (the blue line in the diagram).

Normally Docker containers cannot access the host machine, but Testcontainers offers a mechanism to expose host ports.
Have a look at the documentation, and try and find out:

* how to expose a host port to containers
* once a host port is exposed, what URL should we tell the browser to access? (hint: it's not `localhost`)

With this information, update the TODOs in the test class and see what happens.

## Extra credit

* The `BrowserWebDriverContainer` has a nice feature that allows test runs to be saved to a video, either for all tests or just failing tests. Check the documentation, and give this a try.
* `BrowserWebDriverContainer` exposes a `getVncAddress()` method. Can you use this on your local machine to watch tests as they run via VNC? Even better, try and use the browser's inspect tools while stopped on a breakpoint in your tests!
* You can find some additional ideas [here](https://bsideup.github.io/posts/debugging_containers/) on how to use a fixed port for local development and debugging, leveraging a tcp-proxy.
