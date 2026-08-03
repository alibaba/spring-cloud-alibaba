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

package com.alibaba.cloud.sentinel.rocketmq.interceptor;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.Tracer;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.rocketmq.client.hook.ConsumeMessageContext;
import org.apache.rocketmq.client.hook.ConsumeMessageHook;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Sentinel interceptor for RocketMQ 5.x consumer.
 * Provides flow control and circuit breaking for message consumption.
 *
 * @author github-manager-bot
 * @since 2025.1.0.1
 */
public class SentinelConsumerInterceptor implements ConsumeMessageHook {

    private static final Logger log = LoggerFactory.getLogger(SentinelConsumerInterceptor.class);

    private final String resourcePrefix;
    private final boolean flowControlEnabled;
    private final boolean circuitBreakerEnabled;

    public SentinelConsumerInterceptor(String resourcePrefix, boolean flowControlEnabled, boolean circuitBreakerEnabled) {
        this.resourcePrefix = resourcePrefix;
        this.flowControlEnabled = flowControlEnabled;
        this.circuitBreakerEnabled = circuitBreakerEnabled;
    }

    @Override
    public String hookName() {
        return "SentinelConsumerInterceptor";
    }

    @Override
    public void consumeMessageBefore(ConsumeMessageContext context) {
        if (!flowControlEnabled && !circuitBreakerEnabled) {
            return;
        }

        List<MessageExt> msgs = context.getMsgList();
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        // Use the first message's topic as the resource name
        String topic = msgs.get(0).getTopic();
        String resourceName = resourcePrefix + topic;

        try {
            Entry entry = SphU.entry(resourceName, EntryType.IN);
            context.setMqTrace(entry);
            log.debug("Sentinel entry created for consumer resource: {}", resourceName);
        }
        catch (BlockException ex) {
            log.warn("Message consumption blocked by Sentinel for topic: {}", topic);
            throw new RuntimeException("Message consumption blocked by Sentinel flow control", ex);
        }
    }

    @Override
    public void consumeMessageAfter(ConsumeMessageContext context) {
        Entry entry = (Entry) context.getMqTrace();
        if (entry == null) {
            return;
        }

        Exception exception = context.getException();
        if (exception != null) {
            Tracer.traceEntry(exception, entry);
            log.error("Message consumption failed", exception);
        }

        entry.exit();
        log.debug("Sentinel entry exited for consumer resource");
    }
}
