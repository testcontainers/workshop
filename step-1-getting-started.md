# Step 1: Getting Started

## Check Java
You'll need Java 17 or newer for this workshop. 
Testcontainers libraries are compatible with Java 8+, but this workshop uses a Spring Boot 3.x application which requires Java 17 or newer.

## Install Testcontainers Desktop
Testcontainers Desktop is a free tool that can help you during the local development and debugging of your tests.
You can download the app from [https://testcontainers.com/desktop/](https://testcontainers.com/desktop/).

## Check Docker

To use Testcontainers, you need to have a supported Docker environment.
If you have the Testcontainers Desktop application installed, it will show you the available container runtimes on your machine.

* You can choose any of the installed container runtimes like **Docker Desktop**, **OrbStack**, **Rancher Desktop**, **Podman**, etc.
* It can be [Testcontainers Cloud](https://testcontainers.com/cloud) recommended to avoid straining the conference network by pulling heavy Docker images. 
* If you don't have any container runtimes installed on your machine, you can also use the **Embedded Runtime** provided by Testcontainers Desktop.

You can check the Docker availability by running: 
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

With Maven:
```text
./mvnw verify
```

## \(optionally\) Pull the required images before doing the workshop

This might be helpful if the internet connection at the workshop venue is somewhat slow.

```text
docker pull postgres:16-alpine
docker pull redis:7-alpine
docker pull openjdk:8-jre-alpine
docker pull confluentinc/cp-kafka:7.5.0
```

### 
[Next](step-2-exploring-the-app.md)


