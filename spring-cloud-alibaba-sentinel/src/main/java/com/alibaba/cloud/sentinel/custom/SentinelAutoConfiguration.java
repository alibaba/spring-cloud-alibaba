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

package com.alibaba.cloud.sentinel.custom;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.sentinel.SentinelProperties;
import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.datasource.converter.XmlConverter;
import com.alibaba.csp.sentinel.adapter.servlet.config.WebServletConfig;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import com.alibaba.csp.sentinel.util.AppNameUtil;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

/**
 * @author xiaojing
 * @author jiashuai.xie
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@Configuration
@ConditionalOnProperty(name = "spring.cloud.sentinel.enabled", matchIfMissing = true)
@EnableConfigurationProperties(SentinelProperties.class)
public class SentinelAutoConfiguration {

	@Value("${project.name:${spring.application.name:}}")
	private String projectName;

	@Autowired
	private SentinelProperties properties;

	@PostConstruct
	private void init() {
		if (StringUtils.isEmpty(System.getProperty(LogBase.LOG_DIR))
				&& StringUtils.hasText(properties.getLog().getDir())) {
			System.setProperty(LogBase.LOG_DIR, properties.getLog().getDir());
		}
		if (StringUtils.isEmpty(System.getProperty(LogBase.LOG_NAME_USE_PID))
				&& properties.getLog().isSwitchPid()) {
			System.setProperty(LogBase.LOG_NAME_USE_PID,
					String.valueOf(properties.getLog().isSwitchPid()));
		}
		if (StringUtils.isEmpty(System.getProperty(AppNameUtil.APP_NAME))
				&& StringUtils.hasText(projectName)) {
			System.setProperty(AppNameUtil.APP_NAME, projectName);
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.SERVER_PORT))
				&& StringUtils.hasText(properties.getTransport().getPort())) {
			System.setProperty(TransportConfig.SERVER_PORT,
					properties.getTransport().getPort());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.CONSOLE_SERVER))
				&& StringUtils.hasText(properties.getTransport().getDashboard())) {
			System.setProperty(TransportConfig.CONSOLE_SERVER,
					properties.getTransport().getDashboard());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.HEARTBEAT_INTERVAL_MS))
				&& StringUtils
						.hasText(properties.getTransport().getHeartbeatIntervalMs())) {
			System.setProperty(TransportConfig.HEARTBEAT_INTERVAL_MS,
					properties.getTransport().getHeartbeatIntervalMs());
		}
		if (StringUtils.isEmpty(System.getProperty(TransportConfig.HEARTBEAT_CLIENT_IP))
				&& StringUtils.hasText(properties.getTransport().getClientIp())) {
			System.setProperty(TransportConfig.HEARTBEAT_CLIENT_IP,
					properties.getTransport().getClientIp());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.CHARSET))
				&& StringUtils.hasText(properties.getMetric().getCharset())) {
			System.setProperty(SentinelConfig.CHARSET,
					properties.getMetric().getCharset());
		}
		if (StringUtils
				.isEmpty(System.getProperty(SentinelConfig.SINGLE_METRIC_FILE_SIZE))
				&& StringUtils.hasText(properties.getMetric().getFileSingleSize())) {
			System.setProperty(SentinelConfig.SINGLE_METRIC_FILE_SIZE,
					properties.getMetric().getFileSingleSize());
		}
		if (StringUtils
				.isEmpty(System.getProperty(SentinelConfig.TOTAL_METRIC_FILE_COUNT))
				&& StringUtils.hasText(properties.getMetric().getFileTotalCount())) {
			System.setProperty(SentinelConfig.TOTAL_METRIC_FILE_COUNT,
					properties.getMetric().getFileTotalCount());
		}
		if (StringUtils.isEmpty(System.getProperty(SentinelConfig.COLD_FACTOR))
				&& StringUtils.hasText(properties.getFlow().getColdFactor())) {
			System.setProperty(SentinelConfig.COLD_FACTOR,
					properties.getFlow().getColdFactor());
		}
		if (StringUtils.hasText(properties.getServlet().getBlockPage())) {
			WebServletConfig.setBlockPage(properties.getServlet().getBlockPage());
		}

		// earlier initialize
		if (properties.isEager()) {
			InitExecutor.doInit();
		}

	}

	@Bean
	@ConditionalOnMissingBean
	public SentinelResourceAspect sentinelResourceAspect() {
		return new SentinelResourceAspect();
	}

	@Bean
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
	@ConditionalOnProperty(name = "resttemplate.sentinel.enabled", havingValue = "true", matchIfMissing = true)
	public SentinelBeanPostProcessor sentinelBeanPostProcessor(
			ApplicationContext applicationContext) {
		return new SentinelBeanPostProcessor(applicationContext);
	}

	@Bean
	@ConditionalOnMissingBean
	public SentinelDataSourceHandler sentinelDataSourceHandler(
			DefaultListableBeanFactory beanFactory, SentinelProperties sentinelProperties,
			Environment env) {
		return new SentinelDataSourceHandler(beanFactory, sentinelProperties, env);
	}

	@ConditionalOnClass(ObjectMapper.class)
	@Configuration
	protected static class SentinelConverterConfiguration {

		@Configuration
		protected static class SentinelJsonConfiguration {

			private ObjectMapper objectMapper = new ObjectMapper();

			public SentinelJsonConfiguration() {
				objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);
			}

			@Bean("sentinel-json-flow-converter")
			public JsonConverter jsonFlowConverter() {
				return new JsonConverter(objectMapper, FlowRule.class);
			}

			@Bean("sentinel-json-degrade-converter")
			public JsonConverter jsonDegradeConverter() {
				return new JsonConverter(objectMapper, DegradeRule.class);
			}

			@Bean("sentinel-json-system-converter")
			public JsonConverter jsonSystemConverter() {
				return new JsonConverter(objectMapper, SystemRule.class);
			}

			@Bean("sentinel-json-authority-converter")
			public JsonConverter jsonAuthorityConverter() {
				return new JsonConverter(objectMapper, AuthorityRule.class);
			}

			@Bean("sentinel-json-param-flow-converter")
			public JsonConverter jsonParamFlowConverter() {
				return new JsonConverter(objectMapper, ParamFlowRule.class);
			}

		}

		@ConditionalOnClass(XmlMapper.class)
		@Configuration
		protected static class SentinelXmlConfiguration {

			private XmlMapper xmlMapper = new XmlMapper();

			public SentinelXmlConfiguration() {
				xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
						false);
			}

			@Bean("sentinel-xml-flow-converter")
			public XmlConverter xmlFlowConverter() {
				return new XmlConverter(xmlMapper, FlowRule.class);
			}

			@Bean("sentinel-xml-degrade-converter")
			public XmlConverter xmlDegradeConverter() {
				return new XmlConverter(xmlMapper, DegradeRule.class);
			}

			@Bean("sentinel-xml-system-converter")
			public XmlConverter xmlSystemConverter() {
				return new XmlConverter(xmlMapper, SystemRule.class);
			}

			@Bean("sentinel-xml-authority-converter")
			public XmlConverter xmlAuthorityConverter() {
				return new XmlConverter(xmlMapper, AuthorityRule.class);
			}

			@Bean("sentinel-xml-param-flow-converter")
			public XmlConverter xmlParamFlowConverter() {
				return new XmlConverter(xmlMapper, ParamFlowRule.class);
			}

		}
	}

}
