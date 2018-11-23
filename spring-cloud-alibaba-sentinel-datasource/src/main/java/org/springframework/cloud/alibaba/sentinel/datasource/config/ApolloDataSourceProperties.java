package org.springframework.cloud.alibaba.sentinel.datasource.config;

import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.ApolloDataSourceFactoryBean;

/**
 * Apollo Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link ApolloDataSourceFactoryBean}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ApolloDataSourceProperties extends AbstractDataSourceProperties {

	private String namespaceName;
	private String flowRulesKey;
	private String defaultFlowRuleValue;

	public ApolloDataSourceProperties() {
		super(ApolloDataSourceFactoryBean.class.getName());
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
}
