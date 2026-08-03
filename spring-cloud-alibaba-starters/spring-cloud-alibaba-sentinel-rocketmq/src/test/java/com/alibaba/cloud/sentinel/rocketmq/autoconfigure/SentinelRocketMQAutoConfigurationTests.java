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
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SentinelRocketMQAutoConfiguration}.
 *
 * @author github-manager-bot
 * @since 2025.1.0.1
 */
class SentinelRocketMQAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SentinelRocketMQAutoConfiguration.class));

    @Test
    void testDefaultConfiguration() {
        this.contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SentinelRocketMQProperties.class);
            assertThat(context).hasSingleBean(SentinelProducerInterceptor.class);
            assertThat(context).hasSingleBean(SentinelConsumerInterceptor.class);
        });
    }

    @Test
    void testDisabledConfiguration() {
        this.contextRunner
                .withPropertyValues("spring.cloud.sentinel.rocketmq.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SentinelRocketMQProperties.class);
                    assertThat(context).doesNotHaveBean(SentinelProducerInterceptor.class);
                    assertThat(context).doesNotHaveBean(SentinelConsumerInterceptor.class);
                });
    }

    @Test
    void testProducerDisabledConfiguration() {
        this.contextRunner
                .withPropertyValues("spring.cloud.sentinel.rocketmq.producer.flow-control-enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(SentinelRocketMQProperties.class);
                    assertThat(context).doesNotHaveBean(SentinelProducerInterceptor.class);
                    assertThat(context).hasSingleBean(SentinelConsumerInterceptor.class);
                });
    }

    @Test
    void testConsumerDisabledConfiguration() {
        this.contextRunner
                .withPropertyValues("spring.cloud.sentinel.rocketmq.consumer.flow-control-enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(SentinelRocketMQProperties.class);
                    assertThat(context).hasSingleBean(SentinelProducerInterceptor.class);
                    assertThat(context).doesNotHaveBean(SentinelConsumerInterceptor.class);
                });
    }

    @Test
    void testCustomResourcePrefix() {
        this.contextRunner
                .withPropertyValues("spring.cloud.sentinel.rocketmq.producer.resource-prefix=custom-producer:")
                .run(context -> {
                    assertThat(context).hasSingleBean(SentinelRocketMQProperties.class);
                    SentinelRocketMQProperties properties = context.getBean(SentinelRocketMQProperties.class);
                    assertThat(properties.getProducer().getResourcePrefix()).isEqualTo("custom-producer:");
                });
    }
}
