package org.springframework.cloud.alibaba.sentinel.datasource.factorybean;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.FactoryBean;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.zookeeper.ZookeeperDataSource;

/**
 * @author fangjian
 * @see ZookeeperDataSource
 */
public class ZookeeperDataSourceFactoryBean implements FactoryBean<ZookeeperDataSource> {

	private String serverAddr;

	private String path;

	private String groupId;
	private String dataId;

	private Converter converter;

	@Override
	public ZookeeperDataSource getObject() throws Exception {
		if (StringUtils.isNotEmpty(groupId) && StringUtils.isNotEmpty(dataId)) {
			// the path will be /{groupId}/{dataId}
			return new ZookeeperDataSource(serverAddr, groupId, dataId, converter);
		}
		else {
			// using path directly
			return new ZookeeperDataSource(serverAddr, path, converter);
		}
	}

	@Override
	public Class<?> getObjectType() {
		return ZookeeperDataSource.class;
	}

	public String getServerAddr() {
		return serverAddr;
	}

	public void setServerAddr(String serverAddr) {
		this.serverAddr = serverAddr;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
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
