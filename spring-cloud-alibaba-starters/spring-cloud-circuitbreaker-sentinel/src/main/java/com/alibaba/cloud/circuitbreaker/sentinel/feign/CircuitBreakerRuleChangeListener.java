package com.alibaba.cloud.circuitbreaker.sentinel.feign;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.cloud.circuitbreaker.sentinel.SentinelConfigBuilder;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cloud.client.circuitbreaker.AbstractCircuitBreakerFactory;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.util.CollectionUtils;

/**
 * Sentinel circuit breaker config change listener.
 *
 * @author freeman
 * @date 2022/1/10 12:23
 */
public class CircuitBreakerRuleChangeListener implements ApplicationContextAware,
		ApplicationListener<RefreshScopeRefreshedEvent>, SmartInitializingSingleton {
	private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerRuleChangeListener.class);

	private SentinelFeignClientProperties properties;
	/**
	 * properties backup, prevent rules from being updated every time the container is refreshed
	 */
	private SentinelFeignClientProperties propertiesBackup;
	private AbstractCircuitBreakerFactory circuitBreakerFactory;
	private ApplicationContext applicationContext;

	@Override
	public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
		ensureReady();

		// No need to update the rules
		if (Objects.equals(properties, propertiesBackup)) {
			return;
		}

		clearRules();

		// rebind
		configureRulesInDataSource();
		configureDefault();
		configureCustom();

		updateBackup();

		LOGGER.info("sentinel circuit beaker rules refreshed.");
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		this.propertiesBackup = applicationContext
				.getBean(SentinelFeignClientProperties.class).copy();
	}

	private void ensureReady() {
		// Do not inject these beans directly,
		// as it will cause the bean to be initialized prematurely,
		// and we don't want to change the initialization order of the beans
		if (circuitBreakerFactory == null) {
			String[] names = applicationContext.getBeanNamesForType(AbstractCircuitBreakerFactory.class);
			if (names.length >= 1) {
				this.circuitBreakerFactory = applicationContext.getBean(names[0], AbstractCircuitBreakerFactory.class);
			}
		}
		if (properties == null) {
			this.properties = applicationContext.getBean(SentinelFeignClientProperties.class);
		}
	}

	private void clearRules() {
		clearCircuitBreakerFactory();
		clearSentinelDegradeManager();
	}

	private void configureDefault() {
		configureDefault(properties, circuitBreakerFactory);
	}

	private void configureCustom() {
		configureCustom(properties, circuitBreakerFactory);
	}

	private void clearCircuitBreakerFactory() {
		Map configurations = getConfigurations(circuitBreakerFactory);
		if (configurations != null) {
			configurations.clear();
		}
	}

	private void clearSentinelDegradeManager() {
		DegradeRuleManager.loadRules(new ArrayList<>());
	}

	private void updateBackup() {
		this.propertiesBackup = this.properties.copy();
	}

	private void configureRulesInDataSource() {
		// TODO allow feign client rules to be configured in the data source?
		//  How to distinguish feign client rules from ordinary rules ?

		// Temporarily does not support configuring feign client rules in the data source.
		// But need to keep the normal degrade rules.
		String[] dataSourceNames = applicationContext.getBeanNamesForType(AbstractDataSource.class);

		List<DegradeRule> rules = Arrays.stream(dataSourceNames)
				.map(this::getDegradeRules)
				.flatMap(Collection::stream)
				.distinct()
				.collect(Collectors.toList());

		DegradeRuleManager.loadRules(rules);
	}

	private List<DegradeRule> getDegradeRules(String dataSourceName) {
		AbstractDataSource ds = applicationContext.getBean(dataSourceName, AbstractDataSource.class);
		try {
			@SuppressWarnings("unchecked")
			List<DegradeRule> result = (List<DegradeRule>) ds.loadConfig();
			// be careful with generic wipes
			if (!CollectionUtils.isEmpty(result)
					&& DegradeRule.class.isAssignableFrom(result.get(0).getClass())) {
				return result;
			}
		}
		catch (Exception ignored) {
			// illegal config, ignore
		}
		return new ArrayList<>();
	}

	// static method

	public static void configureCustom(SentinelFeignClientProperties properties,
			AbstractCircuitBreakerFactory factory) {
		properties.getRules().forEach((resourceName, degradeRules) -> {
			if (!Objects.equals(properties.getDefaultRule(), resourceName)) {
				factory.configure(builder -> ((SentinelConfigBuilder) builder)
						.rules(properties.getRules().getOrDefault(resourceName,
								new ArrayList<>())),
						resourceName);
			}
		});
	}

	public static void configureDefault(SentinelFeignClientProperties properties,
			AbstractCircuitBreakerFactory factory) {
		List<DegradeRule> defaultConfigurations = properties.getRules()
				.getOrDefault(properties.getDefaultRule(), new ArrayList<>());
		factory.configureDefault(
				resourceName -> new SentinelConfigBuilder(resourceName.toString())
						.entryType(EntryType.OUT).rules(defaultConfigurations).build());
	}

	public static Map getConfigurations(
			AbstractCircuitBreakerFactory circuitBreakerFactory) {
		try {
			Method method = AbstractCircuitBreakerFactory.class
					.getDeclaredMethod("getConfigurations");
			method.setAccessible(true);
			return (Map) method.invoke(circuitBreakerFactory);
		}
		catch (Exception ignored) {
		}
		return Collections.emptyMap();
	}

}
