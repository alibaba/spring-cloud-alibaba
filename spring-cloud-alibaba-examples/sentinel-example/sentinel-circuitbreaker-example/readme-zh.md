# Sentinel Feign Circuit Breaker Example

## 项目说明

OpenFeign 整合 Sentinel 断路器实现

## 示例

1. 添加配置到配置中心

dataId 为 `sentinel-circuitbreaker-rules.yml`
```yaml
feign:
  circuitbreaker:
    enabled: true # 开启 feign 断路器支持
  sentinel:
    default-rule: default # 默认规则名称
    rules:
      # 默认规则, 对所有 feign client 生效
      default:
        - grade: 2 # 根据异常数目降级
          count: 1
          timeWindow: 15 # 降级后到半开状态的时间
          statIntervalMs: 1000
          minRequestAmount: 1
      # 只对 feign client user 生效
      user:
        - grade: 2
          count: 1
          timeWindow: 15
          statIntervalMs: 1000
          minRequestAmount: 1
      # 只对 feign client user 的方法 feignMethod 生效
      # 括号里是参数类型, 多个逗号分割, 比如 user#method(boolean,String,Map)
      "[user#feignMethod(boolean)]":
        - grade: 2
          count: 1
          timeWindow: 10
          statIntervalMs: 1000
          minRequestAmount: 1
```

2. 启动 FeignCircuitBreakerApplication

## 验证配置生效
启动项目  

验证默认 feign client 生效  
先访问 http://localhost/test/default/false 2 次 （1秒内）  
再访问 http://localhost/test/default/true 断路器处于打开状态

验证指定 feign client 生效  
先访问 http://localhost/test/feign/false 2 次 （1秒内）  
再访问 http://localhost/test/feign/true 断路器处于打开状态

验证 feign client 指定方法生效  
先访问 http://localhost/test/feignMethod/false 2次 （1秒内）  
再访问 http://localhost/test/feignMethod/true 断路器处于打开状态

## 规则动态刷新
修改配置中心的规则, 再访问上述接口


