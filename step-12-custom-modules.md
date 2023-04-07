# Step 12: Custom Modules

Testcontainers provides a range of extension points to write your own modules 
and you are not limited to the modules that are provided out of the box.
Such a container module can be used to encapsulate the logic of starting a container for a specific service,
and configuring it accordingly.

Writing a custom container module is normally done by creating a new class that extends `GenericContainer`.

And although using the `GenericContainer` for Redis is perfectly fine, we want to get a feeling for writing a custom module,
by writing a `RedisContainer` that extends `GenericContainer` and has the corresponding configuration applied.

What about a helper method, returning a Redis URI?
```
redis :// [[username :] password@] host [:port][/database]
          [?[timeout=timeout[d|h|m|s|ms|us|ns]]
```

We also want to make sure, a user can't use the container accidentally with a wrong Docker image.
For this, you can make use of the `dockerImage.assertCompatibleWith(compatibleImageName)` method.

## Bonus

A container module can also be an abstraction for multiple containers.
Write a `ToxicPostgreSQLContainer` that starts a PostgreSQL container and a Toxiproxy container, 
providing more convenient methods for cutting the connection or introducing other failures.