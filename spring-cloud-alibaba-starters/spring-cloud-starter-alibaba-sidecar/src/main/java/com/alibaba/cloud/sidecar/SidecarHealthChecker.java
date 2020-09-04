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

package com.alibaba.cloud.sidecar;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.scheduler.Schedulers;

import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author www.itmuch.com
 * @author yuhuangbin
 */
public class SidecarHealthChecker {

	private static final Logger log = LoggerFactory.getLogger(SidecarHealthChecker.class);

	private final Map<String, SidecarInstanceCache> sidecarInstanceCacheMap = new ConcurrentHashMap<>();

	private final SidecarDiscoveryClient sidecarDiscoveryClient;

	private final HealthIndicator healthIndicator;

	private final SidecarProperties sidecarProperties;

	private final ConfigurableEnvironment environment;

	public SidecarHealthChecker(SidecarDiscoveryClient sidecarDiscoveryClient,
			HealthIndicator healthIndicator, SidecarProperties sidecarProperties,
			ConfigurableEnvironment environment) {
		this.sidecarDiscoveryClient = sidecarDiscoveryClient;
		this.healthIndicator = healthIndicator;
		this.sidecarProperties = sidecarProperties;
		this.environment = environment;
	}

	public void check() {
		Schedulers.single().schedulePeriodically(() -> {
			String applicationName = environment.getProperty("spring.application.name");
			String ip = sidecarProperties.getIp();
			Integer port = sidecarProperties.getPort();

			Status status = healthIndicator.health().getStatus();

			instanceCache(applicationName, ip, port, status);
			if (status.equals(Status.UP)) {
				if (needRegister(applicationName, ip, port, status)) {
					this.sidecarDiscoveryClient.registerInstance(applicationName, ip,
							port);
					log.info(
							"Polyglot service changed and Health check success. register the new instance. applicationName = {}, ip = {}, port = {}, status = {}",
							applicationName, ip, port, status);
				}
			}
			else {
				log.warn(
						"Health check failed. unregister this instance. applicationName = {}, ip = {}, port = {}, status = {}",
						applicationName, ip, port, status);
				this.sidecarDiscoveryClient.deregisterInstance(applicationName, ip, port);

				sidecarInstanceCacheMap.put(applicationName,
						buildCache(ip, port, status));
			}

		}, 0, sidecarProperties.getHealthCheckInterval(), TimeUnit.MILLISECONDS);
	}

	private void instanceCache(String applicationName, String ip, Integer port,
			Status status) {
		sidecarInstanceCacheMap.putIfAbsent(applicationName,
				buildCache(ip, port, status));
	}

	private boolean needRegister(String applicationName, String ip, Integer port,
			Status status) {
		SidecarInstanceCache cacheRecord = sidecarInstanceCacheMap.get(applicationName);
		SidecarInstanceCache cache = buildCache(ip, port, status);

		if (!Objects.equals(cache, cacheRecord)) {
			// modify the cache info
			sidecarInstanceCacheMap.put(applicationName, cache);
			return true;
		}
		return false;
	}

	private SidecarInstanceCache buildCache(String ip, Integer port, Status status) {
		SidecarInstanceCache cache = new SidecarInstanceCache();
		cache.setIp(ip);
		cache.setPort(port);
		cache.setStatus(status);
		return cache;
	}

}
