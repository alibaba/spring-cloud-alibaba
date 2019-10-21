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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author zkz
 */
@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.management.*")
@PowerMockRunnerDelegate(SpringRunner.class)
@PrepareForTest({ NacosConfigService.class })
@SpringBootTest(classes = NacosConfigurationXmlJsonTest.TestConfig.class,
		properties = { "spring.application.name=xmlApp", "spring.profiles.active=dev",
				"spring.cloud.nacos.config.server-addr=127.0.0.1:8848",
				"spring.cloud.nacos.config.namespace=test-namespace",
				"spring.cloud.nacos.config.encode=utf-8",
				"spring.cloud.nacos.config.timeout=1000",
				"spring.cloud.nacos.config.group=test-group",
				"spring.cloud.nacos.config.name=test-name",
				"spring.cloud.nacos.config.cluster-name=test-cluster",
				"spring.cloud.nacos.config.file-extension=xml",
				"spring.cloud.nacos.config.contextPath=test-contextpath",
				"spring.cloud.nacos.config.ext-config[0].data-id=ext-json-test.json",
				"spring.cloud.nacos.config.ext-config[1].data-id=ext-common02.properties",
				"spring.cloud.nacos.config.ext-config[1].group=GLOBAL_GROUP",
				"spring.cloud.nacos.config.shared-dataids=shared-data1.properties",
				"spring.cloud.nacos.config.accessKey=test-accessKey",
				"spring.cloud.nacos.config.secretKey=test-secretKey" },
		webEnvironment = NONE)
public class NacosConfigurationXmlJsonTest {

	static {

		try {

			Method method = PowerMockito.method(NacosConfigService.class, "getConfig",
					String.class, String.class, long.class);
			MethodProxy.proxy(method, new InvocationHandler() {
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
						throws Throwable {

					if ("xmlApp.xml".equals(args[0]) && "test-group".equals(args[1])) {
						return "<top>\n" + "    <first>one</first>\n"
								+ "    <sencond value=\"two\">\n"
								+ "        <third>three</third>\n" + "    </sencond>\n"
								+ "</top>";
					}
					if ("test-name.xml".equals(args[0]) && "test-group".equals(args[1])) {
						return "<Server port=\"8005\" shutdown=\"SHUTDOWN\"> \n"
								+ "    <Service name=\"Catalina\"> \n"
								+ "        <Connector value=\"第二个连接器\"> \n"
								+ "            <open>开启服务</open> \n"
								+ "            <init>初始化一下</init> \n"
								+ "            <process>\n" + "                <top>\n"
								+ "                    <first>one</first>\n"
								+ "                    <sencond value=\"two\">\n"
								+ "                        <third>three</third>\n"
								+ "                    </sencond>\n"
								+ "                </top>\n" + "            </process> \n"
								+ "            <destory>销毁一下</destory> \n"
								+ "            <close>关闭服务</close> \n"
								+ "        </Connector> \n" + "    </Service> \n"
								+ "</Server> ";
					}

					if ("test-name-dev.xml".equals(args[0])
							&& "test-group".equals(args[1])) {
						return "<application android:label=\"@string/app_name\" android:icon=\"@drawable/osg\">\n"
								+ "    <activity android:name=\".osgViewer\"\n"
								+ "              android:label=\"@string/app_name\" android:screenOrientation=\"landscape\">\n"
								+ "        <intent-filter>\n"
								+ "            <action android:name=\"android.intent.action.MAIN\" />\n"
								+ "            <category android:name=\"android.intent.category.LAUNCHER\" />\n"
								+ "        </intent-filter>\n" + "    </activity>\n"
								+ "</application>";
					}

					if ("ext-json-test.json".equals(args[0])
							&& "DEFAULT_GROUP".equals(args[1])) {
						return "{\n" + "    \"people\":{\n"
								+ "        \"firstName\":\"Brett\",\n"
								+ "        \"lastName\":\"McLaughlin\"\n" + "    }\n"
								+ "}";
					}

					if ("ext-config-common02.properties".equals(args[0])
							&& "GLOBAL_GROUP".equals(args[1])) {
						return "global-ext-config=global-config-value-2";
					}

					if ("shared-data1.properties".equals(args[0])
							&& "DEFAULT_GROUP".equals(args[1])) {
						return "shared-name=shared-value-1";
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
	private ApplicationContext applicationContext;

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

		checkoutEndpoint();

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
		assertThat(properties.getFileExtension()).isEqualTo("xml");
	}

	private void checkoutNacosConfigTimeout() {
		assertThat(properties.getTimeout()).isEqualTo(1000);
	}

	private void checkoutNacosConfigEncode() {
		assertThat(properties.getEncode()).isEqualTo("utf-8");
	}

	private void checkoutEndpoint() throws Exception {
		NacosConfigEndpoint nacosConfigEndpoint = new NacosConfigEndpoint(properties,
				refreshHistory);
		nacosConfigEndpoint.setApplicationContext(applicationContext);
		Map<String, Object> map = nacosConfigEndpoint.invoke();
		Map<String, Object> nacosCloud = (Map<String, Object>) map.get("nacosCloud");
		assertThat(properties).isEqualTo(map.get("NacosConfigProperties"));
		assertThat(refreshHistory.getRecords())
				.isEqualTo(nacosCloud.get("RefreshHistory"));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}

}
