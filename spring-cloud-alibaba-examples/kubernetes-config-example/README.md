## Spring Cloud Starter Alibaba Kubernetes Config Example

[English](README.md) | [中文](README-zh.md)

This example demonstrates the use of `spring-cloud-starter-alibaba-kubernetes-config` to implement a dynamic blacklist.

### Procedure

1. Apply the [deployments.yaml](./deployments.yaml) file

   Make sure you are in the `kubernetes-config-example` directory.

   ```shell
   kubectl apply -f deployments.yaml
   ```

2. Start the application

3. Access the application

   ```shell
   curl localhost:8080/echo -H 'x-user-id:1'
   ```

   You should see the user `1` has been blocked, you can check the [deployments.yaml](./deployments.yaml) to see blocked
   userIds.

4. Modify the `blacklist` configuration in the [deployments.yaml](./deployments.yaml) file

   ```yaml
   apiVersion: v1
   kind: ConfigMap
   metadata:
      name: configmap-01
      namespace: default
   data:
      blacklist.yml: |
         blacklist:
           user-ids: 
             - 2
   ```

   Then apply the file again

    ```shell
    kubectl apply -f deployments.yaml
    ```

5. Access the application again

    ```shell
    curl localhost:8080/echo -H 'x-user-id:1'
    ```

   You should see the user `1` has not been blocked, now only user `2` is blocked.

6. Delete resources

   Make sure you are in the `kubernetes-config-example` directory.

   ```shell
   kubectl delete -f deployments.yaml
   ```