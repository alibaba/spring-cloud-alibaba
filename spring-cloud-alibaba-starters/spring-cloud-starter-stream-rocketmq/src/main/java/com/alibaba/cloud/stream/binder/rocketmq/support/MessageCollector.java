package com.alibaba.cloud.stream.binder.rocketmq.support;

import java.util.concurrent.BlockingQueue;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;

public interface MessageCollector {
	BlockingQueue<Message<?>> forChannel(MessageChannel channel);
}
