/*
 * Copyright (C) 2018 the original author or authors.
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

package org.springframework.cloud.alibaba.sentinel;

import com.alibaba.csp.sentinel.adapter.servlet.CommonFilter;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;

/**
 * @author xiaojing
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelWebAutoConfiguration {
	private static final Logger logger = LoggerFactory
			.getLogger(SentinelWebAutoConfiguration.class);

	@Value("${project.name:${spring.application.name:}}")
	private String projectName;

	@Autowired
	private SentinelProperties properties;

	public static final String APP_NAME = "project.name";

	@PostConstruct
	private void init() {
		if (StringUtils.isEmpty(System.getProperty(APP_NAME))) {
			System.setProperty(APP_NAME, projectName);
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.SERVER_PORT))) {
			System.setProperty(TransportConfig.SERVER_PORT, properties.getPort());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))) {
			System.setProperty(TransportConfig.CONSOLE_SERVER, properties.getDashboard());
		}

	}

	@Bean
	@ConditionalOnWebApplication
	public FilterRegistrationBean<Filter> servletRequestListener() {
		FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();

		SentinelProperties.Filter filterConfig = properties.getFilter();

		if (null == filterConfig) {
			filterConfig = new SentinelProperties.Filter();
			properties.setFilter(filterConfig);
		}

		if (filterConfig.getUrlPatterns() == null
				|| filterConfig.getUrlPatterns().isEmpty()) {
			List<String> defaultPatterns = new ArrayList<>();
			defaultPatterns.add("/*");
			filterConfig.setUrlPatterns(defaultPatterns);
		}

		registration.addUrlPatterns(filterConfig.getUrlPatterns().toArray(new String[0]));
		Filter filter = new CommonFilter();
		registration.setFilter(filter);
		registration.setOrder(filterConfig.getOrder());
		logger.info("[Sentinel Starter] register Sentinel with urlPatterns: {}.",
				filterConfig.getUrlPatterns());
		return registration;

	}
}