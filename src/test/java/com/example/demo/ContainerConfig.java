package com.example.demo;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class ContainerConfig {

  @Bean
  @ServiceConnection
  public PostgreSQLContainer postgreSQLContainer(){
    return new PostgreSQLContainer<>("postgres:14-alpine")
      .withCopyToContainer(MountableFile.forClasspathResource("schema.sql"),
        "/docker-entrypoint-initdb.d/schema.sql");
  }

  @Bean
  @ServiceConnection(name="redis")
  public GenericContainer redis(){
    return new GenericContainer<>("redis:6-alpine")
      .withExposedPorts(6379);
  }

  @Bean
  @ServiceConnection
  public KafkaContainer kafka() {
    return new KafkaContainer(
      DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
  }
}



