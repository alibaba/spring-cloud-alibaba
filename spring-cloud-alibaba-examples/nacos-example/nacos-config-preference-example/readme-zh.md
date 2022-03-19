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

添加配置 `test2.yml`
```yaml
dev:
  age: 22
```

2. 设置配置偏好

设置默认配置偏好 
```yaml
spring:
  cloud:
    nacos:
      config:
        preference: remote
```

指定配置（test2.yml）设置配置偏好
```yaml
spring:
  config:
    import:
      - optional:nacos:test.yml
      - optional:nacos:test2.yml?preference=local
```

3. 验证
访问 `localhost`，应该能看到值为 `freeman: 20`，因为 `name` 优先使用了配置中心配置，`age` 优先使用本地配置。  