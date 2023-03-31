# Step 9.5: Local Development environment with Testcontainers

Testcontainers is essential for creating ephemeral environments for testing your applications.
However, nothing in the API is specific to those conditions, and you can use it to programmatically manage any environment.

One of the more common scenarios is creating an environment for your application when you run it locally.
Some application frameworks integrate with Testcontainers to provide this functionality out of the box:

* Quarkus has [Dev Services](https://quarkus.io/guides/dev-services).
* Micronaut has [Test Resources](https://micronaut-projects.github.io/micronaut-test-resources/latest/guide/).

Spring Boot doesn't provide this functionality out of the box, but adding it to any individual project is reasonably straightforward.

## Extracting environment to a Context Initializer

This application requires configuring Postgres, Kafka, and Redis instances to run locally.
Our tests already have all the code to instantiate these services, configure them, and the application to use them.
However, for running the application locally, we need to refactor this code to be a part of the application initialization lifecycle.

We'll use a [Context Initializer](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/context/ApplicationContextInitializer.html) to encapsulate the environment creation.

Create a class `LocalEnvironmentInitializer` in the Test classpath that will implement the context initializer:

```java
class LocalEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  
    @Override
    public void initialize(ConfigurableApplicationContext context) {
      
    }
}
```

Now we'll use it to create the environment.
Extract the fields holding the container definitions from `AbstractIntegrationTest` to `LocalEnvironmentInitializer.`

Now use the `initialize` method to start the containers and inject information about the services they represent into the `ConfigurableApplicationContext` context.

The general structure of the `initialize` method could look like this:

```java
@Override
public void initialize(ConfigurableApplicationContext context) {
  var env = context.getEnvironment();
  env.getPropertySources().addFirst(new MapPropertySource(
      "testcontainers",
      (Map) getProperties()
  ));
}
```

Where `getProperties` returns a `Map` with the details of how to connect to our ephemeral services:

```java
Map.of(
    "spring.datasource.url", postgres.getJdbcUrl(),
    "spring.datasource.username", postgres.getUsername(),
    "spring.datasource.password", postgres.getPassword(),

    "spring.redis.host", redis.getContainerIpAddress(),
    "spring.redis.port", redis.getFirstMappedPort() + "",

    "spring.kafka.bootstrap-servers", kafka.getBootstrapServers()
);
```

## Running tests with the Context Initializer approach

Context Initializers can be used to configure any Spring content: both for tests and running the app.
This means you can replace the initialization of the containers in the `AbstractIntegrationTest` class (and the dynamic property source configuration) with the one-line config:

```java
@ContextConfiguration(initializers = LocalEnvironmentInitializer.class)
public class AbstractIntegrationTest {
  ...
}
```
You can check if the tests still pass normally.

## Running your application with the Context Initializer approach

The `LocalEnvironmentInitializer` class is on the Test classpath and unavailable to the application classpath.
This is by design because we don't want to make Testcontainers and other testing libraries available in the application classpath.

So to use the `LocalEnvironmentInitializer`, we need to create a separate entry point for running the application.
Create a `TestDemoApplication` class, that looks exactly like `DemoApplication` but uses the Context Initializer we implemented above: 

```java
@SpringBootApplication
public class TestDemoApplication {

  public static void main(String[] args) {
    SpringApplication springApplication = new SpringApplication(DemoApplication.class);

    springApplication.addInitializers(new AbstractIntegrationTest.Initializer());
    springApplication.run(args);
  }
}
```

Running this class should run, and you should see from the logs that the application is started successfully.
Stopping the application will remove the containers just like Testcontainers cleans up the environment after running the tests.


## Hint 1:
One way to make the current setup more flexible is to make container creation and configuring the app conditional on not having the service defined in the existing application configuration.
For example, something like:
```
if(getProperty("spring.datasource.url") != null)
```
can be used to conditionally create an ephemeral database with Testcontainers.

## Hint 2:
Local development environments can experience a lot of start/stop cycles: for example, when you change a line of code and want to see it in action.
Try the [Reusable containers mode](https://www.testcontainers.org/features/reuse/) to avoid stopping the containers on application stop and speeding up application starts. 
