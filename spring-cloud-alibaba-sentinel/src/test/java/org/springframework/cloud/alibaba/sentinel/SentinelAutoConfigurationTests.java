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

package org.springframework.cloud.alibaba.sentinel;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelAutoConfiguration;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelBeanPostProcessor;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelProtectInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author fangjian
 * @author jiashuai.xie
 */
public class SentinelAutoConfigurationTests {

	private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(SentinelAutoConfiguration.class,
					SentinelWebAutoConfiguration.class, SentinelTestConfiguration.class))
			.withPropertyValues("spring.cloud.sentinel.transport.port=8888")
			.withPropertyValues("spring.cloud.sentinel.filter.order=123")
			.withPropertyValues("spring.cloud.sentinel.filter.urlPatterns=/*,/test");

	@Test
	public void testFilter() {
		this.contextRunner.run(context -> {
			assertThat(context.getBean("servletRequestListener")
					.getClass() == FilterRegistrationBean.class).isTrue();
		});
	}

	@Test
	public void testBeanPostProcessor() {
		this.contextRunner.run(context -> {
			assertThat(context.getBean("sentinelBeanPostProcessor")
					.getClass() == SentinelBeanPostProcessor.class).isTrue();
		});
	}

	@Test
	public void testProperties() {
		this.contextRunner.run(context -> {
			SentinelProperties sentinelProperties = context
					.getBean(SentinelProperties.class);
			assertThat(sentinelProperties.getTransport().getPort()).isEqualTo("8888");
			assertThat(sentinelProperties.getFilter().getUrlPatterns().size())
					.isEqualTo(2);
			assertThat(sentinelProperties.getFilter().getUrlPatterns().get(0))
					.isEqualTo("/*");
			assertThat(sentinelProperties.getFilter().getUrlPatterns().get(1))
					.isEqualTo("/test");
		});
	}

	@Test
	public void testRestTemplate() {
		this.contextRunner.run(context -> {
			assertThat(context.getBeansOfType(RestTemplate.class).size()).isEqualTo(2);
			RestTemplate restTemplate = context.getBean("restTemplateWithBlockClass",
					RestTemplate.class);
			assertThat(restTemplate.getInterceptors().size()).isEqualTo(1);
			assertThat(restTemplate.getInterceptors().get(0).getClass())
					.isEqualTo(SentinelProtectInterceptor.class);
		});
	}

	@Configuration
	static class SentinelTestConfiguration {

		@Bean
		@SentinelRestTemplate
		RestTemplate restTemplate() {
			return new RestTemplate();
		}

		@Bean
		@SentinelRestTemplate(blockHandlerClass = ExceptionUtil.class, blockHandler = "handleException")
		RestTemplate restTemplateWithBlockClass() {
			return new RestTemplate();
		}

	}

	static class ExceptionUtil {
		public static void handleException(BlockException ex) {
			System.out.println("Oops: " + ex.getClass().getCanonicalName());
		}
	}

}
