package com.alibaba.cloud.tests.nacos.discovery;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientConfiguration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistryAutoConfiguration;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = NacosDiscoveryPropertiesServerAddressBothLevelTests.TestConfig.class, properties = {
		"spring.application.name=app", "spring.cloud.nacos.server-addr=127.0.0.1:8848" })
public class NacosDiscoveryPropertiesServerAddressTopLevelTests {

	@Autowired
	private NacosDiscoveryProperties properties;

	@Test
	public void testGetServerAddr() {
		assertThat(properties.getServerAddr()).isEqualTo("127.0.0.1:8848");
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientConfiguration.class,
			NacosServiceRegistryAutoConfiguration.class })
	public static class TestConfig {

	}
}
