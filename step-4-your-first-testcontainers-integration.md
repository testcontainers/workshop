# Step 4: Your first Testcontainers integration

From the Testcontainers website, we learn that there is a simple way of running different supported JDBC databases with Docker:  
[https://www.testcontainers.org/usage/database\_containers.html](https://www.testcontainers.org/usage/database_containers.html)

An especially interesting part are JDBC-URL based containers:  
[https://www.testcontainers.org/usage/database\_containers.html\#jdbc-url](https://www.testcontainers.org/usage/database_containers.html#jdbc-url)

It means that starting to use Testcontainers in our project \(once we add a dependency\) is as simple as changing a few properties in Spring Boot:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
 "spring.datasource.url=jdbc:tc:postgresql:14-alpine://testcontainers/workshop"
})
```

If we split the magical JDBC url, we see:

* `jdbc:tc:` - this part says that we should use Testcontainers as JDBC provider
* `postgresql:14-alpine://` - we use a PostgreSQL database, and we select the correct PostgreSQL image from the Docker Hub as the image
* `testcontainers/workshop` - the host name \(can be anything\) is `testcontainers` and the database name is `workshop`. Your choice!

After adding the properties and run the test again. Fixed? Good!

Check the logs.

```text
                   
    12:26:58.658  INFO 32972 --- [    Test worker] o.t.d.DockerClientProviderStrategy       : Found Docker environment with Environment variables, system properties and defaults. Resolved dockerHost=tcp://127.0.0.1:56956
    12:26:58.660  INFO 32972 --- [    Test worker] org.testcontainers.DockerClientFactory   : Docker host IP address is 127.0.0.1
    12:26:58.784  INFO 32972 --- [    Test worker] org.testcontainers.DockerClientFactory   : Connected to docker:
      Server Version: 66+testcontainerscloud
      API Version: 1.41t com.example.demo.DemoApplicationTest
      Operating System: Ubuntu 20.04.3 LTS
      Total Memory: 11149 MB
    12:26:58.790  INFO 32972 --- [    Test worker] o.t.utility.ImageNameSubstitutor         : Image name substitution will be performed by: DefaultImageNameSubstitutor (composite of 'ConfigurationFileImageNameSubstitutor' and 'PrefixingImageNameSubstitutor')
    12:26:59.973  INFO 32972 --- [    Test worker] org.testcontainers.DockerClientFactory   : Ryuk started - will monitor and terminate Testcontainers containers on JVM exit :test > 0 tests completed
    12:26:59.974  INFO 32972 --- [    Test worker] org.testcontainers.DockerClientFactory   : Checking the system...
    12:26:59.975  INFO 32972 --- [    Test worker] org.testcontainers.DockerClientFactory   : ✔️ Docker server version should be at least 1.6.0
    12:27:00.244  INFO 32972 --- [    Test worker] org.testcontainers.DockerClientFactory   : ✔️ Docker environment should have more than 2GB free disk space

```

As you can see, Testcontainers quickly discovered your environment and connected to Docker. 
It did some pre-flight checks as well to ensure that you have a valid environment.

## Hint 1:

Add the following line to your `~/.testcontainers.properties` file to disable these checks and speed up the tests:

```text
checks.disable=true
```

## Hint 2:

Changing the PostgreSQL version is as simple as replacing `14-alpine` with, for example, `10-alpine`.
Try it, but don't forget that it will download the new image from the internet, if it's not already present on your computer.

