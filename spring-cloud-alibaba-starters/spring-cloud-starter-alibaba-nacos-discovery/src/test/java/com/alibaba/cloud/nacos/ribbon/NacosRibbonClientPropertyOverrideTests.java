/*
 * Copyright 2013-2017 the original author or authors.
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

package com.alibaba.cloud.nacos.ribbon;

import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.netflix.loadbalancer.ConfigurationBasedServerList;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.cloud.netflix.archaius.ArchaiusAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author liujunjie
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NacosRibbonClientPropertyOverrideTests.TestConfiguration.class,
		properties = { "spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
				"spring.cloud.nacos.discovery.port=18080",
				"spring.cloud.nacos.discovery.service=remoteApp",
				"localApp.ribbon.NIWSServerListClassName="
						+ "com.netflix.loadbalancer.ConfigurationBasedServerList",
				"localApp.ribbon.listOfServers=127.0.0.1:19090",
				"localApp.ribbon.ServerListRefreshInterval=15000" })
public class NacosRibbonClientPropertyOverrideTests {

	@Autowired
	private SpringClientFactory factory;

	@Test
	public void serverListOverridesToTest() {
		ConfigurationBasedServerList.class
				.cast(getLoadBalancer("localApp").getServerListImpl());
	}

	@Test
	public void serverListRemoteTest() {
		NacosServerList.class.cast(getLoadBalancer("remoteApp").getServerListImpl());
	}

	@SuppressWarnings("unchecked")
	private ZoneAwareLoadBalancer<Server> getLoadBalancer(String name) {
		return (ZoneAwareLoadBalancer<Server>) this.factory.getLoadBalancer(name);
	}

	@Configuration
	@RibbonClients
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ UtilAutoConfiguration.class,
			PropertyPlaceholderAutoConfiguration.class, ArchaiusAutoConfiguration.class,
			RibbonNacosAutoConfiguration.class, NacosDiscoveryClientConfiguration.class })
	protected static class TestConfiguration {

	}

}
