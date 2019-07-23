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

package com.alibaba.cloud.nacos;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.junit.Assert;
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

import com.alibaba.cloud.nacos.client.NacosPropertySourceLocator;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpoint;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import com.alibaba.cloud.nacos.refresh.NacosRefreshHistory;
import com.alibaba.nacos.client.config.NacosConfigService;

/**
 * @author xiaojing
 */

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ NacosConfigService.class })
@SpringBootTest(classes = NacosConfigurationTests.TestConfig.class, properties = {
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
		"spring.cloud.nacos.config.ext-config[0].data-id=ext-config-common01.properties",
		"spring.cloud.nacos.config.ext-config[1].data-id=ext-config-common02.properties",
		"spring.cloud.nacos.config.ext-config[1].group=GLOBAL_GROUP",
		"spring.cloud.nacos.config.shared-dataids=common1.properties,common2.properties",
		"spring.cloud.nacos.config.accessKey=test-accessKey",
		"spring.cloud.nacos.config.secretKey=test-secretKey" }, webEnvironment = NONE)
public class NacosConfigurationTests {

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

		assertNotNull("NacosPropertySourceLocator was not created", locator);
		assertNotNull("NacosConfigProperties was not created", properties);

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

		checkoutEndpoint();
		checkoutDataLoad();

	}

	private void checkoutNacosConfigServerAddr() {
		assertEquals("NacosConfigProperties server address is wrong", "127.0.0.1:8848",
				properties.getServerAddr());
	}

	private void checkoutNacosConfigNamespace() {
		assertEquals("NacosConfigProperties namespace is wrong", "test-namespace",
				properties.getNamespace());
	}

	private void checkoutNacosConfigClusterName() {
		assertEquals("NacosConfigProperties' cluster is wrong", "test-cluster",
				properties.getClusterName());
	}

	private void checkoutNacosConfigAccessKey() {
		assertEquals("NacosConfigProperties' is access key is wrong", "test-accessKey",
				properties.getAccessKey());
	}

	private void checkoutNacosConfigSecrectKey() {
		assertEquals("NacosConfigProperties' is secret key is wrong", "test-secretKey",
				properties.getSecretKey());
	}

	private void checkoutNacosConfigContextPath() {
		assertEquals("NacosConfigProperties' context path is wrong", "test-contextpath",
				properties.getContextPath());
	}

	private void checkoutNacosConfigName() {
		assertEquals("NacosConfigProperties' name is wrong", "test-name",
				properties.getName());
	}

	private void checkoutNacosConfigGroup() {
		assertEquals("NacosConfigProperties' group is wrong", "test-group",
				properties.getGroup());
	}

	private void checkoutNacosConfigFileExtension() {
		assertEquals("NacosConfigProperties' file extension is wrong", "properties",
				properties.getFileExtension());
	}

	private void checkoutNacosConfigTimeout() {
		assertEquals("NacosConfigProperties' timeout is wrong", 1000,
				properties.getTimeout());
	}

	private void checkoutNacosConfigEncode() {
		assertEquals("NacosConfigProperties' encode is wrong", "utf-8",
				properties.getEncode());
	}

	private void checkoutDataLoad() {

		Assert.assertEquals("dev", environment.getProperty("user.name"));
		Assert.assertEquals("12", environment.getProperty("user.age"));
	}

	private void checkoutEndpoint() throws Exception {
		NacosConfigEndpoint nacosConfigEndpoint = new NacosConfigEndpoint(properties,
				refreshHistory);
		Map<String, Object> map = nacosConfigEndpoint.invoke();
		assertEquals(map.get("NacosConfigProperties"), properties);
		assertEquals(map.get("RefreshHistory"), refreshHistory.getRecords());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {
	}
}
