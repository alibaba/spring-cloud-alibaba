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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
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
 * @author yuhuangbin
 */
public class NacosWatch implements ApplicationEventPublisherAware, SmartLifecycle {

	private static final Logger log = LoggerFactory.getLogger(NacosWatch.class);

	private Map<String, EventListener> listenerMap = new ConcurrentHashMap<>(16);

	private final AtomicBoolean running = new AtomicBoolean(false);

	private final AtomicLong nacosWatchIndex = new AtomicLong(0);

	private ApplicationEventPublisher publisher;

	private ScheduledFuture<?> watchFuture;

	private NacosServiceManager nacosServiceManager;

	private final NacosDiscoveryProperties properties;

	private final TaskScheduler taskScheduler;

	public NacosWatch(NacosServiceManager nacosServiceManager,
			NacosDiscoveryProperties properties,
			ObjectProvider<TaskScheduler> taskScheduler) {
		this.nacosServiceManager = nacosServiceManager;
		this.properties = properties;
		this.taskScheduler = taskScheduler.getIfAvailable(NacosWatch::getTaskScheduler);
	}

	private static ThreadPoolTaskScheduler getTaskScheduler() {
		ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
		taskScheduler.setBeanName("Nacos-Watch-Task-Scheduler");
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
			EventListener eventListener = listenerMap.computeIfAbsent(buildKey(),
					event -> new EventListener() {
						@Override
						public void onEvent(Event event) {
							if (event instanceof NamingEvent) {
								List<Instance> instances = ((NamingEvent) event)
										.getInstances();
								Optional<Instance> instanceOptional = selectCurrentInstance(
										instances);
								instanceOptional.ifPresent(currentInstance -> {
									resetIfNeeded(currentInstance);
								});
							}
						}
					});

			NamingService namingService = nacosServiceManager
					.getNamingService(properties.getNacosProperties());
			try {
				namingService.subscribe(properties.getService(), properties.getGroup(),
						Arrays.asList(properties.getClusterName()), eventListener);
			}
			catch (Exception e) {
				log.error("namingService subscribe failed, properties:{}", properties, e);
			}

			this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(
					this::nacosServicesWatch, this.properties.getWatchDelay());
		}
	}

	private String buildKey() {
		return String.join(":", properties.getService(), properties.getGroup());
	}

	private void resetIfNeeded(Instance instance) {
		if (!properties.getMetadata().equals(instance.getMetadata())) {
			properties.setMetadata(instance.getMetadata());
		}
	}

	private Optional<Instance> selectCurrentInstance(List<Instance> instances) {
		return instances.stream()
				.filter(instance -> properties.getIp().equals(instance.getIp())
						&& properties.getPort() == instance.getPort())
				.findFirst();
	}

	@Override
	public void stop() {
		if (this.running.compareAndSet(true, false)) {
			if (this.watchFuture != null) {
				// shutdown current user-thread,
				// then the other daemon-threads will terminate automatic.
				((ThreadPoolTaskScheduler) this.taskScheduler).shutdown();
				this.watchFuture.cancel(true);
			}

			EventListener eventListener = listenerMap.get(buildKey());
			try {
				NamingService namingService = nacosServiceManager
						.getNamingService(properties.getNacosProperties());
				namingService.unsubscribe(properties.getService(), properties.getGroup(),
						Arrays.asList(properties.getClusterName()), eventListener);
			}
			catch (NacosException e) {
				log.error("namingService unsubscribe failed, properties:{}", properties,
						e);
			}
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
