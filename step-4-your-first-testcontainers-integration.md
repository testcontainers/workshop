# Step 4: Your first Testcontainers integration

From the Testcontainers website, we learn that there is a simply way of running different supported JDBC databases with Docker:  
[https://www.testcontainers.org/usage/database\_containers.html](https://www.testcontainers.org/usage/database_containers.html)

An especially interesting part are JDBC-URL based containers:  
[https://www.testcontainers.org/usage/database\_containers.html\#jdbc-url](https://www.testcontainers.org/usage/database_containers.html#jdbc-url)

It means that starting to use Testcontainers in our project \(once we add a dependency\) is as simple as changing a few properties in Spring Boot:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, properties = {
 "spring.datasource.url=jdbc:tc:postgresql:10-alpine://testcontainers/workshop",
 "spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver"
})
```

If we split the magical JDBC url, we get:

* `jdbc:tc:` - this part says that we should use Testcontainers as JDBC provider
* `postgresql:10-alpine://` - we use a PostgreSQL database, and we select PostgreSQL 10 from the Docker Hub as the image
* `testcontainers/workshop` - the host name \(can be anything\) is `testcontainers` and the database name is `workshop`. Your choice!

Add the following properties and run the test again. Fixed? Good!

Check the logs.

```text
2018-05-25 16:40:52.897  INFO   --- [    Test worker] o.t.d.UnixSocketClientProviderStrategy   : Accessing docker with local Unix socket
2018-05-25 16:40:52.898  INFO   --- [    Test worker] o.t.d.DockerClientProviderStrategy       : Found Docker environment with local Unix socket (unix:///var/run/docker.sock)
2018-05-25 16:40:52.899  INFO   --- [    Test worker] org.testcontainers.DockerClientFactory   : Docker host IP address is localhost
2018-05-25 16:40:53.137  INFO   --- [    Test worker] org.testcontainers.DockerClientFactory   : Connected to docker: 
  Server Version: 18.03.1-ce
  API Version: 1.37
  Operating System: Docker for Mac
  Total Memory: 4948 MB
2018-05-25 16:40:54.471  INFO   --- [    Test worker] org.testcontainers.DockerClientFactory   : Ryuk started - will monitor and terminate Testcontainers containers on JVM exit
        ℹ︎ Checking the system...
        ✔ Docker version should be at least 1.6.0
        ✔ Docker environment should have more than 2GB free disk space
        ✔ File should be mountable
```

As you can see, Testcontainers quickly discovered your environment and connected to Docker. It did some pre-flight checks as well to ensure that you have a valid environment.

## Hint 1:

Add the following line to your `~/.testcontainers.properties` file to disable these checks and speed up the tests:

```text
checks.disable=true
```

## Hint 2:

Changing the PostgreSQL version is as simple as replacing `10-alpine` with, for example, `9-alpine`.

Try it, but don't forget that it will download the image from the internet, if it's not already present on your computer.

