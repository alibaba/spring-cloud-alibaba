package org.springframework.cloud.alibaba.sentinel.datasource.config;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.cloud.alibaba.sentinel.datasource.RuleType;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract class Using by {@link DataSourcePropertiesConfiguration}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class AbstractDataSourceProperties {

	@NotEmpty
	private String dataType = "json";
	@NotNull
	private RuleType ruleType;
	private String converterClass;
	@JsonIgnore
	protected String factoryBeanName;

	public AbstractDataSourceProperties(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public RuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(RuleType ruleType) {
		this.ruleType = ruleType;
	}

	public String getConverterClass() {
		return converterClass;
	}

	public void setConverterClass(String converterClass) {
		this.converterClass = converterClass;
	}

	public String getFactoryBeanName() {
		return factoryBeanName;
	}

	public void setFactoryBeanName(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public void preCheck(String dataSourceName) {

	}

	public void postRegister(AbstractDataSource dataSource) {
		switch (this.getRuleType()) {
		case FLOW:
			FlowRuleManager.register2Property(dataSource.getProperty());
			break;
		case DEGRADE:
			DegradeRuleManager.register2Property(dataSource.getProperty());
			break;
		case PARAM_FLOW:
			ParamFlowRuleManager.register2Property(dataSource.getProperty());
			break;
		case SYSTEM:
			SystemRuleManager.register2Property(dataSource.getProperty());
			break;
		case AUTHORITY:
			AuthorityRuleManager.register2Property(dataSource.getProperty());
			break;
		default:
			break;
		}
	}
}
