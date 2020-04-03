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

package com.alibaba.cloud.nacos.discovery;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.commons.util.UtilAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:echooy.mxq@gmail.com">echooymxq</a>
 **/
public class NacosDiscoveryClientConfigurationTest {

	private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(UtilAutoConfiguration.class,
					NacosDiscoveryAutoConfiguration.class,
					NacosDiscoveryClientConfiguration.class, this.getClass()));

	@Bean
	public TaskScheduler taskScheduler() {
		return new ThreadPoolTaskScheduler();
	}

	@Test
	public void testDefaultInitialization() {
		contextRunner.run(context -> {
			assertThat(context).hasSingleBean(DiscoveryClient.class);
			assertThat(context).hasSingleBean(NacosWatch.class);
		});
	}

	@Test
	public void testDiscoveryBlockingDisabled() {
		contextRunner.withPropertyValues("spring.cloud.discovery.blocking.enabled=false")
				.run(context -> {
					assertThat(context).doesNotHaveBean(DiscoveryClient.class);
					assertThat(context).doesNotHaveBean(NacosWatch.class);
				});
	}

}
