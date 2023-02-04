## Spring Cloud Starter Alibaba Kubernetes Config Example

[English](README.md) | [中文](README-zh.md)

这个例子演示了使用 `spring-cloud-starter-alibaba-kubernetes-config` 来实现一个动态黑名单功能。

### 步骤

1. Apply [deployments.yaml](./deployments.yaml) 文件

   确保在 `kubernetes-config-example` 目录。

   ```shell
   kubectl apply -f deployments.yaml
   ```

2. 启动程序

3. 访问应用

   ```shell
   curl localhost:8080/echo -H 'x-user-id:1'
   ```

   可以看到用户 `1` 已经被 block 了，可以查看 [deployments.yaml](./deployments.yaml) 得知 blocked 的用户 id。

4. 修改 [deployments.yaml](./deployments.yaml) 文件中的 `blacklist` 配置。

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

   再次 apply

    ```shell
    kubectl apply -f deployments.yaml
    ```

5. 再次访问应用

    ```shell
    curl localhost:8080/echo -H 'x-user-id:1'
    ```

   可以看到用户 `1` 不再被 block，现在只有用户 `2` 被 block。

6. 删除资源

   确保在 `kubernetes-config-example` 目录。

   ```shell
   kubectl delete -f deployments.yaml
   ```