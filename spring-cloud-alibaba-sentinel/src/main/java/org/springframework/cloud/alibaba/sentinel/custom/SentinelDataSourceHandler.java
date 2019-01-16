package org.springframework.cloud.alibaba.sentinel.custom;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.alibaba.sentinel.SentinelConstants;
import org.springframework.cloud.alibaba.sentinel.SentinelProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.SentinelDataSourceConstants;
import org.springframework.cloud.alibaba.sentinel.datasource.config.AbstractDataSourceProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.config.DataSourcePropertiesConfiguration;
import org.springframework.cloud.alibaba.sentinel.datasource.config.NacosDataSourceProperties;
import org.springframework.cloud.alibaba.sentinel.datasource.converter.JsonConverter;
import org.springframework.cloud.alibaba.sentinel.datasource.converter.XmlConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.AbstractRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

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

	private static final Logger logger = LoggerFactory
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
		// commercialization
		if (!StringUtils.isEmpty(System.getProperties()
				.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_ENDPOINT))) {
			Map<String, DataSourcePropertiesConfiguration> newDataSourceMap = new TreeMap<>(
					String.CASE_INSENSITIVE_ORDER);
			for (Map.Entry<String, DataSourcePropertiesConfiguration> entry : sentinelProperties
					.getDatasource().entrySet()) {
				if (entry.getValue().getValidDataSourceProperties()
						.getClass() != NacosDataSourceProperties.class) {
					newDataSourceMap.put(entry.getKey(), entry.getValue());
				}
			}
			newDataSourceMap.put(SentinelConstants.FLOW_DATASOURCE_NAME,
					new DataSourcePropertiesConfiguration(
							NacosDataSourceProperties.buildFlowByEDAS()));
			newDataSourceMap.put(SentinelConstants.DEGRADE_DATASOURCE_NAME,
					new DataSourcePropertiesConfiguration(
							NacosDataSourceProperties.buildDegradeByEDAS()));
			sentinelProperties.setDatasource(newDataSourceMap);
		}

		sentinelProperties.getDatasource()
				.forEach((dataSourceName, dataSourceProperties) -> {
					try {
						List<String> validFields = dataSourceProperties.getValidField();
						if (validFields.size() != 1) {
							logger.error("[Sentinel Starter] DataSource " + dataSourceName
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
						logger.error("[Sentinel Starter] DataSource " + dataSourceName
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
						logger.error("[Sentinel Starter] DataSource " + dataSourceName
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
						logger.error("[Sentinel Starter] DataSource " + dataSourceName
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

		// commercialization
		if (!StringUtils.isEmpty(System.getProperties()
				.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_ENDPOINT))) {
			if (dataSourceName.contains(SentinelConstants.FLOW_DATASOURCE_NAME)) {
				FlowRuleManager.register2Property(newDataSource.getProperty());
			}
			else if (dataSourceName.contains(SentinelConstants.DEGRADE_DATASOURCE_NAME)) {
				DegradeRuleManager.register2Property(newDataSource.getProperty());
			}
		}
	}

	private void logAndCheckRuleType(AbstractDataSource dataSource, String dataSourceName,
			Class<? extends AbstractRule> ruleClass) {
		Object ruleConfig;
		try {
			ruleConfig = dataSource.loadConfig();
		}
		catch (Exception e) {
			logger.error("[Sentinel Starter] DataSource " + dataSourceName
					+ " loadConfig error: " + e.getMessage(), e);
			return;
		}
		if (ruleConfig instanceof List) {
			List convertedRuleList = (List) ruleConfig;
			if (CollectionUtils.isEmpty(convertedRuleList)) {
				logger.warn("[Sentinel Starter] DataSource {} rule list is empty.",
						dataSourceName);
				return;
			}
			if (convertedRuleList.stream()
					.noneMatch(rule -> rule.getClass() == ruleClass)) {
				logger.error("[Sentinel Starter] DataSource {} none rules are {} type.",
						dataSourceName, ruleClass.getSimpleName());
				throw new IllegalArgumentException("[Sentinel Starter] DataSource "
						+ dataSourceName + " none rules are " + ruleClass.getSimpleName()
						+ " type.");
			}
			else if (!convertedRuleList.stream()
					.allMatch(rule -> rule.getClass() == ruleClass)) {
				logger.warn("[Sentinel Starter] DataSource {} all rules are not {} type.",
						dataSourceName, ruleClass.getSimpleName());
			}
			else {
				logger.info("[Sentinel Starter] DataSource {} load {} {}", dataSourceName,
						convertedRuleList.size(), ruleClass.getSimpleName());
			}
		}
		else {
			logger.error("[Sentinel Starter] DataSource " + dataSourceName
					+ " rule class is not List<" + ruleClass.getSimpleName()
					+ ">. Class: " + ruleConfig.getClass());
			throw new IllegalArgumentException("[Sentinel Starter] DataSource "
					+ dataSourceName + " rule class is not List<"
					+ ruleClass.getSimpleName() + ">. Class: " + ruleConfig.getClass());
		}
	}
}
