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

package com.alibaba.cloud.sentinel.rocketmq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Sentinel RocketMQ integration.
 *
 * @author github-manager-bot
 * @since 2025.1.0.1
 */
@ConfigurationProperties(prefix = "spring.cloud.sentinel.rocketmq")
public class SentinelRocketMQProperties {

    /**
     * Whether to enable Sentinel integration for RocketMQ.
     */
    private boolean enabled = true;

    /**
     * Producer configuration.
     */
    private Producer producer = new Producer();

    /**
     * Consumer configuration.
     */
    private Consumer consumer = new Consumer();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Producer-specific Sentinel configuration.
     */
    public static class Producer {

        /**
         * Whether to enable flow control for producer.
         */
        private boolean flowControlEnabled = true;

        /**
         * Whether to enable circuit breaking for producer.
         */
        private boolean circuitBreakerEnabled = true;

        /**
         * Resource name prefix for producer flow control.
         */
        private String resourcePrefix = "rocketmq-producer:";

        public boolean isFlowControlEnabled() {
            return flowControlEnabled;
        }

        public void setFlowControlEnabled(boolean flowControlEnabled) {
            this.flowControlEnabled = flowControlEnabled;
        }

        public boolean isCircuitBreakerEnabled() {
            return circuitBreakerEnabled;
        }

        public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
            this.circuitBreakerEnabled = circuitBreakerEnabled;
        }

        public String getResourcePrefix() {
            return resourcePrefix;
        }

        public void setResourcePrefix(String resourcePrefix) {
            this.resourcePrefix = resourcePrefix;
        }
    }

    /**
     * Consumer-specific Sentinel configuration.
     */
    public static class Consumer {

        /**
         * Whether to enable flow control for consumer.
         */
        private boolean flowControlEnabled = true;

        /**
         * Whether to enable circuit breaking for consumer.
         */
        private boolean circuitBreakerEnabled = true;

        /**
         * Resource name prefix for consumer flow control.
         */
        private String resourcePrefix = "rocketmq-consumer:";

        public boolean isFlowControlEnabled() {
            return flowControlEnabled;
        }

        public void setFlowControlEnabled(boolean flowControlEnabled) {
            this.flowControlEnabled = flowControlEnabled;
        }

        public boolean isCircuitBreakerEnabled() {
            return circuitBreakerEnabled;
        }

        public void setCircuitBreakerEnabled(boolean circuitBreakerEnabled) {
            this.circuitBreakerEnabled = circuitBreakerEnabled;
        }

        public String getResourcePrefix() {
            return resourcePrefix;
        }

        public void setResourcePrefix(String resourcePrefix) {
            this.resourcePrefix = resourcePrefix;
        }
    }
}
