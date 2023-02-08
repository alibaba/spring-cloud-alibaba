/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.governance.istio;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import javax.annotation.Resource;

import com.alibaba.cloud.commons.io.FileUtils;
import com.alibaba.cloud.governance.auth.AuthenticationAutoConfiguration;
import com.alibaba.cloud.governance.auth.repository.AuthRepository;
import com.alibaba.cloud.governance.istio.protocol.impl.LdsProtocol;
import com.alibaba.cloud.governance.istio.protocol.impl.RdsProtocol;
import com.alibaba.cloud.routing.RoutingAutoConfiguration;
import com.alibaba.cloud.routing.repository.FilterService;
import com.alibaba.cloud.routing.repository.RoutingDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.envoyproxy.envoy.config.listener.v3.Listener;
import io.envoyproxy.envoy.config.route.v3.RouteConfiguration;
import io.envoyproxy.envoy.service.discovery.v3.DiscoveryResponse;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * @author musi
 * @author <a href="liuziming@buaa.edu.cn"></a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = XdsRulesTests.TestConfig.class,
		properties = { "spring.cloud.istio.config.port=15010",
				"spring.cloud.istio.config.enabled=true",
				"spring.cloud.istio.config.log-xds=false",
				"spring.cloud.nacos.discovery.watch.enabled=false" },
		webEnvironment = NONE)
public class XdsRulesTests {

	private static final Logger log = LoggerFactory.getLogger(XdsRulesTests.class);

	private static final String TARGET_SERVICE = "service-provider";

	@Autowired
	private AuthRepository authRepository;

	@Autowired
	private RoutingDataRepository routeDataRepository;

	@Autowired
	private LdsProtocol ldsProtocol;

	@Autowired
	private RdsProtocol rdsProtocol;

	@Resource
	private ObjectMapper objectMapper;

	private DiscoveryResponse decodeResponse(String path) throws Exception {
		File file = new File(path);
		FileInputStream stream = FileUtils.openInputStream(file);
		byte[] bytes = new byte[(int) file.length()];
		int readBytes = stream.read(bytes);
		if (readBytes == -1) {
			throw new Exception("Unreadable response file");
		}
		return DiscoveryResponse.parseFrom(bytes);
	}

	@Test
	public void testAuthTransform() throws Exception {
		DiscoveryResponse discoveryResponse = decodeResponse(
				"src/test/resources/LdsResponse.in");
		List<Listener> listeners = ldsProtocol.decodeXdsResponse(discoveryResponse);
		if (listeners == null) {
			throw new Exception("Can not parse listeners from xds response");
		}
		log.info("Auth rules are {}", objectMapper.writeValueAsString(authRepository));
		Assert.assertEquals(authRepository.getAllowAuthRules().size(), 1);
		Assert.assertEquals(authRepository.getDenyAuthRules().size(), 1);
		Assert.assertEquals(authRepository.getJwtRules().size(), 1);
	}

	@Test
	public void testLabelRoutingTransform() throws Exception {
		DiscoveryResponse discoveryResponse = decodeResponse(
				"src/test/resources/RdsResponse.in");
		List<RouteConfiguration> routeConfigurations = rdsProtocol
				.decodeXdsResponse(discoveryResponse);
		if (routeConfigurations == null) {
			throw new Exception("Can not parse route configurations from xds response");
		}
		if (routeDataRepository.getRouteRule(TARGET_SERVICE) == null) {
			throw new Exception("Can not get target service from route configurations");
		}
		log.info("Label routing rules are {}", objectMapper
				.writeValueAsString(routeDataRepository.getRouteRule(TARGET_SERVICE)));
	}

	/**
	 * dummy class for label routing filter service.
	 */
	static class Dummy {

	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ XdsAutoConfiguration.class,
			AuthenticationAutoConfiguration.class, RoutingAutoConfiguration.class })
	public static class TestConfig {

		@Bean(name = TARGET_SERVICE + FilterService.FEIGN_CLIENT_BEAN_SPECIFICATION)
		public Dummy dummy() {
			return new Dummy();
		}

	}

}
