package org.springframework.cloud.alibaba.sentinel.custom;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.alibaba.sentinel.SentinelProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.config.AbstractDataSourceProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.converter.JsonConverter;
import org.springframework.cloud.alibaba.sentinel.datasource.converter.XmlConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sentinel {@link ReadableDataSource} Handler Handle the configurations of
 * 'spring.cloud.sentinel.datasource'
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see SentinelProperties#datasource
 * @see JsonConverter
 * @see XmlConverter
 */
public class SentinelDataSourceHandler implements SmartInitializingSingleton {

	private static final Logger log = LoggerFactory
			.getLogger(SentinelDataSourceHandler.class);

	private List<String> dataTypeList = Arrays.asList("json", "xml");

	private final String DATA_TYPE_FIELD = "dataType";
	private final String CUSTOM_DATA_TYPE = "custom";
	private final String CONVERTER_CLASS_FIELD = "converterClass";

	private final DefaultListableBeanFactory beanFactory;

	public SentinelDataSourceHandler(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Autowired
	private SentinelProperties sentinelProperties;

	@Override
	public void afterSingletonsInstantiated() {
		sentinelProperties.getDatasource()
				.forEach((dataSourceName, dataSourceProperties) -> {
					try {
						List<String> validFields = dataSourceProperties.getValidField();
						if (validFields.size() != 1) {
							log.error("[Sentinel Starter] DataSource " + dataSourceName
									+ " multi datasource active and won't loaded: "
									+ dataSourceProperties.getValidField());
							return;
						}
						AbstractDataSourceProperties abstractDataSourceProperties = dataSourceProperties
								.getValidDataSourceProperties();
						abstractDataSourceProperties.preCheck(dataSourceName);
						registerBean(abstractDataSourceProperties, dataSourceName
								+ "-sentinel-" + validFields.get(0) + "-datasource");
					}
					catch (Exception e) {
						log.error("[Sentinel Starter] DataSource " + dataSourceName
								+ " build error: " + e.getMessage(), e);
					}
				});
	}

	private void registerBean(final AbstractDataSourceProperties dataSourceProperties,
			String dataSourceName) {

		Map<String, Object> propertyMap = Arrays
				.stream(dataSourceProperties.getClass().getDeclaredFields())
				.collect(HashMap::new, (m, v) -> {
					try {
						v.setAccessible(true);
						m.put(v.getName(), v.get(dataSourceProperties));
					}
					catch (IllegalAccessException e) {
						log.error("[Sentinel Starter] DataSource " + dataSourceName
								+ " field: " + v.getName() + " invoke error");
						throw new RuntimeException(
								"[Sentinel Starter] DataSource " + dataSourceName
										+ " field: " + v.getName() + " invoke error",
								e);
					}
				}, HashMap::putAll);
		propertyMap.put(CONVERTER_CLASS_FIELD, dataSourceProperties.getConverterClass());
		propertyMap.put(DATA_TYPE_FIELD, dataSourceProperties.getDataType());

		BeanDefinitionBuilder builder = BeanDefinitionBuilder
				.genericBeanDefinition(dataSourceProperties.getFactoryBeanName());

		propertyMap.forEach((propertyName, propertyValue) -> {
			Field field = ReflectionUtils.findField(dataSourceProperties.getClass(),
					propertyName);
			if (null == field) {
				return;
			}
			if (DATA_TYPE_FIELD.equals(propertyName)) {
				String dataType = StringUtils.trimAllWhitespace(propertyValue.toString());
				if (CUSTOM_DATA_TYPE.equals(dataType)) {
					try {
						if (StringUtils
								.isEmpty(dataSourceProperties.getConverterClass())) {
							throw new RuntimeException("[Sentinel Starter] DataSource "
									+ dataSourceName
									+ "dataType is custom, please set converter-class "
									+ "property");
						}
						// construct custom Converter with 'converterClass'
						// configuration and register
						String customConvertBeanName = "sentinel-"
								+ dataSourceProperties.getConverterClass();
						if (!this.beanFactory.containsBean(customConvertBeanName)) {
							this.beanFactory.registerBeanDefinition(customConvertBeanName,
									BeanDefinitionBuilder
											.genericBeanDefinition(
													Class.forName(dataSourceProperties
															.getConverterClass()))
											.getBeanDefinition());
						}
						builder.addPropertyReference("converter", customConvertBeanName);
					}
					catch (ClassNotFoundException e) {
						log.error("[Sentinel Starter] DataSource " + dataSourceName
								+ " handle "
								+ dataSourceProperties.getClass().getSimpleName()
								+ " error, class name: "
								+ dataSourceProperties.getConverterClass());
						throw new RuntimeException("[Sentinel Starter] DataSource "
								+ dataSourceName + " handle "
								+ dataSourceProperties.getClass().getSimpleName()
								+ " error, class name: "
								+ dataSourceProperties.getConverterClass(), e);
					}
				}
				else {
					if (!dataTypeList.contains(
							StringUtils.trimAllWhitespace(propertyValue.toString()))) {
						throw new RuntimeException("[Sentinel Starter] DataSource "
								+ dataSourceName + " dataType: " + propertyValue
								+ " is not support now. please using these types: "
								+ dataTypeList.toString());
					}
					// converter type now support xml or json.
					// The bean name of these converters wrapped by
					// 'sentinel-{converterType}-{ruleType}-converter'
					builder.addPropertyReference("converter",
							"sentinel-" + propertyValue.toString() + "-"
									+ dataSourceProperties.getRuleType().getName()
									+ "-converter");
				}
			}
			else if (CONVERTER_CLASS_FIELD.equals(propertyName)) {
				return;
			}
			else {
				// wired properties
				Optional.ofNullable(propertyValue)
						.ifPresent(v -> builder.addPropertyValue(propertyName, v));
			}
		});

		this.beanFactory.registerBeanDefinition(dataSourceName,
				builder.getBeanDefinition());
		// init in Spring
		AbstractDataSource newDataSource = (AbstractDataSource) this.beanFactory
				.getBean(dataSourceName);

		logAndCheckRuleType(newDataSource, dataSourceName,
				dataSourceProperties.getRuleType().getClazz());

		// register property in RuleManager
		dataSourceProperties.postRegister(newDataSource);
	}

	private void logAndCheckRuleType(AbstractDataSource dataSource, String dataSourceName,
			Class<? extends AbstractRule> ruleClass) {
		Object ruleConfig;
		try {
			ruleConfig = dataSource.loadConfig();
		}
		catch (Exception e) {
			log.error("[Sentinel Starter] DataSource " + dataSourceName
					+ " loadConfig error: " + e.getMessage(), e);
			return;
		}
		if (ruleConfig instanceof List) {
			List convertedRuleList = (List) ruleConfig;
			if (CollectionUtils.isEmpty(convertedRuleList)) {
				log.warn("[Sentinel Starter] DataSource {} rule list is empty.",
						dataSourceName);
				return;
			}
			if (convertedRuleList.stream()
					.noneMatch(rule -> rule.getClass() == ruleClass)) {
				log.error("[Sentinel Starter] DataSource {} none rules are {} type.",
						dataSourceName, ruleClass.getSimpleName());
				throw new IllegalArgumentException("[Sentinel Starter] DataSource "
						+ dataSourceName + " none rules are " + ruleClass.getSimpleName()
						+ " type.");
			}
			else if (!convertedRuleList.stream()
					.allMatch(rule -> rule.getClass() == ruleClass)) {
				log.warn("[Sentinel Starter] DataSource {} all rules are not {} type.",
						dataSourceName, ruleClass.getSimpleName());
			}
			else {
				log.info("[Sentinel Starter] DataSource {} load {} {}", dataSourceName,
						convertedRuleList.size(), ruleClass.getSimpleName());
			}
		}
		else {
			log.error("[Sentinel Starter] DataSource " + dataSourceName
					+ " rule class is not List<" + ruleClass.getSimpleName()
					+ ">. Class: " + ruleConfig.getClass());
			throw new IllegalArgumentException("[Sentinel Starter] DataSource "
					+ dataSourceName + " rule class is not List<"
					+ ruleClass.getSimpleName() + ">. Class: " + ruleConfig.getClass());
		}
	}
}
