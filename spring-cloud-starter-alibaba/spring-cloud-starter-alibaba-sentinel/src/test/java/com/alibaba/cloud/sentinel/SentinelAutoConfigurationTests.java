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

import java.util.Arrays;
import java.util.Map;

import com.alibaba.cloud.sentinel.annotation.SentinelRestTemplate;
import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.alibaba.cloud.sentinel.custom.SentinelBeanPostProcessor;
import com.alibaba.cloud.sentinel.endpoint.SentinelEndpoint;
import com.alibaba.cloud.sentinel.rest.SentinelClientHttpResponse;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.transport.config.TransportConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
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

import static com.alibaba.cloud.sentinel.SentinelConstants.BLOCK_PAGE_URL_CONF_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @author jiashuai.xie
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SentinelAutoConfigurationTests.TestConfig.class },
		properties = { "spring.cloud.sentinel.filter.order=123",
				"spring.cloud.sentinel.filter.urlPatterns=/*,/test",
				"spring.cloud.sentinel.metric.fileSingleSize=9999",
				"spring.cloud.sentinel.metric.fileTotalCount=100",
				"spring.cloud.sentinel.blockPage=/error",
				"spring.cloud.sentinel.flow.coldFactor=3",
				"spring.cloud.sentinel.eager=true",
				"spring.cloud.sentinel.log.switchPid=true",
				"spring.cloud.sentinel.transport.dashboard=http://localhost:8080",
				"spring.cloud.sentinel.transport.port=9999",
				"spring.cloud.sentinel.transport.clientIp=1.1.1.1",
				"spring.cloud.sentinel.transport.heartbeatIntervalMs=20000" },
		webEnvironment = RANDOM_PORT)
public class SentinelAutoConfigurationTests {

	@Autowired
	private SentinelProperties sentinelProperties;

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

	private String flowUrl = "http://localhost:" + port + "/flow";

	private String degradeUrl = "http://localhost:" + port + "/degrade";

	@Before
	public void setUp() {
		FlowRule rule = new FlowRule();
		rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
		rule.setCount(0);
		rule.setResource("GET:" + flowUrl);
		rule.setLimitApp("default");
		rule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
		rule.setStrategy(RuleConstant.STRATEGY_DIRECT);
		FlowRuleManager.loadRules(Arrays.asList(rule));

		DegradeRule degradeRule = new DegradeRule();
		degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
		degradeRule.setResource("GET:" + degradeUrl);
		degradeRule.setCount(0);
		degradeRule.setTimeWindow(60);
		DegradeRuleManager.loadRules(Arrays.asList(degradeRule));
	}

	@Test
	public void contextLoads() throws Exception {
		assertThat(sentinelBeanPostProcessor).isNotNull();

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

		assertThat(map.get("logUsePid")).isEqualTo(Boolean.TRUE);
		assertThat(map.get("consoleServer")).isEqualTo("http://localhost:8080");
		assertThat(map.get("clientPort")).isEqualTo("9999");
		assertThat(map.get("heartbeatIntervalMs")).isEqualTo(20000L);
		assertThat(map.get("clientIp")).isEqualTo("1.1.1.1");
		assertThat(map.get("metricsFileSize")).isEqualTo(9999L);
		assertThat(map.get("totalMetricsFileCount")).isEqualTo(100);
		assertThat(map.get("metricsFileCharset")).isEqualTo("UTF-8");
		assertThat(map.get("blockPage")).isEqualTo("/error");
	}

	private void checkSentinelFilter() {
		assertThat(sentinelProperties.getFilter().getOrder()).isEqualTo(123);
		assertThat(sentinelProperties.getFilter().getUrlPatterns().size()).isEqualTo(2);
		assertThat(sentinelProperties.getFilter().getUrlPatterns().get(0))
				.isEqualTo("/*");
		assertThat(sentinelProperties.getFilter().getUrlPatterns().get(1))
				.isEqualTo("/test");
	}

	private void checkSentinelMetric() {
		assertThat(sentinelProperties.getMetric().getCharset()).isEqualTo("UTF-8");
		assertThat(sentinelProperties.getMetric().getFileSingleSize()).isEqualTo("9999");
		assertThat(sentinelProperties.getMetric().getFileTotalCount()).isEqualTo("100");
	}

	private void checkSentinelColdFactor() {
		assertThat(sentinelProperties.getFlow().getColdFactor()).isEqualTo("3");
	}

	private void checkSentinelTransport() {
		assertThat(sentinelProperties.getTransport().getPort()).isEqualTo("9999");
		assertThat(sentinelProperties.getTransport().getDashboard())
				.isEqualTo("http://localhost:8080");
		assertThat(sentinelProperties.getTransport().getClientIp()).isEqualTo("1.1.1.1");
		assertThat(sentinelProperties.getTransport().getHeartbeatIntervalMs())
				.isEqualTo("20000");
	}

	private void checkSentinelEager() {
		assertThat(sentinelProperties.isEager()).isEqualTo(true);
	}

	private void checkSentinelLog() {
		assertThat(sentinelProperties.getLog().isSwitchPid()).isEqualTo(true);
	}

	@Test
	public void testSentinelSystemProperties() {
		assertThat(LogBase.isLogNameUsePid()).isEqualTo(true);
		assertThat(TransportConfig.getConsoleServer()).isEqualTo("http://localhost:8080");
		assertThat(TransportConfig.getPort()).isEqualTo("9999");
		assertThat(TransportConfig.getHeartbeatIntervalMs().longValue())
				.isEqualTo(20000L);
		assertThat(TransportConfig.getHeartbeatClientIp()).isEqualTo("1.1.1.1");
		assertThat(SentinelConfig.singleMetricFileSize()).isEqualTo(9999);
		assertThat(SentinelConfig.totalMetricFileCount()).isEqualTo(100);
		assertThat(SentinelConfig.charset()).isEqualTo("UTF-8");
		assertThat(SentinelConfig.getConfig(BLOCK_PAGE_URL_CONF_KEY)).isEqualTo("/error");
	}

	@Test
	public void testFlowRestTemplate() {

		assertThat(restTemplate.getInterceptors().size()).isEqualTo(2);
		assertThat(restTemplateWithBlockClass.getInterceptors().size()).isEqualTo(1);

		ResponseEntity responseEntityBlock = restTemplateWithBlockClass
				.getForEntity(flowUrl, String.class);

		assertThat(responseEntityBlock.getBody()).isEqualTo("Oops");
		assertThat(responseEntityBlock.getStatusCode()).isEqualTo(HttpStatus.OK);

		ResponseEntity responseEntityRaw = restTemplate.getForEntity(flowUrl,
				String.class);

		assertThat(responseEntityRaw.getBody())
				.isEqualTo("RestTemplate request block by sentinel");
		assertThat(responseEntityRaw.getStatusCode()).isEqualTo(HttpStatus.OK);
	}

	@Test
	public void testNormalRestTemplate() {
		assertThat(restTemplateWithoutBlockClass.getInterceptors().size()).isEqualTo(0);

		assertThatThrownBy(() -> {
			restTemplateWithoutBlockClass.getForEntity(flowUrl, String.class);
		}).isInstanceOf(RestClientException.class);
	}

	@Test
	public void testFallbackRestTemplate() {
		ResponseEntity responseEntity = restTemplateWithFallbackClass
				.getForEntity(degradeUrl, String.class);

		assertThat(responseEntity.getBody()).isEqualTo("Oops fallback");
		assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
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
		@SentinelRestTemplate(blockHandlerClass = ExceptionUtil.class,
				blockHandler = "handleException")
		RestTemplate restTemplateWithBlockClass() {
			return new RestTemplate();
		}

		@Bean
		@SentinelRestTemplate(fallbackClass = ExceptionUtil.class,
				fallback = "fallbackException")
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
