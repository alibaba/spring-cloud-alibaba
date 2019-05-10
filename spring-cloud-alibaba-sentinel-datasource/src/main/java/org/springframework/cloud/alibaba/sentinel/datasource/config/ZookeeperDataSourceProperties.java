package org.springframework.cloud.alibaba.sentinel.datasource.config;

import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.ZookeeperDataSourceFactoryBean;
import org.springframework.util.StringUtils;

/**
 * Zookeeper Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link ZookeeperDataSourceFactoryBean}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class ZookeeperDataSourceProperties extends AbstractDataSourceProperties {

	public ZookeeperDataSourceProperties() {
		super(ZookeeperDataSourceFactoryBean.class.getName());
	}

	private String serverAddr;

	private String path;

	private String groupId;

	private String dataId;

	@Override
	public void preCheck(String dataSourceName) {
		if (StringUtils.isEmpty(serverAddr)) {
			serverAddr = this.getEnv()
					.getProperty("spring.cloud.sentinel.datasource.zk.server-addr", "");
			if (StringUtils.isEmpty(serverAddr)) {
				throw new IllegalArgumentException(
						"ZookeeperDataSource server-addr is empty");
			}
		}
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
}
