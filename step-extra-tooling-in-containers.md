# Step 13: Tooling in containers

Testcontainers gives you an API to manage applications in containers. 
It can be databases, message brokers, or anything else your application needs to run, but you can also run additional tools that you as a developer want to have access to during development and testing.

For example, you might want to run a tool to connect to the database, or a Kafka cluster monitoring console, and so on.
Running these tools together with the containers and wiring them together programmatically is an easy way to ensure everyone on your team can use the same setup reliably. 

In this chapter, we'll write a sample test spinning up a Kubernetes cluster, and wire it together with the [k9s console](https://k9scli.io/). 

# Setup 

We'll need a K8s module for Testcontainers implemnetation, and a library to interact with the K8s cluster from our Java code.

Include the following dependencies into your project: 

```
<dependency>
  <groupId>com.dajudge.kindcontainer</groupId>
  <artifactId>kindcontainer</artifactId>
  <version>1.4.1</version>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>io.fabric8</groupId>
  <artifactId>kubernetes-client</artifactId>
  <version>6.4.1</version>
</dependency>
```

Since we're not actually making our Spring Boot application work within the K8s cluster, we can completely detach the source files. 
Create a new `K8sTest.java` class file in the test sources with the boilerplate setup we'll need. 

The `createDeployment` method you see below will take a configured `KubernetesClient` and deploy 2 replicas of Nginx webserver. 

```java
public class K8sTest {
 static final String NAME = "testcontainers";
 static Network network = Network.newNetwork();

 private static void createDeployment(KubernetesClient client, Map<String, String> selectors) {
        Deployment d = new DeploymentBuilder()
                .withNewMetadata()
                    .withName(NAME)
                    .withLabels(selectors)
                .endMetadata()
                .withSpec(new DeploymentSpecBuilder()
                        .withReplicas(2)
                        .withTemplate(new PodTemplateSpecBuilder()
                                .withNewMetadata()
                                    .withLabels(selectors)
                                .endMetadata()
                                .withNewSpec()
                                .addNewContainer()
                                    .withName("nginx")
                                    .withImage("nginx:1.23.1")
                                    .addNewPort().withContainerPort(80).endPort()
                                .endContainer()
                                .endSpec()
                                .build())
                        .withNewSelector()
                        .withMatchLabels(selectors)
                        .endSelector()
                        .build())
                .build();

        client.apps().deployments().inNamespace(NAME).create(d);
    }
}
```

# Spinning up a K8s cluster
Now let's spin up a K8s cluster, create a normal JUnit 5 `@Test` method, and start a `K3sContainer` in it: 

```java
@Test
public void myTest() throws IOException {
    K3sContainer<?> k8s = new K3sContainer<>();

    k8s.withNetwork(network);
    k8s.withNetworkAliases("k3s");

    k8s.start();

// obtain a kubeconfig file which allows us to connect to k3s
    String kubeConfigYaml = k8s.getKubeconfig();

// use the config and deploy nginx containers. 
    Config config = Config.fromKubeconfig(kubeConfigYaml);
    KubernetesClient client = new DefaultKubernetesClient(config);
    Namespace ns = new NamespaceBuilder().withNewMetadata().withName(NAME)
            .endMetadata().build();
    client.namespaces().create(ns);
    var selectors = Map.of("app", NAME);
    createDeployment(client, selectors);
}
```

Now in the `kubeConfigYaml` variable we have the yaml configuration that allows us to connect to this cluster.
And the rest of the code deploys the containers into our clusters. 

The test doesn't actually verify anything right now, so to observe the k8s cluster working you can set a breakpoint on the last line of the test, or stop the execution otherwise: 

```
// Don't do this in your tests!
System.in.read();
```

The `getKubeconfig()` method produces the config to connect from the host machine.
We'll need to edit it to allow other tools running in the same Docker environment to connect using the correct host/ports. 

```
String inDockerConfig = kubeConfigYaml.replaceAll("127\\.0\\.0\\.1", "k3s");
inDockerConfig = inDockerConfig.replaceAll(Integer.toString(k8s.getFirstMappedPort(), "6443"),
```

# Adding the k9s console 

Let's add a `GenericContainer` with the K9s running. Here's the example config you can use: 

```java
final String command = "#!/bin/sh\n"
+ "set -ex \n"
+ "wget https://github.com/tsl0922/ttyd/releases/download/1.7.3/ttyd.i686 \n"
+ "chmod u+x ttyd.i686 \n"
+ "./ttyd.i686 -W k9s \n";

GenericContainer<?> k9s = new GenericContainer("derailed/k9s:latest") {
    @Override
    protected void containerIsStarting(InspectContainerResponse containerInfo) {
        super.containerIsStarting(containerInfo);
        this.copyFileToContainer(Transferable.of(command, 0777), "/testcontainers_start.sh");
    }
}
.withNetwork(network)
.withExposedPorts(7681)
.withCopyToContainer(Transferable.of(inDockerConfig), "/root/.kube/config")
.withStartupTimeout(Duration.of(20, ChronoUnit.SECONDS))
.waitingFor(Wait.forLogMessage(".*Listening on port:.*\\n", 1))

.withCommand(new String[]{"-c", "while [ ! -f /testcontainers_start.sh ]; do sleep 0.1; done; /testcontainers_start.sh"});

k9s.withCreateContainerCmdModifier((cmd) -> {
    cmd.withEntrypoint(new String[]{"sh"});
});
k9s.start();
System.out.println("http://localhost:" + k9s.getFirstMappedPort() + "/");
```

Note, we use the `GenericContainer("derailed/k9s:latest")` for the container, put it on the same `Network`, and configure it to have shell as the enrtypoint. 
Additionally we use [ttyd](https://github.com/tsl0922/ttyd) to expose the K9s console as a web application. 

If you run the test now, the output will contain a link to the k9s in the browser. Click on it and explore your Kubernetes cluster. 
