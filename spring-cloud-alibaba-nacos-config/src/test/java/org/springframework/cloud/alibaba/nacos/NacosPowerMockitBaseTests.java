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
package org.springframework.cloud.alibaba.nacos;

import com.alibaba.nacos.api.config.ConfigService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.MethodProxy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySource;
import org.springframework.cloud.alibaba.nacos.client.NacosPropertySourceBuilder;
import org.springframework.cloud.alibaba.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author pbting
 * @date 2019-01-17 8:54 PM
 */
@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@PowerMockIgnore({ "javax.management.*", "javax.net.ssl.*" })
@PrepareForTest({ NacosPropertySourceBuilder.class })
@SpringBootTest(classes = { NacosConfigBootstrapConfiguration.class,
		NacosConfigEndpointAutoConfiguration.class, NacosConfigAutoConfiguration.class,
		NacosPowerMockitBaseTests.TestConfiguration.class }, properties = {
				"spring.application.name=sca-nacos-config",
				"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
				"spring.cloud.nacos.config.name=sca-nacos-config",
				// "spring.cloud.nacos.config.refresh.enabled=false",
				"spring.cloud.nacos.config.encode=utf-8",
				"spring.cloud.nacos.config.shared-data-ids=base-common.properties,common.properties",
				"spring.cloud.nacos.config.refreshable-dataids=common.properties",
				"spring.cloud.nacos.config.ext-config[0].data-id=ext00.yaml",
				"spring.cloud.nacos.config.ext-config[1].data-id=ext01.yml",
				"spring.cloud.nacos.config.ext-config[1].group=EXT01_GROUP",
				"spring.cloud.nacos.config.ext-config[1].refresh=true",
				"spring.cloud.nacos.config.ext-config[2].data-id=ext02.yaml",
				"spring.profiles.active=develop", "server.port=19090" })
public class NacosPowerMockitBaseTests {

	private final static List<String> DATAIDS = Arrays.asList("common.properties",
			"base-common.properties", "ext00.yaml", "ext01.yml", "ext02.yaml",
			"sca-nacos-config.properties", "sca-nacos-config-develop.properties");

	private final static HashMap<String, Properties> VALUES = new HashMap<>();

	@Autowired
	protected ApplicationContext context;

	static {
		initDataIds();
		try {
			final Constructor constructor = ReflectionUtils.accessibleConstructor(
					NacosPropertySource.class, String.class, String.class, Map.class,
					Date.class, boolean.class);
			Method method = PowerMockito.method(NacosPropertySourceBuilder.class, "build",
					String.class, String.class, String.class, boolean.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {
					Properties properties = VALUES.get(args[0].toString());
					if (properties == null) {
						properties = new Properties();
						properties.put("user.name", args[0].toString());
					}
					Object instance = constructor.newInstance(args[1].toString(),
							args[0].toString(), properties, new Date(), args[3]);
					return instance;
				}
			});
		}
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private static void initDataIds() {
		DATAIDS.forEach(dataId -> {
			String realpath = "/" + dataId;
			ClassPathResource classPathResource = new ClassPathResource(realpath);
			if (realpath.endsWith("properties")) {
				Properties properties = new Properties();
				try {
					properties.load(classPathResource.getInputStream());
					VALUES.put(dataId, properties);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (realpath.endsWith("yaml") || realpath.endsWith("yml")) {
				YamlPropertiesFactoryBean yamlFactory = new YamlPropertiesFactoryBean();
				yamlFactory.setResources(classPathResource);
				try {
					VALUES.put(dataId, yamlFactory.getObject());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public NacosPropertySourceBuilder nacosPropertySourceBuilderInstance() {
		NacosConfigProperties nacosConfigProperties = this.context
				.getBean(NacosConfigProperties.class);

		ConfigService configService = nacosConfigProperties.configServiceInstance();
		long timeout = nacosConfigProperties.getTimeout();
		NacosPropertySourceBuilder nacosPropertySourceBuilder = new NacosPropertySourceBuilder(
				configService, timeout);
		return nacosPropertySourceBuilder;
	}

	@Configuration
	@AutoConfigureBefore(NacosConfigAutoConfiguration.class)
	static class TestConfiguration {

		@Autowired
		ConfigurableApplicationContext context;

		@Bean
		ContextRefresher contextRefresher() {
			RefreshScope refreshScope = new RefreshScope();
			refreshScope.setApplicationContext(context);
			return new ContextRefresher(context, refreshScope);
		}
	}

	@Test
	public void testAppContext() {
		System.err.println(this.context);
	}
}