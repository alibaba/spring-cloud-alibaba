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

package com.alibaba.cloud.nacos.discovery;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;


/**
 * @author yuhuangbin
 * @author ruansheng
 */
public class NacosDiscoveryHeartBeatPublisher implements ApplicationEventPublisherAware, SmartLifecycle {

	private static final Logger log = LoggerFactory.getLogger(NacosDiscoveryHeartBeatPublisher.class);

	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	private final ThreadPoolTaskScheduler taskScheduler;
	private final AtomicLong nacosHeartBeatIndex = new AtomicLong(0);
	private final AtomicBoolean running = new AtomicBoolean(false);
	private ApplicationEventPublisher publisher;
	private ScheduledFuture<?> heartBeatFuture;

	public NacosDiscoveryHeartBeatPublisher(NacosDiscoveryProperties nacosDiscoveryProperties) {
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
		this.taskScheduler = getTaskScheduler();
	}

	private static ThreadPoolTaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setBeanName("HeartBeat-Task-Scheduler");
		taskScheduler.initialize();
		return taskScheduler;
	}

	@Override
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			log.info("Start nacos heartBeat task scheduler.");
			this.heartBeatFuture = this.taskScheduler.scheduleWithFixedDelay(
					this::publishHeartBeat, Duration.ofMillis(this.nacosDiscoveryProperties.getWatchDelay()));
		}
	}

	@Override
	public void stop() {
		if (this.running.compareAndSet(true, false)) {
			if (this.heartBeatFuture != null) {
				// shutdown current user-thread,
				// then the other daemon-threads will terminate automatic.
				this.taskScheduler.shutdown();
				this.heartBeatFuture.cancel(true);
			}
		}
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.publisher = applicationEventPublisher;
	}

	/**
	 * nacos doesn't support watch now , publish an event every 30 seconds.
	 */
	public void publishHeartBeat() {
		HeartbeatEvent event = new HeartbeatEvent(this, nacosHeartBeatIndex.getAndIncrement());
		this.publisher.publishEvent(event);
	}
}
