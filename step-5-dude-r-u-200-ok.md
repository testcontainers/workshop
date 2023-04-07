# Step 5: Hello, r u 200 OK?

One of the great features of Spring Boot is the Actuator and its health endpoint. 
It gives you an overview how healthy your app is.

The context starts, but what's about the health of the app?

## Configure Rest Assured

To check the health endpoint of our app, we will use the [RestAssured](http://rest-assured.io/) library.

However, before using it, we first need to configure it. 
Add the following to your abstract test class since we will share it between all tests:

```java
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
```

Here we ask Spring Boot to inject the random port it received at the start of the app, so that we can pre-configure RestAssured's requestSpecification.

## Call the endpoint

Now let's check if the app is actually healthy by doing the following in our `DemoApplicationTest`:

```java
@Test
public void healthy() {
    given(requestSpecification)
            .when()
            .get("/actuator/health")
            .then()
            .statusCode(200)
            .log().ifValidationFails(LogDetail.ALL);
}
```

If we run the test, it will fail:

```text
...
HTTP/1.1 503 Service Unavailable
transfer-encoding: chunked
Content-Type: application/vnd.spring-boot.actuator.v2+json;charset=UTF-8

{
    "status": "DOWN",
    "details": {
        "diskSpace": { ... },
        "redis": {
            "status": "DOWN",
            "details": {
                "error": "org.springframework.data.redis.RedisConnectionFailureException: Unable to connect to Redis; nested exception is io.lettuce.core.RedisConnectionException: Unable to connect to localhost:6379"
            }
        },
        "db": {
            "status": "UP",
            "details": {
                "database": "PostgreSQL",
                "hello": 1
            }
        }
    }
}
... 
Expected status code <200> but was <503>.
```

It seems that it couldn't find Redis and there is no autoconfigurable option for it.

