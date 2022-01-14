package com.alibaba.cloud.nacos;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NacosConfigAutoConfiguration Tester.
 * 
 * @author freeman
 */
public class NacosConfigAutoConfigurationTest {

	@Test
	public void noImports_thenCreateProperties() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				NacosConfigAutoConfiguration.class);

		assertThat(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context,
				NacosConfigProperties.class).length).isEqualTo(1);
		assertThat(context.getBean(NacosConfigProperties.class).getServerAddr())
				.isEqualTo("localhost:8848");
		context.close();
	}

	@Test
	public void imports_thenNoCreateProperties() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
				NacosConfigAutoConfiguration.class);
		// mock import
		context.registerBean(NacosConfigProperties.class, () -> {
			NacosConfigProperties properties = new NacosConfigProperties();
			properties.setServerAddr("localhost");
			return properties;
		});

		assertThat(BeanFactoryUtils.beanNamesForTypeIncludingAncestors(context,
				NacosConfigProperties.class).length).isEqualTo(1);
		assertThat(context.getBean(NacosConfigProperties.class).getServerAddr())
				.isEqualTo("localhost");
		context.close();
	}

}
