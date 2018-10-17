package org.springframework.cloud.alibaba.nacos.ribbon;

import com.netflix.client.config.DefaultClientConfigImpl;
import com.netflix.client.config.IClientConfig;
import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.cloud.alibaba.nacos.NacosDiscoveryClientAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaojing
 */
public class NacosRibbonClientConfigurationTests {

	private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(NacosRibbonTestConfiguration.class,
					NacosRibbonClientConfiguration.class,
					NacosDiscoveryClientAutoConfiguration.class,
					RibbonNacosAutoConfiguration.class))
			.withPropertyValues("spring.cloud.nacos.discovery.server-addr=127.0.0.1:8080")
			.withPropertyValues("spring.cloud.nacos.discovery.port=18080")
			.withPropertyValues("spring.cloud.nacos.discovery.service=myapp");

	@Test
	public void testProperties() {

		this.contextRunner.run(context -> {
			NacosServerList serverList = context.getBean(NacosServerList.class);
			assertThat(serverList.getServiceId()).isEqualTo("myapp");
		});
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableDiscoveryClient
	static class NacosRibbonTestConfiguration {

		@Bean
		IClientConfig iClientConfig() {
			// return new IClientConfig.Builder().s.build();
			DefaultClientConfigImpl config = new DefaultClientConfigImpl();
			config.setClientName("myapp");
			return config;
		}

		@Bean
		@LoadBalanced
		RestTemplate restTemplate() {
			return new RestTemplate();
		}

	}

}
