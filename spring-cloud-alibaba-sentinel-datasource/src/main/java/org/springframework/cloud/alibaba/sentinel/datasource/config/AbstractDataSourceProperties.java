package org.springframework.cloud.alibaba.sentinel.datasource.config;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract class Using by {@link DataSourcePropertiesConfiguration}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class AbstractDataSourceProperties {

	private String dataType = "json";
	private String converterClass;
	@JsonIgnore
	private final String factoryBeanName;

	public AbstractDataSourceProperties(String factoryBeanName) {
		this.factoryBeanName = factoryBeanName;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
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

}
