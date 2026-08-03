# Spring Cloud Alibaba Sentinel RocketMQ

Sentinel integration for Apache RocketMQ 5.x in Spring Cloud Alibaba.

## Overview

This module provides flow control and circuit breaking capabilities for RocketMQ 5.x producers and consumers using Alibaba Sentinel. It automatically intercepts message sending and consumption operations to apply Sentinel rules.

## Features

- **Producer Flow Control**: Apply flow control rules to message sending operations
- **Consumer Flow Control**: Apply flow control rules to message consumption operations
- **Circuit Breaking**: Automatically circuit break when error rate exceeds threshold
- **Topic-based Resource Naming**: Each topic is treated as a separate Sentinel resource
- **Auto-configuration**: Automatically configures interceptors when RocketMQ and Sentinel are present
- **Customizable**: Configure resource prefixes and enable/disable features via properties

## Requirements

- Java 17+
- Spring Boot 4.x
- Apache RocketMQ 5.x (client 5.3.1+)
- Sentinel Core

## Usage

### Add Dependency

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-rocketmq</artifactId>
</dependency>
```

### Configuration

The module is auto-configured when both RocketMQ and Sentinel are present. You can customize the behavior via `application.yml`:

```yaml
spring:
  cloud:
    sentinel:
      rocketmq:
        enabled: true  # Enable/disable the integration (default: true)
        producer:
          flow-control-enabled: true  # Enable producer flow control (default: true)
          circuit-breaker-enabled: true  # Enable producer circuit breaking (default: true)
          resource-prefix: "rocketmq-producer:"  # Resource name prefix (default: "rocketmq-producer:")
        consumer:
          flow-control-enabled: true  # Enable consumer flow control (default: true)
          circuit-breaker-enabled: true  # Enable consumer circuit breaking (default: true)
          resource-prefix: "rocketmq-consumer:"  # Resource name prefix (default: "rocketmq-consumer:")
```

### Define Sentinel Rules

Configure Sentinel flow rules for your RocketMQ topics:

```java
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class SentinelRocketMQConfig {
    
    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        
        // Producer flow rule for topic "OrderTopic"
        FlowRule producerRule = new FlowRule();
        producerRule.setResource("rocketmq-producer:OrderTopic");
        producerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        producerRule.setCount(100); // 100 QPS limit
        rules.add(producerRule);
        
        // Consumer flow rule for topic "OrderTopic"
        FlowRule consumerRule = new FlowRule();
        consumerRule.setResource("rocketmq-consumer:OrderTopic");
        consumerRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        consumerRule.setCount(200); // 200 QPS limit
        rules.add(consumerRule);
        
        FlowRuleManager.loadRules(rules);
    }
}
```

### How It Works

1. **Producer Interceptor**: The `SentinelProducerInterceptor` is registered as a `SendMessageHook` and intercepts all message sending operations. Before sending a message, it creates a Sentinel entry for the topic. If the flow control rule is triggered, the message sending is blocked.

2. **Consumer Interceptor**: The `SentinelConsumerInterceptor` is registered as a `ConsumeMessageHook` and intercepts all message consumption operations. Before consuming messages, it creates a Sentinel entry for the topic. If the flow control rule is triggered, the message consumption is blocked.

3. **Resource Naming**: Each topic is treated as a separate Sentinel resource with the configured prefix. For example, with the default prefix, a topic named "OrderTopic" will have resources:
   - Producer: `rocketmq-producer:OrderTopic`
   - Consumer: `rocketmq-consumer:OrderTopic`

## Differences from RocketMQ 4.x Adapter

The existing `sentinel-rocketmq-adapter` module in the Sentinel project only supports RocketMQ 4.x. This module is specifically designed for RocketMQ 5.x with the following differences:

1. **API Compatibility**: Uses RocketMQ 5.x client API (`rocketmq-client` 5.3.1+)
2. **Hook Mechanism**: Utilizes the updated `SendMessageHook` and `ConsumeMessageHook` interfaces from RocketMQ 5.x
3. **Spring Boot Integration**: Provides Spring Boot auto-configuration for seamless integration
4. **Configuration Properties**: Exposes configuration via Spring Boot properties for easy customization

## License

Apache License 2.0
