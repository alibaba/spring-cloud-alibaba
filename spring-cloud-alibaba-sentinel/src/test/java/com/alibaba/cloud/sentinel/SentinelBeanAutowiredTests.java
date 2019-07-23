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

package com.alibaba.cloud.sentinel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
import com.alibaba.csp.sentinel.adapter.servlet.util.FilterUtil;
import com.alibaba.csp.sentinel.slots.block.BlockException;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SentinelBeanAutowiredTests.TestConfig.class }, properties = {
		"spring.cloud.sentinel.filter.order=111" })
public class SentinelBeanAutowiredTests {

	@Autowired
	private UrlCleaner urlCleaner;

	@Autowired
	private UrlBlockHandler urlBlockHandler;

	@Autowired
	private RequestOriginParser requestOriginParser;

	@Autowired
	private SentinelProperties sentinelProperties;

	@Test
	public void contextLoads() throws Exception {
		assertNotNull("UrlCleaner was not created", urlCleaner);
		assertNotNull("UrlBlockHandler was not created", urlBlockHandler);
		assertNotNull("RequestOriginParser was not created", requestOriginParser);
		assertNotNull("SentinelProperties was not created", sentinelProperties);

		checkUrlPattern();
	}

	private void checkUrlPattern() {
		assertEquals("SentinelProperties filter order was wrong", 111,
				sentinelProperties.getFilter().getOrder());
		assertEquals("SentinelProperties filter url pattern size was wrong", 1,
				sentinelProperties.getFilter().getUrlPatterns().size());
		assertEquals("SentinelProperties filter url pattern was wrong", "/*",
				sentinelProperties.getFilter().getUrlPatterns().get(0));
	}

	@Test
	public void testBeanAutowired() {
		assertEquals("UrlCleaner was not autowired", urlCleaner,
				WebCallbackManager.getUrlCleaner());
		assertEquals("UrlBlockHandler was not autowired", urlBlockHandler,
				WebCallbackManager.getUrlBlockHandler());
		assertEquals("RequestOriginParser was not autowired", requestOriginParser,
				WebCallbackManager.getRequestOriginParser());
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
		public UrlBlockHandler urlBlockHandler() {
			return new UrlBlockHandler() {
				@Override
				public void blocked(HttpServletRequest httpServletRequest,
						HttpServletResponse httpServletResponse, BlockException e)
						throws IOException {
					FilterUtil.blockRequest(httpServletRequest, httpServletResponse);
				}
			};
		}

	}

}
