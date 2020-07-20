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

package com.alibaba.cloud.sentinel.endpoint;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.heartbeat.HeartbeatSenderProvider;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.function.Tuple2;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.util.CollectionUtils;

/**
 * A {@link HealthIndicator} for Sentinel, which checks the status of Sentinel Dashboard
 * and DataSource.
 *
 * <p>
 * Check the status of Sentinel Dashboard by sending a heartbeat message to it. If return
 * true, it's OK.
 *
 * Check the status of Sentinel DataSource by calling loadConfig method of
 * {@link AbstractDataSource}. If no Exception thrown, it's OK.
 *
 * If Dashboard and DataSource are both OK, the health status is UP.
 * </p>
 *
 * <p>
 * Note: If Sentinel isn't enabled, the health status is up. If Sentinel Dashboard isn't
 * configured, it's OK and mark the status of Dashboard with UNKNOWN. More informations
 * are provided in details.
 * </p>
 *
 * @author cdfive
 */
public class SentinelHealthIndicator extends AbstractHealthIndicator {

	private DefaultListableBeanFactory beanFactory;

	private SentinelProperties sentinelProperties;

	public SentinelHealthIndicator(DefaultListableBeanFactory beanFactory,
			SentinelProperties sentinelProperties) {
		this.beanFactory = beanFactory;
		this.sentinelProperties = sentinelProperties;
	}

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		Map<String, Object> detailMap = new HashMap<>();

		// If sentinel isn't enabled, set the status up and set the enabled to false in
		// detail
		if (!sentinelProperties.isEnabled()) {
			detailMap.put("enabled", false);
			builder.up().withDetails(detailMap);
			return;
		}

		detailMap.put("enabled", true);

		// Check health of Dashboard
		boolean dashboardUp = true;
		List<Tuple2<String, Integer>> consoleServerList = TransportConfig
				.getConsoleServerList();
		if (CollectionUtils.isEmpty(consoleServerList)) {
			// If Dashboard isn't configured, it's OK and mark the status of Dashboard
			// with UNKNOWN.
			detailMap.put("dashboard",
					new Status(Status.UNKNOWN.getCode(), "dashboard isn't configured"));
		}
		else {
			// If Dashboard is configured, send a heartbeat message to it and check the
			// result
			HeartbeatSender heartbeatSender = HeartbeatSenderProvider
					.getHeartbeatSender();
			boolean result = heartbeatSender.sendHeartbeat();
			if (result) {
				detailMap.put("dashboard", Status.UP);
			}
			else {
				// If failed to send heartbeat message, means that the Dashboard is DOWN
				dashboardUp = false;
				detailMap.put("dashboard",
						new Status(Status.DOWN.getCode(), String.format(
								"the dashboard servers [%s] one of them can't be connected",
								consoleServerList)));
			}
		}

		// Check health of DataSource
		boolean dataSourceUp = true;
		Map<String, Object> dataSourceDetailMap = new HashMap<>();
		detailMap.put("dataSource", dataSourceDetailMap);

		// Get all DataSources and each call loadConfig to check if it's OK
		// If no Exception thrown, it's OK
		// Note:
		// Even if the dynamic config center is down, the loadConfig() might return
		// successfully
		// e.g. for Nacos client, it might retrieve from the local cache)
		// But in most circumstances it's okay
		Map<String, AbstractDataSource> dataSourceMap = beanFactory
				.getBeansOfType(AbstractDataSource.class);
		for (Map.Entry<String, AbstractDataSource> dataSourceMapEntry : dataSourceMap
				.entrySet()) {
			String dataSourceBeanName = dataSourceMapEntry.getKey();
			AbstractDataSource dataSource = dataSourceMapEntry.getValue();
			try {
				dataSource.loadConfig();
				dataSourceDetailMap.put(dataSourceBeanName, Status.UP);
			}
			catch (Exception e) {
				// If one DataSource failed to loadConfig, means that the DataSource is
				// DOWN
				dataSourceUp = false;
				dataSourceDetailMap.put(dataSourceBeanName,
						new Status(Status.DOWN.getCode(), e.getMessage()));
			}
		}

		// If Dashboard and DataSource are both OK, the health status is UP
		if (dashboardUp && dataSourceUp) {
			builder.up().withDetails(detailMap);
		}
		else {
			builder.down().withDetails(detailMap);
		}
	}

}
