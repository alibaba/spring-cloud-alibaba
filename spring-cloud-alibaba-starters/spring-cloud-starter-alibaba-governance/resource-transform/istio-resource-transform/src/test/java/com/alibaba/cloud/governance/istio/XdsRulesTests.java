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

package com.alibaba.cloud.governance.istio;

import com.alibaba.cloud.data.controlsurface.ControlSurfaceConnectionAutoConfiguration;
import com.alibaba.cloud.governance.auth.rules.manager.IpBlockRuleManager;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = XdsRulesTests.TestConfig.class,
		properties = { "spring.cloud.istio.config.host=39.105.35.234",
				"spring.cloud.istio.config.port=15010",
				"spring.cloud.istio.config.enabled=true",
				"spring.cloud.istio.config.polling-pool-size=10",
				"spring.cloud.istio.config.polling-time=30" },
		webEnvironment = NONE)
public class XdsRulesTests {

	@Test
	public void testIpBlockRules() {
		boolean isAllow = IpBlockRuleManager.isValid("127.0.0.1", "10.2.5.4",
				"192.168.6.7");
		Assertions.assertTrue(isAllow);
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ XdsAutoConfiguration.class,
			ControlSurfaceConnectionAutoConfiguration.class })
	public static class TestConfig {

	}

}
