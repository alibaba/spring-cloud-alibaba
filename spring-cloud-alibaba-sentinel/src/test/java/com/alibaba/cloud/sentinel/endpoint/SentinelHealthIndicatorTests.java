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

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.heartbeat.HeartbeatSenderProvider;
import com.alibaba.csp.sentinel.transport.HeartbeatSender;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link SentinelHealthIndicator}.
 *
 * @author cdfive
 */
public class SentinelHealthIndicatorTests {

	private SentinelHealthIndicator sentinelHealthIndicator;

	private DefaultListableBeanFactory beanFactory;

	private SentinelProperties sentinelProperties;

	private HeartbeatSender heartbeatSender;

	@Before
	public void setUp() {
		beanFactory = mock(DefaultListableBeanFactory.class);
		sentinelProperties = mock(SentinelProperties.class);
		sentinelHealthIndicator = new SentinelHealthIndicator(beanFactory,
				sentinelProperties);

		SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "");

		heartbeatSender = mock(HeartbeatSender.class);
		Field heartbeatSenderField = ReflectionUtils
				.findField(HeartbeatSenderProvider.class, "heartbeatSender");
		heartbeatSenderField.setAccessible(true);
		ReflectionUtils.setField(heartbeatSenderField, null, heartbeatSender);
	}

	@Test
	public void testSentinelNotEnabled() {
		when(sentinelProperties.isEnabled()).thenReturn(false);

		Health health = sentinelHealthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails().get("enabled")).isEqualTo(false);
	}

	@Test
	public void testSentinelDashboardNotConfigured() {
		when(sentinelProperties.isEnabled()).thenReturn(true);

		Health health = sentinelHealthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		assertThat(health.getDetails().get("dashboard")).isEqualTo(Status.UNKNOWN);
	}

	@Test
	public void testSentinelDashboardConfiguredSuccess() throws Exception {
		when(sentinelProperties.isEnabled()).thenReturn(true);
		SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "localhost:8080");
		when(heartbeatSender.sendHeartbeat()).thenReturn(true);

		Health health = sentinelHealthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
	}

	@Test
	public void testSentinelDashboardConfiguredFailed() throws Exception {
		when(sentinelProperties.isEnabled()).thenReturn(true);
		SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "localhost:8080");
		when(heartbeatSender.sendHeartbeat()).thenReturn(false);

		Health health = sentinelHealthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		assertThat(health.getDetails().get("dashboard")).isEqualTo(
				new Status(Status.DOWN.getCode(), "localhost:8080 can't be connected"));
	}

	@Test
	public void testSentinelDataSourceSuccess() throws Exception {
		when(sentinelProperties.isEnabled()).thenReturn(true);
		SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "localhost:8080");
		when(heartbeatSender.sendHeartbeat()).thenReturn(true);

		Map<String, AbstractDataSource> dataSourceMap = new HashMap<>();

		FileRefreshableDataSource fileDataSource1 = mock(FileRefreshableDataSource.class);
		dataSourceMap.put("ds1-sentinel-file-datasource", fileDataSource1);

		FileRefreshableDataSource fileDataSource2 = mock(FileRefreshableDataSource.class);
		dataSourceMap.put("ds2-sentinel-file-datasource", fileDataSource2);

		when(beanFactory.getBeansOfType(AbstractDataSource.class))
				.thenReturn(dataSourceMap);

		Health health = sentinelHealthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.UP);
		Map<String, Status> dataSourceDetailMap = (Map<String, Status>) health
				.getDetails().get("dataSource");
		assertThat(dataSourceDetailMap.get("ds1-sentinel-file-datasource"))
				.isEqualTo(Status.UP);
		assertThat(dataSourceDetailMap.get("ds2-sentinel-file-datasource"))
				.isEqualTo(Status.UP);
	}

	@Test
	public void testSentinelDataSourceFailed() throws Exception {
		when(sentinelProperties.isEnabled()).thenReturn(true);
		SentinelConfig.setConfig(TransportConfig.CONSOLE_SERVER, "localhost:8080");
		when(heartbeatSender.sendHeartbeat()).thenReturn(true);

		Map<String, AbstractDataSource> dataSourceMap = new HashMap<>();

		FileRefreshableDataSource fileDataSource1 = mock(FileRefreshableDataSource.class);
		dataSourceMap.put("ds1-sentinel-file-datasource", fileDataSource1);

		FileRefreshableDataSource fileDataSource2 = mock(FileRefreshableDataSource.class);
		when(fileDataSource2.loadConfig())
				.thenThrow(new RuntimeException("fileDataSource2 error"));
		dataSourceMap.put("ds2-sentinel-file-datasource", fileDataSource2);

		when(beanFactory.getBeansOfType(AbstractDataSource.class))
				.thenReturn(dataSourceMap);

		Health health = sentinelHealthIndicator.health();

		assertThat(health.getStatus()).isEqualTo(Status.DOWN);
		Map<String, Status> dataSourceDetailMap = (Map<String, Status>) health
				.getDetails().get("dataSource");
		assertThat(dataSourceDetailMap.get("ds1-sentinel-file-datasource"))
				.isEqualTo(Status.UP);
		assertThat(dataSourceDetailMap.get("ds2-sentinel-file-datasource"))
				.isEqualTo(new Status(Status.DOWN.getCode(), "fileDataSource2 error"));
	}

}
