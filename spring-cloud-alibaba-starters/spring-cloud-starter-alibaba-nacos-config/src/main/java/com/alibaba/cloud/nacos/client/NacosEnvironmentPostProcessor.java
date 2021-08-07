package com.alibaba.cloud.nacos.client;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosConfigProperties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import static com.alibaba.cloud.nacos.client.NacosPropertySourceLocator.NACOS_PROPERTY_SOURCE_NAME;

/**
 * @author ooooo
 */
public class NacosEnvironmentPostProcessor implements EnvironmentPostProcessor {

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment,
			SpringApplication application) {
		Boolean eagerLoad = environment.getProperty(
				NacosConfigProperties.PREFIX + ".eagerLoad.enabled", Boolean.class,
				false);
		if (!eagerLoad || environment.getPropertySources()
				.contains(NACOS_PROPERTY_SOURCE_NAME)) {
			return;
		}

		Binder binder = Binder.get(environment);
		NacosConfigProperties nacosConfigProperties = binder
				.bind(NacosConfigProperties.PREFIX, NacosConfigProperties.class).get();
		nacosConfigProperties.setEnvironment(environment);
		nacosConfigProperties.init();

		NacosConfigManager nacosConfigManager = new NacosConfigManager(
				nacosConfigProperties);
		NacosPropertySourceLocator nacosPropertySourceLocator = new NacosPropertySourceLocator(
				nacosConfigManager);
		PropertySource<?> propertySource = nacosPropertySourceLocator.locate(environment);
		environment.getPropertySources().addFirst(propertySource);
	}

}
