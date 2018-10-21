package org.springframework.cloud.alibaba.sentinel.datasource.factorybean;

import org.springframework.beans.factory.FactoryBean;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;

/**
 * @author fangjian
 * @see NacosDataSource
 */
public class NacosDataSourceFactoryBean implements FactoryBean<NacosDataSource> {

	private String serverAddr;
	private String groupId;
	private String dataId;
	private Converter converter;

	@Override
	public NacosDataSource getObject() throws Exception {
		return new NacosDataSource(serverAddr, groupId, dataId, converter);
	}

	@Override
	public Class<?> getObjectType() {
		return NacosDataSource.class;
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

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter Converter) {
		this.converter = Converter;
	}
}
