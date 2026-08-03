/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sentinel.rocketmq.autoconfigure;

import com.alibaba.cloud.sentinel.rocketmq.config.SentinelRocketMQProperties;
import com.alibaba.cloud.sentinel.rocketmq.interceptor.SentinelConsumerInterceptor;
import com.alibaba.cloud.sentinel.rocketmq.interceptor.SentinelProducerInterceptor;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for Sentinel RocketMQ 5.x integration.
 *
 * @author github-manager-bot
 * @since 2025.1.0.1
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({SendMessageHook.class, ConsumeMessageHook.class})
@EnableConfigurationProperties(SentinelRocketMQProperties.class)
@ConditionalOnProperty(prefix = "spring.cloud.sentinel.rocketmq", name = "enabled", matchIfMissing = true)
public class SentinelRocketMQAutoConfiguration {

    private final SentinelRocketMQProperties properties;

    public SentinelRocketMQAutoConfiguration(SentinelRocketMQProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.sentinel.rocketmq.producer", name = "flow-control-enabled", matchIfMissing = true)
    public SentinelProducerInterceptor sentinelProducerInterceptor() {
        SentinelRocketMQProperties.Producer producerProps = properties.getProducer();
        return new SentinelProducerInterceptor(
                producerProps.getResourcePrefix(),
                producerProps.isFlowControlEnabled(),
                producerProps.isCircuitBreakerEnabled()
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.cloud.sentinel.rocketmq.consumer", name = "flow-control-enabled", matchIfMissing = true)
    public SentinelConsumerInterceptor sentinelConsumerInterceptor() {
        SentinelRocketMQProperties.Consumer consumerProps = properties.getConsumer();
        return new SentinelConsumerInterceptor(
                consumerProps.getResourcePrefix(),
                consumerProps.isFlowControlEnabled(),
                consumerProps.isCircuitBreakerEnabled()
        );
    }
}
