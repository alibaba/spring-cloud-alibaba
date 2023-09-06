/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.nacos.util;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(SpringRunner.class)
@SpringBootTest(classes = InetIPv6UtilsTest.TestConfig.class,
		properties = { "spring.cloud.nacos.discovery.ipType=IPv6" },
		webEnvironment = RANDOM_PORT)
public class InetIPv6UtilsTest {

	@Autowired
	NacosDiscoveryProperties properties;

	@Test
	public void getIPv6() {
		String ip = properties.getIp();
		ip = ip.substring(1, ip.length() - 1);
		assert (InetAddressValidator.getInstance().isValidInet6Address(ip));
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration(NacosDiscoveryClientConfiguration.class)
	public static class TestConfig {

	}

}
