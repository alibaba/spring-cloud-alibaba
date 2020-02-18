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

package com.alibaba.cloud.sentinel;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.DefaultBlockExceptionHandler;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.config.SentinelWebMvcConfig;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SentinelBeanAutowiredTests.TestConfig.class },
		properties = { "spring.cloud.sentinel.filter.order=111" })
public class SentinelBeanAutowiredTests {

	@Autowired
	private UrlCleaner urlCleaner;

	@Autowired
	private BlockExceptionHandler blockExceptionHandler;

	@Autowired
	private RequestOriginParser requestOriginParser;

	@Autowired
	private SentinelProperties sentinelProperties;

	@Autowired
	private SentinelWebMvcConfig sentinelWebMvcConfig;

	@Test
	public void contextLoads() throws Exception {
		assertThat(urlCleaner).isNotNull();
		assertThat(blockExceptionHandler).isNotNull();
		assertThat(requestOriginParser).isNotNull();
		assertThat(sentinelProperties).isNotNull();

		checkUrlPattern();
	}

	private void checkUrlPattern() {
		assertThat(sentinelProperties.getFilter().getOrder()).isEqualTo(111);
		assertThat(sentinelProperties.getFilter().getUrlPatterns().size()).isEqualTo(1);
		assertThat(sentinelProperties.getFilter().getUrlPatterns().get(0))
				.isEqualTo("/**");
	}

	@Test
	public void testBeanAutowired() {
		assertThat(sentinelWebMvcConfig.getUrlCleaner()).isEqualTo(urlCleaner);
		assertThat(sentinelWebMvcConfig.getBlockExceptionHandler())
				.isEqualTo(blockExceptionHandler);
		assertThat(sentinelWebMvcConfig.getOriginParser()).isEqualTo(requestOriginParser);
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ SentinelAutoConfiguration.class,
			SentinelWebAutoConfiguration.class })
	public static class TestConfig {

		@Bean
		public UrlCleaner urlCleaner() {
			return new UrlCleaner() {
				@Override
				public String clean(String s) {
					return s;
				}
			};
		}

		@Bean
		public RequestOriginParser requestOriginParser() {
			return new RequestOriginParser() {
				@Override
				public String parseOrigin(HttpServletRequest httpServletRequest) {
					return httpServletRequest.getRemoteAddr();
				}
			};
		}

		@Bean
		public BlockExceptionHandler blockExceptionHandler() {
			return new DefaultBlockExceptionHandler();
		}

	}

}
