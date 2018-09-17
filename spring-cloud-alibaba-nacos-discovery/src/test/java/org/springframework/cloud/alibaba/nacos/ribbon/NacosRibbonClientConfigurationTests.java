package org.springframework.cloud.alibaba.nacos.ribbon;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryAutoConfiguration;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.commons.util.InetUtilsProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 */
public class NacosRibbonClientConfigurationTests {

	private ConfigurableApplicationContext context;

	@Before
	public void setUp() throws Exception {
		this.context = new SpringApplicationBuilder(NacosRibbonTestConfiguration.class,
				NacosDiscoveryAutoConfiguration.class,
				NacosRibbonClientConfiguration.class, RibbonNacosAutoConfiguration.class)
						.web(false).run("--server.port=18080",
								"--spring.cloud.nacos.discovery.server-addr=127.0.0.1:8080",
								"--spring.cloud.nacos.discovery.port=18080",
								"--spring.cloud.nacos.discovery.service=myapp");
	}

	@Test
	public void testProperties() {

		NacosServerList serverList = context.getBean(NacosServerList.class);
		assertThat(serverList.getServiceId()).isEqualTo("myapp");
	}

	@Configuration
	@AutoConfigureBefore(value = { NacosDiscoveryAutoConfiguration.class })
	static class NacosRibbonTestConfiguration {

		@Bean
		@ConditionalOnMissingBean
		AutoServiceRegistrationProperties autoServiceRegistrationProperties() {
			return new AutoServiceRegistrationProperties();
		}

		@Bean
		IClientConfig iClientConfig() {
			DefaultClientConfigImpl config = new DefaultClientConfigImpl();
			config.setClientName("myapp");
			return config;
		}

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate();
		}

		@Bean
		InetUtils inetUtils() {
			return new InetUtils(new InetUtilsProperties());
		}

	}

}
