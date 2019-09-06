package com.alibaba.cloud.nacos;

import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration;

/**
 * @author <a href="mailto:lyuzb@lyuzb.com">lyuzb</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = NacosConfigPropertiesServerAddressBothLevelTests.TestConfig.class, properties = {
		"spring.cloud.nacos.config.server-addr=321,321,321,321:8848",
		"spring.cloud.nacos.server-addr=123.123.123.123:8848" }, webEnvironment = RANDOM_PORT)
public class NacosConfigPropertiesServerAddressBothLevelTests {

	@Autowired
	private NacosConfigProperties properties;

	@Test
	public void testGetServerAddr() {
		assertEquals("NacosConfigProperties server address was wrong",
				"321,321,321,321:8848", properties.getServerAddr());
	}

	@Configuration
	@EnableAutoConfiguration
	@ImportAutoConfiguration({ NacosConfigEndpointAutoConfiguration.class,
			NacosConfigAutoConfiguration.class, NacosConfigBootstrapConfiguration.class })
	public static class TestConfig {
	}
}
