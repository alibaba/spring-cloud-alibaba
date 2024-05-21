/*
 * Copyright 2024-2025 the original author or authors.
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

package com.alibaba.cloud.sentinel.custom.context;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.sentinel.SentinelConstants;
import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static com.alibaba.cloud.sentinel.SentinelConstants.BLOCK_PAGE_URL_CONF_KEY;
import static com.alibaba.csp.sentinel.config.SentinelConfig.setConfig;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public class SentinelApplicationContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {

		ConfigurableEnvironment environment = applicationContext.getEnvironment();
		String applicationName = environment.getProperty("spring.application.name");
		SentinelProperties sentinelProperties = Binder.get(environment)
				.bindOrCreate(SentinelConstants.PROPERTY_PREFIX, SentinelProperties.class);

		initSentinelConfig(sentinelProperties, applicationName);
	}

	private void initSentinelConfig(SentinelProperties properties, String applicationName) {

		if (StringUtils.isEmpty(System.getProperty(LogBase.LOG_DIR))
				&& StringUtils.isNotBlank(properties.getLog().getDir())) {
			System.setProperty(LogBase.LOG_DIR, properties.getLog().getDir());
		}
		if (StringUtils.isEmpty(System.getProperty(LogBase.LOG_NAME_USE_PID))
				&& properties.getLog().isSwitchPid()) {
			System.setProperty(LogBase.LOG_NAME_USE_PID,
					String.valueOf(properties.getLog().isSwitchPid()));
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.APP_NAME_PROP_KEY))
				&& StringUtils.isNotBlank(applicationName)) {
			System.setProperty(SentinelConfig.APP_NAME_PROP_KEY, applicationName);
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.SERVER_PORT))
				&& StringUtils.isNotBlank(properties.getTransport().getPort())) {
			System.setProperty(TransportConfig.SERVER_PORT,
					properties.getTransport().getPort());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))
				&& StringUtils.isNotBlank(properties.getTransport().getDashboard())) {
			System.setProperty(TransportConfig.CONSOLE_SERVER,
					properties.getTransport().getDashboard());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.HEARTBEAT_INTERVAL_MS))
				&& StringUtils
				.isNotBlank(properties.getTransport().getHeartbeatIntervalMs())) {
			System.setProperty(TransportConfig.HEARTBEAT_INTERVAL_MS,
					properties.getTransport().getHeartbeatIntervalMs());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.HEARTBEAT_CLIENT_IP))
				&& StringUtils.isNotBlank(properties.getTransport().getClientIp())) {
			System.setProperty(TransportConfig.HEARTBEAT_CLIENT_IP,
					properties.getTransport().getClientIp());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.CHARSET))
				&& StringUtils.isNotBlank(properties.getMetric().getCharset())) {
			System.setProperty(SentinelConfig.CHARSET,
					properties.getMetric().getCharset());
		}
		if (StringUtils
				.isEmpty(System.getProperty(SentinelConfig.SINGLE_METRIC_FILE_SIZE))
				&& StringUtils.isNotBlank(properties.getMetric().getFileSingleSize())) {
			System.setProperty(SentinelConfig.SINGLE_METRIC_FILE_SIZE,
					properties.getMetric().getFileSingleSize());
		}
		if (StringUtils
				.isEmpty(System.getProperty(SentinelConfig.TOTAL_METRIC_FILE_COUNT))
				&& StringUtils.isNotBlank(properties.getMetric().getFileTotalCount())) {
			System.setProperty(SentinelConfig.TOTAL_METRIC_FILE_COUNT,
					properties.getMetric().getFileTotalCount());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.COLD_FACTOR))
				&& StringUtils.isNotBlank(properties.getFlow().getColdFactor())) {
			System.setProperty(SentinelConfig.COLD_FACTOR,
					properties.getFlow().getColdFactor());
		}
		if (StringUtils.isNotBlank(properties.getBlockPage())) {
			setConfig(BLOCK_PAGE_URL_CONF_KEY, properties.getBlockPage());
		}

		// earlier initialize
		if (properties.isEager()) {

			InitExecutor.doInit();
		}

	}

}
