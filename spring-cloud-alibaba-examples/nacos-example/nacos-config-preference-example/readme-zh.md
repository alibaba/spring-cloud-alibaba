# Nacos Config Preference

## 项目说明

本项目演示如何使用配置偏好配置

## 示例

1. 启动一个 Nacos server  
添加配置 `test.yml`

```yaml
configdata:
  user:
    name: freeman
```

2. 设置配置偏好 `spring.cloud.nacos.config.preference=remote`

3. 访问 `localhost`  
应该能看到值为 `freeman`，因为优先使用了配置中心配置  
修改配置为 `spring.cloud.nacos.config.preference=local` 那么值应该为 `aa`