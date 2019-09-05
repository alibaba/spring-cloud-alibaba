package com.alibaba.cloud.nacos;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.cloud.nacos.NacosDiscoveryAutoConfiguration;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosDiscoveryClientAutoConfiguration;

/**
 * 
 * @author <a href="mailto:lyuzb@lyuzb.com">lyuzb</a>
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NacosDiscoveryPropertiesServerAddressTopLevelTests.TestConfig.class, properties = {
		"spring.cloud.nacos.server-addr=123.123.123.123:8848" }, webEnvironment = RANDOM_PORT)

public class NacosDiscoveryPropertiesServerAddressTopLevelTests {

	@Autowired
	private NacosDiscoveryProperties properties;

	@Test
	public void testGetServerAddr() {
		assertEquals("NacosDiscoveryProperties server address was wrong",
				"123.123.123.123:8848", properties.getServerAddr());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ AutoServiceRegistrationConfiguration.class,
			NacosDiscoveryClientAutoConfiguration.class,
			NacosDiscoveryAutoConfiguration.class })
	public static class TestConfig {
	}
}
