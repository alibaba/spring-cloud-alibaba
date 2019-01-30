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

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelRestTemplate;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelAutoConfiguration;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelBeanPostProcessor;
import org.springframework.cloud.alibaba.sentinel.endpoint.SentinelEndpoint;
import org.springframework.cloud.alibaba.sentinel.rest.SentinelClientHttpResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.alibaba.csp.sentinel.adapter.servlet.config.WebServletConfig;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @author jiashuai.xie
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
		SentinelAutoConfigurationTests.TestConfig.class }, properties = {
				"spring.cloud.sentinel.filter.order=123",
				"spring.cloud.sentinel.filter.urlPatterns=/*,/test",
				"spring.cloud.sentinel.metric.fileSingleSize=9999",
				"spring.cloud.sentinel.metric.fileTotalCount=100",
				"spring.cloud.sentinel.servlet.blockPage=/error",
				"spring.cloud.sentinel.flow.coldFactor=3",
				"spring.cloud.sentinel.eager=true",
				"spring.cloud.sentinel.log.switchPid=true",
				"spring.cloud.sentinel.transport.dashboard=http://localhost:8080",
				"spring.cloud.sentinel.transport.port=9999",
				"spring.cloud.sentinel.transport.clientIp=1.1.1.1",
				"spring.cloud.sentinel.transport.heartbeatIntervalMs=20000" }, webEnvironment = RANDOM_PORT)
public class SentinelAutoConfigurationTests {

	@Autowired
	private SentinelProperties sentinelProperties;

	@Autowired
	private FilterRegistrationBean filterRegistrationBean;

	@Autowired
	private SentinelBeanPostProcessor sentinelBeanPostProcessor;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestTemplate restTemplateWithBlockClass;

	@Autowired
	private RestTemplate restTemplateWithoutBlockClass;

	@Autowired
	private RestTemplate restTemplateWithFallbackClass;

	@LocalServerPort
	private int port;

	private String url = "http://localhost:" + port;

	@Before
	public void setUp() {
		FlowRule rule = new FlowRule();
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule.setCount(0);
		rule.setResource(url);
		rule.setLimitApp("default");
		rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
		FlowRuleManager.loadRules(Arrays.asList(rule));

		DegradeRule degradeRule = new DegradeRule();
		degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
		degradeRule.setResource(url + "/test");
		degradeRule.setCount(0);
		degradeRule.setTimeWindow(60);
		DegradeRuleManager.loadRules(Arrays.asList(degradeRule));
	}

	@Test
	public void contextLoads() throws Exception {
		assertNotNull("FilterRegistrationBean was not created", filterRegistrationBean);
		assertNotNull("SentinelProperties was not created", sentinelProperties);
		assertNotNull("SentinelBeanPostProcessor was not created",
				sentinelBeanPostProcessor);

		checkSentinelLog();
		checkSentinelEager();
		checkSentinelTransport();
		checkSentinelColdFactor();
		checkSentinelMetric();
		checkSentinelFilter();
		checkEndpoint();
	}

	private void checkEndpoint() {
		SentinelEndpoint sentinelEndpoint = new SentinelEndpoint(sentinelProperties);
		Map<String, Object> map = sentinelEndpoint.invoke();
		assertEquals("Endpoint Sentinel log pid was wrong", true, map.get("logUsePid"));
		assertEquals("Endpoint Sentinel transport console server was wrong",
				"http://localhost:8080", map.get("consoleServer"));
		assertEquals("Endpoint Sentinel transport port was wrong", "9999",
				map.get("clientPort"));
		assertEquals("Endpoint Sentinel transport heartbeatIntervalMs was wrong", 20000l,
				map.get("heartbeatIntervalMs"));
		assertEquals("Endpoint Sentinel transport clientIp was wrong", "1.1.1.1",
				map.get("clientIp"));
		assertEquals("Endpoint Sentinel metric file size was wrong", 9999l,
				map.get("metricsFileSize"));
		assertEquals("Endpoint Sentinel metric file count was wrong", 100,
				map.get("totalMetricsFileCount"));
		assertEquals("Endpoint Sentinel metric file charset was wrong", "UTF-8",
				map.get("metricsFileCharset"));
		assertEquals("Endpoint Sentinel block page was wrong", "/error",
				map.get("blockPage"));
	}

	private void checkSentinelFilter() {
		assertEquals("SentinelProperties filter order was wrong", 123,
				sentinelProperties.getFilter().getOrder());
		assertEquals("SentinelProperties filter url pattern size was wrong", 2,
				sentinelProperties.getFilter().getUrlPatterns().size());
		assertEquals("SentinelProperties filter url pattern item was wrong", "/*",
				sentinelProperties.getFilter().getUrlPatterns().get(0));
		assertEquals("SentinelProperties filter url pattern item was wrong", "/test",
				sentinelProperties.getFilter().getUrlPatterns().get(1));
	}

	private void checkSentinelMetric() {
		assertEquals("SentinelProperties metric charset was wrong", "UTF-8",
				sentinelProperties.getMetric().getCharset());
		assertEquals("SentinelProperties metric file single size was wrong", "9999",
				sentinelProperties.getMetric().getFileSingleSize());
		assertEquals("SentinelProperties metric file total count was wrong", "100",
				sentinelProperties.getMetric().getFileTotalCount());
	}

	private void checkSentinelColdFactor() {
		assertEquals("SentinelProperties coldFactor was wrong", "3",
				sentinelProperties.getFlow().getColdFactor());
	}

	private void checkSentinelTransport() {
		assertEquals("SentinelProperties transport port was wrong", "9999",
				sentinelProperties.getTransport().getPort());
		assertEquals("SentinelProperties transport dashboard was wrong",
				"http://localhost:8080",
				sentinelProperties.getTransport().getDashboard());
		assertEquals("SentinelProperties transport clientIp was wrong", "1.1.1.1",
				sentinelProperties.getTransport().getClientIp());
		assertEquals("SentinelProperties transport heartbeatIntervalMs was wrong",
				"20000", sentinelProperties.getTransport().getHeartbeatIntervalMs());
	}

	private void checkSentinelEager() {
		assertEquals("SentinelProperties eager was wrong", true,
				sentinelProperties.isEager());
	}

	private void checkSentinelLog() {
		assertEquals("SentinelProperties log file pid was wrong", true,
				sentinelProperties.getLog().isSwitchPid());
	}

	@Test
	public void testFilter() {
		assertEquals("Sentinel Filter order was wrong", filterRegistrationBean.getOrder(),
				123);
		assertEquals("Sentinel Filter url-pattern was wrong",
				filterRegistrationBean.getUrlPatterns().size(), 2);
	}

	@Test
	public void testSentinelSystemProperties() {
		assertEquals("Sentinel log pid was wrong", true, LogBase.isLogNameUsePid());
		assertEquals("Sentinel transport console server was wrong",
				"http://localhost:8080", TransportConfig.getConsoleServer());
		assertEquals("Sentinel transport port was wrong", "9999",
				TransportConfig.getPort());
		assertEquals("Sentinel transport heartbeatIntervalMs was wrong", 20000l,
				TransportConfig.getHeartbeatIntervalMs().longValue());
		assertEquals("Sentinel transport clientIp was wrong", "1.1.1.1",
				TransportConfig.getHeartbeatClientIp());
		assertEquals("Sentinel metric file size was wrong", 9999,
				SentinelConfig.singleMetricFileSize());
		assertEquals("Sentinel metric file count was wrong", 100,
				SentinelConfig.totalMetricFileCount());
		assertEquals("Sentinel metric file charset was wrong", "UTF-8",
				SentinelConfig.charset());
		assertEquals("Sentinel block page was wrong", "/error",
				WebServletConfig.getBlockPage());
	}

	@Test
	public void testFlowRestTemplate() {
		assertEquals("RestTemplate interceptors size was wrong", 2,
				restTemplate.getInterceptors().size());
		assertEquals("RestTemplateWithBlockClass interceptors size was wrong", 1,
				restTemplateWithBlockClass.getInterceptors().size());
		ResponseEntity responseEntityBlock = restTemplateWithBlockClass.getForEntity(url,
				String.class);
		assertEquals("RestTemplateWithBlockClass Sentinel Block Message was wrong",
				"Oops", responseEntityBlock.getBody());
		assertEquals(
				"RestTemplateWithBlockClass Sentinel Block Http Status Code was wrong",
				HttpStatus.OK, responseEntityBlock.getStatusCode());
		ResponseEntity responseEntityRaw = restTemplate.getForEntity(url, String.class);
		assertEquals("RestTemplate Sentinel Block Message was wrong",
				"RestTemplate request block by sentinel", responseEntityRaw.getBody());
		assertEquals("RestTemplate Sentinel Block Http Status Code was wrong",
				HttpStatus.OK, responseEntityRaw.getStatusCode());
	}

	@Test
	public void testNormalRestTemplate() {
		assertEquals("RestTemplateWithoutBlockClass interceptors size was wrong", 0,
				restTemplateWithoutBlockClass.getInterceptors().size());
		assertThatExceptionOfType(RestClientException.class).isThrownBy(() -> {
			restTemplateWithoutBlockClass.getForEntity(url, String.class);
		});
	}

	@Test
	public void testFallbackRestTemplate() {
		ResponseEntity responseEntity = restTemplateWithFallbackClass
				.getForEntity(url + "/test", String.class);
		assertEquals("RestTemplateWithFallbackClass Sentinel Message was wrong",
				"Oops fallback", responseEntity.getBody());
		assertEquals("RestTemplateWithFallbackClass Sentinel Http Status Code was wrong",
				HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Configuration
	static class SentinelTestConfiguration {

		@Bean
		@SentinelRestTemplate
		RestTemplate restTemplate() {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getInterceptors().add(mock(ClientHttpRequestInterceptor.class));
			return restTemplate;
		}

		@Bean
		@SentinelRestTemplate(blockHandlerClass = ExceptionUtil.class, blockHandler = "handleException")
		RestTemplate restTemplateWithBlockClass() {
			return new RestTemplate();
		}

		@Bean
		@SentinelRestTemplate(fallbackClass = ExceptionUtil.class, fallback = "fallbackException")
		RestTemplate restTemplateWithFallbackClass() {
			return new RestTemplate();
		}

		@Bean
		RestTemplate restTemplateWithoutBlockClass() {
			return new RestTemplate();
		}

	}

	public static class ExceptionUtil {
		public static SentinelClientHttpResponse handleException(HttpRequest request,
				byte[] body, ClientHttpRequestExecution execution, BlockException ex) {
			System.out.println("Oops: " + ex.getClass().getCanonicalName());
			return new SentinelClientHttpResponse("Oops");
		}

		public static SentinelClientHttpResponse fallbackException(HttpRequest request,
				byte[] body, ClientHttpRequestExecution execution, BlockException ex) {
			System.out.println("Oops: " + ex.getClass().getCanonicalName());
			return new SentinelClientHttpResponse("Oops fallback");
		}
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ SentinelAutoConfiguration.class,
			SentinelWebAutoConfiguration.class, SentinelTestConfiguration.class })
	public static class TestConfig {
	}

}
