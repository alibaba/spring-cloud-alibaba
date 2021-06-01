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

package com.alibaba.cloud.dubbo.registry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.alibaba.cloud.dubbo.env.DubboCloudProperties;
import com.alibaba.cloud.dubbo.registry.event.ServiceInstancesChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cloud.client.ServiceInstance;

/**
 * @author <a href="mailto:chenxilzx1@gmail.com">theonefx</a>
 */
public class ReSubscribeManager {

	private final Logger logger = LoggerFactory.getLogger(ReSubscribeManager.class);

	private final Map<String, ReSubscribeMetadataJob> reConnectJobMap = new ConcurrentHashMap<>();

	private final ScheduledThreadPoolExecutor reConnectPool = new ScheduledThreadPoolExecutor(
			5);

	private final DubboCloudRegistry registry;

	private final DubboCloudProperties properties;

	public ReSubscribeManager(DubboCloudRegistry registry) {
		this.registry = registry;
		this.properties = registry.getBean(DubboCloudProperties.class);

		reConnectPool.setKeepAliveTime(10, TimeUnit.MINUTES);
		reConnectPool.allowCoreThreadTimeOut(true);
	}

	public void onRefreshSuccess(ServiceInstancesChangedEvent event) {
		reConnectJobMap.remove(event.getServiceName());
	}

	public void onRefreshFail(ServiceInstancesChangedEvent event) {
		String serviceName = event.getServiceName();

		int count = 1;

		if (event instanceof FakeServiceInstancesChangedEvent) {
			count = ((FakeServiceInstancesChangedEvent) event).getCount() + 1;
		}

		if (count >= properties.getMaxReSubscribeMetadataTimes()) {
			logger.error(
					"reSubscribe failed too many times, serviceName = {}, count = {}",
					serviceName, count);
			return;
		}

		ReSubscribeMetadataJob job = new ReSubscribeMetadataJob(serviceName, count);
		reConnectPool.schedule(job, properties.getReSubscribeMetadataIntervial(),
				TimeUnit.SECONDS);
	}

	private final class ReSubscribeMetadataJob implements Runnable {

		private final String serviceName;

		private final int errorCounts;

		private ReSubscribeMetadataJob(String serviceName, int errorCounts) {
			this.errorCounts = errorCounts;
			this.serviceName = serviceName;
		}

		@Override
		public void run() {
			if (!reConnectJobMap.containsKey(serviceName)
					|| reConnectJobMap.get(serviceName) != this) {
				return;
			}
			List<ServiceInstance> list = registry.getServiceInstances(serviceName);
			FakeServiceInstancesChangedEvent event = new FakeServiceInstancesChangedEvent(
					serviceName, list, errorCounts);
			registry.onApplicationEvent(event);
		}

	}

	private static final class FakeServiceInstancesChangedEvent
			extends ServiceInstancesChangedEvent {

		private static final long serialVersionUID = -2832478604601472915L;

		private final int count;

		private FakeServiceInstancesChangedEvent(String serviceName,
				List<ServiceInstance> serviceInstances, int count) {
			super(serviceName, serviceInstances);
			this.count = count;
		}

		public int getCount() {
			return count;
		}

	}

}
