/*
 * Copyright 2013-2018 the original author or authors.
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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @author xiaojing
 */
public class NacosWatch implements ApplicationEventPublisherAware, SmartLifecycle {

	private static final Logger log = LoggerFactory.getLogger(NacosWatch.class);

	private final NacosDiscoveryProperties properties;

	private final TaskScheduler taskScheduler;

	private final AtomicLong nacosWatchIndex = new AtomicLong(0);

	private final AtomicBoolean running = new AtomicBoolean(false);

	private ApplicationEventPublisher publisher;

	private ScheduledFuture<?> watchFuture;

	public NacosWatch(NacosDiscoveryProperties properties) {
		this(properties, getTaskScheduler());
	}

	public NacosWatch(NacosDiscoveryProperties properties, TaskScheduler taskScheduler) {
		this.properties = properties;
		this.taskScheduler = taskScheduler;
	}

	/**
	 * The constructor with {@link NacosDiscoveryProperties} bean and the optional.
	 * {@link TaskScheduler} bean
	 * @param properties {@link NacosDiscoveryProperties} bean
	 * @param taskScheduler the optional {@link TaskScheduler} bean
	 * @since 2.2.0
	 */
	public NacosWatch(NacosDiscoveryProperties properties,
			ObjectProvider<TaskScheduler> taskScheduler) {
		this(properties, taskScheduler.getIfAvailable(NacosWatch::getTaskScheduler));
	}

	private static ThreadPoolTaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setBeanName("Nacso-Watch-Task-Scheduler");
		taskScheduler.initialize();
		return taskScheduler;
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
		this.publisher = publisher;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		this.stop();
		callback.run();
	}

	@Override
	public void start() {
		if (this.running.compareAndSet(false, true)) {
			this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(
					this::nacosServicesWatch, this.properties.getWatchDelay());
		}
	}

	@Override
	public void stop() {
		if (this.running.compareAndSet(true, false) && this.watchFuture != null) {
			// shutdown current user-thread,
			// then the other daemon-threads will terminate automatic.
			((ThreadPoolTaskScheduler) this.taskScheduler).shutdown();

			this.watchFuture.cancel(true);
		}
	}

	@Override
	public boolean isRunning() {
		return this.running.get();
	}

	@Override
	public int getPhase() {
		return 0;
	}

	public void nacosServicesWatch() {

		// nacos doesn't support watch now , publish an event every 30 seconds.
		this.publisher.publishEvent(
				new HeartbeatEvent(this, nacosWatchIndex.getAndIncrement()));

	}

}
