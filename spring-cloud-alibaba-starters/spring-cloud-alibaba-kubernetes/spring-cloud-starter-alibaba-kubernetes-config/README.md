# spring-cloud-starter-alibaba-kubernetes-config

The main purpose of this module is to use Kubernetes ConfigMap/Secret as a distributed configuration center to achieve
dynamic configuration updates without restarting the application.

## Quick Start

Maven:

```xml

<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-kubernetes-config</artifactId>
</dependency>
```

Gradle:

```groovy
implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-kubernetes-config'
```

First you need a Kubernetes cluster, you can use [docker-desktop](https://www.docker.com/products/docker-desktop/)
or [minikube](https://minikube.sigs.k8s.io/docs/) to create a cluster.

1. Clone the project

    ```bash
    git clone --depth=1 https://github.com/alibaba/spring-cloud-alibaba.git
    ```

2. Create Role and RoleBinding for ServiceAccount

    ```bash
    # Just for the example, we created a ClusterRole, but in fact, you can control resources more finely, only need the get,list,watch permissions of ConfigMap/Secret
    kubectl create clusterrole config-cluster-reader --verb=get,list,watch --resource=configmaps,secrets
    # Bind ClusterRole to ServiceAccount (namespace: default, name: default)
    kubectl create clusterrolebinding config-cluster-reader-default-default --clusterrole config-cluster-reader --serviceaccount default:default
    ```

3. Build and Start
    ```shell
    ./mvnw clean package -pl com.alibaba.cloud:kubernetes-config-example -am -DskipTests
    
    docker build -f spring-cloud-alibaba-examples/kubernetes-config-example/Dockerfile -t kubernetes-config-example:latest .
    
    kubectl apply -f spring-cloud-alibaba-examples/kubernetes-config-example/deployment.yaml
    ```
    ```shell
    # Execute the following command after the application startup, the startup process should be very fast (less than 3s)
    curl http://localhost:`kubectl get service kubernetes-config-example -o jsonpath='{..nodePort}'`/price
    
    # You should see a response of `100`
    ```

4. Add a ConfigMap
    ```shell
    # This ConfigMap is being monitored by the current application, so when this ConfigMap is added, the application will automatically update the configuration
    kubectl apply -f spring-cloud-alibaba-examples/kubernetes-config-example/configmap-example-01.yaml
   
    # Visit again
    curl http://localhost:`kubectl get svc kubernetes-config -o jsonpath='{..nodePort}'`/price
   
    # You should see a response of `200`
    ```
   You can modify the configuration in `configmap-example-01.yaml`, and then re-apply the file to observe the change of
   the interface result.

   Through the above operations, you can see that the application can dynamically update the configuration without
   restarting.

5. Delete Resources
    ```shell
    # Delete all resources created by the above operations
    kubectl delete -f spring-cloud-alibaba-examples/kubernetes-config-example/deployment.yaml
    kubectl delete -f spring-cloud-alibaba-examples/kubernetes-config-example/configmap-example-01.yaml
    kubectl delete clusterrole config-cluster-reader
    kubectl delete clusterrolebinding config-cluster-reader-default-default
    ```

#### Main Features

- Dynamic update configuration（ConfigMap/Secret）

  You can manually configure whether to monitor configuration file changes.

- Configuration priority

  Through configuration, choose to use local configuration or remote configuration first.

- Supports multiple configuration file formats

  Supports configuration files in `yaml`, `properties`, `json` and key-value pair.

## Core Configurations

```yaml
spring:
  cloud:
    k8s:
      config:
        enabled: true
        namespace: default # The namespace where the configuration is located (global configuration). If it is inside the Kubernetes cluster, it defaults to the namespace where the current pod is located; if it is outside the Kubernetes cluster, it defaults to the namespace of the current context
        preference: remote # Configuration priority (global configuration), remote is preferred to use remote configuration, local is preferred to use local configuration, and the default is remote
        refreshable: true # Whether to enable dynamic update configuration (global configuration), the default is true
        refresh-on-delete: false # Whether to automatically refresh when deleting the configuration, enabling this configuration may bring certain risks, if your configuration items only exist on the remote side but not locally, if you delete the configmap by mistake, it may cause abnormalities in the program, so the default value is false
        fail-on-missing-config: true
        config-maps:
          - name: my-configmap # configmap name
            namespace: default # The namespace where configmap is located will override the global configuration of the namespace
            preference: remote # Configuration priority, which will override the global configuration of preference
            refreshable: true # Whether to enable dynamic update configuration, it will override the refresh-enabled global configuration
        secrets:
          - name: my-secret # secret name
            namespace: default # The namespace where the secret is located will override the global configuration of the namespace
            preference: remote # Configuration priority, which will override the global configuration of preference
            refreshable: false # Whether to enable dynamic update configuration will override the global configuration of refresh-enabled, because secrets generally do not require dynamic refresh, so the default value is false
```

## Best Practices

Spring Cloud provides the capability of dynamically refreshing the Environment at runtime, which mainly dynamically
updates the properties of two types of beans:

- Beans annotated with `@ConfigurationProperties`
- Beans annotated with `@RefreshScope`

A good practice is to use `@ConfigurationProperties` to organize your configurations.

In general, the configuration of an application falls into two categories:

- Basic configuration

    - public

      It can be managed through the configuration center or the jar package. Generally, there is no need for
      dynamic updates, such as Tomcat connection pool parameters, database connection pool parameters, etc.

    - private

      It can be placed in a local configuration file, such as database connection information. This type of
      configuration is generally sensitive and can be managed through Kubernetes Secret.

- Business configuration

  This type of configuration should be strongly related to the business logic, but users need to judge whether they need
  to be
  placed in the configuration center and whether there is a need for dynamic updates.
