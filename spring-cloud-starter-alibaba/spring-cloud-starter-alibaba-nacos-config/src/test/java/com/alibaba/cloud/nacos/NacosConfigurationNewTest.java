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

package com.alibaba.cloud.nacos;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.alibaba.cloud.nacos.NacosConfigProperties.Config;
import com.alibaba.cloud.nacos.client.NacosPropertySourceLocator;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpoint;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import com.alibaba.nacos.client.config.NacosConfigService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.support.MethodProxy;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author xiaojing
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ NacosConfigService.class })
@SpringBootTest(classes = NacosConfigurationNewTest.TestConfig.class, properties = {
		"spring.application.name=myTestService1", "spring.profiles.active=dev,test",
		"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.config.namespace=test-namespace",
		"spring.cloud.nacos.config.encode=utf-8",
		"spring.cloud.nacos.config.timeout=1000",
		"spring.cloud.nacos.config.group=test-group",
		"spring.cloud.nacos.config.name=test-name",
		"spring.cloud.nacos.config.cluster-name=test-cluster",
		"spring.cloud.nacos.config.file-extension=properties",
		"spring.cloud.nacos.config.contextPath=test-contextpath",
		"spring.cloud.nacos.config.extension-configs[0].data-id=ext-config-common01.properties",
		"spring.cloud.nacos.config.extension-configs[1].data-id=ext-config-common02.properties",
		"spring.cloud.nacos.config.extension-configs[1].group=GLOBAL_GROUP",
		"spring.cloud.nacos.config.shared-configs[0]=common1.properties",
		"spring.cloud.nacos.config.shared-configs[1]=common2.properties",
		"spring.cloud.nacos.config.accessKey=test-accessKey",
		"spring.cloud.nacos.config.secretKey=test-secretKey" }, webEnvironment = NONE)
public class NacosConfigurationNewTest {

	static {

		try {

			Method method = PowerMockito.method(NacosConfigService.class, "getConfig",
					String.class, String.class, long.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {

					if ("test-name.properties".equals(args[0])
							&& "test-group".equals(args[1])) {
						return "user.name=hello\nuser.age=12";
					}

					if ("test-name-dev.properties".equals(args[0])
							&& "test-group".equals(args[1])) {
						return "user.name=dev";
					}

					if ("ext-config-common01.properties".equals(args[0])
							&& "DEFAULT_GROUP".equals(args[1])) {
						return "test-ext-config1=config1\ntest-ext-config2=config1";
					}
					if ("ext-config-common02.properties".equals(args[0])
							&& "GLOBAL_GROUP".equals(args[1])) {
						return "test-ext-config2=config2";
					}

					if ("common1.properties".equals(args[0])
							&& "DEFAULT_GROUP".equals(args[1])) {
						return "test-common1=common1\ntest-common2=common1";
					}

					if ("common2.properties".equals(args[0])
							&& "DEFAULT_GROUP".equals(args[1])) {
						return "test-common2=common2";
					}

					return "";
				}
			});

		}
		catch (Exception ignore) {
			ignore.printStackTrace();

		}
	}

	@Autowired
	private Environment environment;

	@Autowired
	private NacosPropertySourceLocator locator;

	@Autowired
	private NacosConfigProperties properties;

	@Autowired
	private NacosRefreshHistory refreshHistory;

	@Test
	public void contextLoads() throws Exception {

		assertThat(locator).isNotNull();
		assertThat(properties).isNotNull();

		checkoutNacosConfigServerAddr();
		checkoutNacosConfigNamespace();
		checkoutNacosConfigClusterName();
		checkoutNacosConfigAccessKey();
		checkoutNacosConfigSecrectKey();
		checkoutNacosConfigName();
		checkoutNacosConfigGroup();
		checkoutNacosConfigContextPath();
		checkoutNacosConfigFileExtension();
		checkoutNacosConfigTimeout();
		checkoutNacosConfigEncode();
		checkShareConfigs();
		checkExtensionConfigs();

		checkoutEndpoint();
		checkoutDataLoad();
		System.out.println(properties.toString());
	}

	private void checkoutNacosConfigServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
	}

	private void checkoutNacosConfigNamespace() {
		assertThat(properties.getNamespace()).isEqualTo("test-namespace");
	}

	private void checkoutNacosConfigClusterName() {
		assertThat(properties.getClusterName()).isEqualTo("test-cluster");
	}

	private void checkoutNacosConfigAccessKey() {
		assertThat(properties.getAccessKey()).isEqualTo("test-accessKey");
	}

	private void checkoutNacosConfigSecrectKey() {
		assertThat(properties.getSecretKey()).isEqualTo("test-secretKey");
	}

	private void checkoutNacosConfigContextPath() {
		assertThat(properties.getContextPath()).isEqualTo("test-contextpath");
	}

	private void checkoutNacosConfigName() {
		assertThat(properties.getName()).isEqualTo("test-name");
	}

	private void checkoutNacosConfigGroup() {
		assertThat(properties.getGroup()).isEqualTo("test-group");
	}

	private void checkoutNacosConfigFileExtension() {
		assertThat(properties.getFileExtension()).isEqualTo("properties");
	}

	private void checkoutNacosConfigTimeout() {
		assertThat(properties.getTimeout()).isEqualTo(1000);
	}

	private void checkoutNacosConfigEncode() {
		assertThat(properties.getEncode()).isEqualTo("utf-8");
	}

	private void checkoutDataLoad() {
		assertThat(environment.getProperty("user.name")).isEqualTo("dev");
		assertThat(environment.getProperty("user.age")).isEqualTo("12");
	}

	private void checkoutEndpoint() throws Exception {
		NacosConfigEndpoint nacosConfigEndpoint = new NacosConfigEndpoint(properties,
				refreshHistory);
		Map<String, Object> map = nacosConfigEndpoint.invoke();
		assertThat(map.get("NacosConfigProperties")).isEqualTo(properties);
		assertThat(map.get("RefreshHistory")).isEqualTo(refreshHistory.getRecords());
	}

	private void checkShareConfigs() throws Exception {
		assertThat(properties.getSharedConfigs() != null);
		assertThat(properties.getSharedConfigs()).contains(
				new Config("common1.properties"), new Config("common2.properties"));
	}

	private void checkExtensionConfigs() throws Exception {
		assertThat(properties.getExtensionConfigs() != null);
		assertThat(properties.getExtensionConfigs()).contains(
				new Config("ext-config-common01.properties"),
				new Config("ext-config-common02.properties", "GLOBAL_GROUP"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
