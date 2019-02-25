/*
 * Copyright (C) 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.nacos.discovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.ListView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
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

	private Set<String> cacheServices = new HashSet<>();

	private HashMap<String, EventListener> subscribeListeners = new HashMap<>();

	public NacosWatch(NacosDiscoveryProperties properties) {
		this(properties, getTaskScheduler());
	}

	public NacosWatch(NacosDiscoveryProperties properties, TaskScheduler taskScheduler) {
		this.properties = properties;
		this.taskScheduler = taskScheduler;
	}

	private static ThreadPoolTaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
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
			this.watchFuture.cancel(true);
		}
	}

	@Override
	public boolean isRunning() {
		return false;
	}

	@Override
	public int getPhase() {
		return 0;
	}

	public void nacosServicesWatch() {
		try {

			boolean changed = false;
			NamingService namingService = properties.namingServiceInstance();

			ListView<String> listView = properties.namingServiceInstance()
					.getServicesOfServer(1, Integer.MAX_VALUE);

			List<String> serviceList = listView.getData();

			// if there are new services found, publish event
			Set<String> currentServices = new HashSet<>(serviceList);
			currentServices.removeAll(cacheServices);
			if (currentServices.size() > 0) {
				changed = true;
			}

			// if some services disappear, publish event
			if (cacheServices.removeAll(new HashSet<>(serviceList))
					&& cacheServices.size() > 0) {
				changed = true;

				for (String serviceName : cacheServices) {
					namingService.unsubscribe(serviceName,
							subscribeListeners.get(serviceName));
					subscribeListeners.remove(serviceName);
				}
			}

			cacheServices = new HashSet<>(serviceList);

			// subscribe services's node change, publish event if nodes changed
			for (String serviceName : cacheServices) {
				if (!subscribeListeners.containsKey(serviceName)) {
					EventListener eventListener = event -> NacosWatch.this.publisher
							.publishEvent(new HeartbeatEvent(NacosWatch.this,
									nacosWatchIndex.getAndIncrement()));
					subscribeListeners.put(serviceName, eventListener);
					namingService.subscribe(serviceName, eventListener);

				}
			}

			if (changed) {
				this.publisher.publishEvent(
						new HeartbeatEvent(this, nacosWatchIndex.getAndIncrement()));
			}

		}
		catch (Exception e) {
			log.error("Error watching Nacos Service change", e);
		}
	}
}
