# Step 11: Using Testcontainers for Chaos Engineering

So far we have tested our system under very expected conditions.
But in reality, we know that things can go wrong.
The network can be slow, the database can be unavailable, and so on.

In this step, we will use Testcontainers to simulate such conditions and see how our system behaves.

For this, we will use the [Toxiproxy](https://www.testcontainers.org/modules/toxiproxy/) module.

Check out the documentation and write a test that checks the following test scenario:
1. Initially, PostgreSQL is available, and we can record a rating.
2. We then simulate a network outage between our application and PostgreSQL using Toxiproxy, and we expect the endpoint to return an error.
3. We then simulate a network recovery, and we expect the endpoint to return a success.

## Hint

You need to add the Toxiproxy module to your project's dependencies.
You also need a Toxiproxy client, such as:
```groovy
testImplementation("eu.rekawek.toxiproxy:toxiproxy-java:2.1.0")
```