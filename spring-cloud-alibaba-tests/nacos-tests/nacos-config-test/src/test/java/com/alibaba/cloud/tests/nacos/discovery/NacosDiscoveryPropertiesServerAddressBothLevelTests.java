package com.alibaba.cloud.tests.nacos.discovery;

import com.alibaba.cloud.nacos.NacosConfigAutoConfiguration;
import com.alibaba.cloud.nacos.NacosConfigBootstrapConfiguration;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClient;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;
import com.alibaba.cloud.testsupport.SpringCloudAlibaba;
import com.alibaba.cloud.testsupport.TestExtend;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static com.alibaba.cloud.testsupport.Constant.TIME_OUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@SpringCloudAlibaba(composeFiles = "docker/nacos-compose-test.yml", serviceName = "nacos-standalone")
@TestExtend(time = 4 * TIME_OUT)
@SpringBootTest(classes = NacosDiscoveryPropertiesServerAddressBothLevelTests.TestConfig.class, webEnvironment = NONE, properties = {
		"spring.application.name=app",
		"spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848",
		"spring.cloud.nacos.server-addr=127.0.0.1:8848" })
public class NacosDiscoveryPropertiesServerAddressBothLevelTests {

	@Autowired
	private NacosDiscoveryProperties properties;

	@Mock
	private NacosServiceDiscovery serviceDiscovery;

	@Mock
	private NacosServiceInstance serviceInstance;

	@InjectMocks
	private NacosDiscoveryClient client;

	@Test
	public void testGetServerAddr() {

		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {

	}
}
