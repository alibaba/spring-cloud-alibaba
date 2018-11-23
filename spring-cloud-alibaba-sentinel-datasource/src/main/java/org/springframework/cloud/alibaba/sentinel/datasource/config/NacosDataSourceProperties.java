package org.springframework.cloud.alibaba.sentinel.datasource.config;

import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;

/**
 * Nacos Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link NacosDataSourceFactoryBean}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourceProperties extends AbstractDataSourceProperties {

	private String serverAddr;
	private String groupId;
	private String dataId;

	public NacosDataSourceProperties() {
		super(NacosDataSourceFactoryBean.class.getName());
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}
}
