# Step 1: Getting Started

## Check Docker

Make sure you have Docker available on your machine by running the following command:

```text
$ docker version

Client:
 Cloud integration: v1.0.22
 Version:           20.10.11
 API version:       1.41
 Go version:        go1.16.10
 Git commit:        dea9396
 Built:             Thu Nov 18 00:42:51 2021
 OS/Arch:           windows/amd64
 Context:           default
Server: Docker Engine - Community
 Engine:
  Version:          20.10.11
  API version:      1.41 (minimum version 1.12)
  Go version:       go1.16.9
  Git commit:       847da18
  Built:            Thu Nov 18 00:35:39 2021
  OS/Arch:          linux/amd64
  Experimental:     false
  ...
```

## Download the project

Clone the following project from GitHub to your computer:  
[https://github.com/testcontainers/workshop](https://github.com/testcontainers/workshop)

## Build the project to download the dependencies

With Gradle:
```text
# -x check - skips the tests
./gradlew build -x check
```

## \(optionally\) Pull the required images before doing the workshop

This might be helpful if the internet connection at the workshop venue is somewhat slow.

```text
docker pull postgres:14-alpine
docker pull redis:6-alpine
docker pull openjdk:8-jre-alpine
docker pull confluentinc/cp-kafka:5.4.6
```


