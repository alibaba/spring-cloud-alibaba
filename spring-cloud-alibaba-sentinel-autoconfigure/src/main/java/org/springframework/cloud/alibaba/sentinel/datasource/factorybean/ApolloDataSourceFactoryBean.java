package org.springframework.cloud.alibaba.sentinel.datasource.factorybean;

import org.springframework.beans.factory.FactoryBean;

import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;

/**
 * @author fangjian
 * @see ApolloDataSource
 */
public class ApolloDataSourceFactoryBean implements FactoryBean<ApolloDataSource> {

	private String namespaceName;
	private String flowRulesKey;
	private String defaultFlowRuleValue;
	private ConfigParser configParser;

	@Override
	public ApolloDataSource getObject() throws Exception {
		return new ApolloDataSource(namespaceName, flowRulesKey, defaultFlowRuleValue,
				configParser);
	}

	@Override
	public Class<?> getObjectType() {
		return ApolloDataSource.class;
	}

	public String getNamespaceName() {
		return namespaceName;
	}

	public void setNamespaceName(String namespaceName) {
		this.namespaceName = namespaceName;
	}

	public String getFlowRulesKey() {
		return flowRulesKey;
	}

	public void setFlowRulesKey(String flowRulesKey) {
		this.flowRulesKey = flowRulesKey;
	}

	public String getDefaultFlowRuleValue() {
		return defaultFlowRuleValue;
	}

	public void setDefaultFlowRuleValue(String defaultFlowRuleValue) {
		this.defaultFlowRuleValue = defaultFlowRuleValue;
	}

	public ConfigParser getConfigParser() {
		return configParser;
	}

	public void setConfigParser(ConfigParser configParser) {
		this.configParser = configParser;
	}
}
