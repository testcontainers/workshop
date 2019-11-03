# Step 1: Getting Started

## Check Docker

Make sure you have Docker installed on your machine by running the following command:

```text
$ docker version

Client:
 Version:      18.03.1-ce
 API version:  1.37
 Go version:   go1.9.5
 Git commit:   9ee9f40
 Built:        Thu Apr 26 07:13:02 2018
 OS/Arch:      darwin/amd64
 Experimental: false
 Orchestrator: swarm

Server:
 Engine:
  Version:      18.03.1-ce
  API version:  1.37 (minimum version 1.12)
  Go version:   go1.9.5
  Git commit:   9ee9f40
  Built:        Thu Apr 26 07:22:38 2018
  OS/Arch:      linux/amd64
  Experimental: false
```

## Download the project

Clone the following project from GitHub to your computer:  
[https://github.com/testcontainers/workshop](https://github.com/testcontainers/workshop)

## \(optionally\) Build the project to download the dependencies

```text
./gradlew build -x check
```

## \(optionally\) Pull the required images before doing the workshop

```text
docker pull postgres:10-alpine
docker pull redis:3-alpine
docker pull openjdk:8-jre-alpine
docker pull confluentinc/cp-kafka:5.2.1
```


