package org.springframework.cloud.alibaba.sentinel.datasource.config;

import javax.validation.constraints.NotEmpty;

import org.springframework.cloud.alibaba.sentinel.datasource.RuleType;
import org.springframework.cloud.alibaba.sentinel.datasource.SentinelDataSourceConstants;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import org.springframework.util.StringUtils;

/**
 * Nacos Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link NacosDataSourceFactoryBean}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class NacosDataSourceProperties extends AbstractDataSourceProperties {

	private String serverAddr;

	@NotEmpty
	private String groupId = "DEFAULT_GROUP";

	@NotEmpty
	private String dataId;

	private String endpoint;
	private String namespace;
	private String accessKey;
	private String secretKey;

	public NacosDataSourceProperties() {
		super(NacosDataSourceFactoryBean.class.getName());
	}

	@Override
	public void preCheck(String dataSourceName) {
		if (StringUtils.isEmpty(serverAddr) && acmPropertiesInvalid()) {
			throw new IllegalArgumentException(
					"NacosDataSource properties value not correct. serverAddr is empty but there is empty value in accessKey, secretKey, endpoint, namespace property");
		}
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

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public boolean acmPropertiesInvalid() {
		return StringUtils.isEmpty(endpoint) || StringUtils.isEmpty(accessKey)
				|| StringUtils.isEmpty(secretKey) || StringUtils.isEmpty(namespace);
	}

}
