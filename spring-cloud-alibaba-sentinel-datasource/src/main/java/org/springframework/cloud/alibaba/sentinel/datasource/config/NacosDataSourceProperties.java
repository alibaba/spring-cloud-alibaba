package org.springframework.cloud.alibaba.sentinel.datasource.config;

import javax.validation.constraints.NotEmpty;

import org.springframework.cloud.alibaba.sentinel.datasource.RuleType;
import org.springframework.cloud.alibaba.sentinel.datasource.SentinelDataSourceConstants;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.NacosDataSourceFactoryBean;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.NacosDataSourceWithAuthorizationFactoryBean;
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
	private String groupId;

	@NotEmpty
	private String dataId;

	// commercialized usage

	private String endpoint;
	private String namespace;
	private String accessKey;
	private String secretKey;

	public NacosDataSourceProperties() {
		super(NacosDataSourceFactoryBean.class.getName());
	}

	@Override
	public void preCheck(String dataSourceName) {
		if (!StringUtils.isEmpty(System.getProperties()
				.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_ENDPOINT))) {
			this.setServerAddr(null);
			this.setFactoryBeanName(
					NacosDataSourceWithAuthorizationFactoryBean.class.getName());
			this.setEndpoint(System.getProperties()
					.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_ENDPOINT));
			this.setNamespace(System.getProperties()
					.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_NAMESPACE));
			this.setAccessKey(System.getProperties()
					.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_AK));
			this.setSecretKey(System.getProperties()
					.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_SK));
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

	public static NacosDataSourceProperties buildFlowByEDAS() {
		return buildByEDAS("flow");
	}

	public static NacosDataSourceProperties buildDegradeByEDAS() {
		return buildByEDAS("degrade");
	}

	public static NacosDataSourceProperties buildByEDAS(String type) {
		NacosDataSourceProperties result = new NacosDataSourceProperties();
		result.setFactoryBeanName(
				NacosDataSourceWithAuthorizationFactoryBean.class.getName());
		result.setEndpoint(System.getProperties()
				.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_ENDPOINT));
		result.setNamespace(System.getProperties()
				.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_NAMESPACE));
		result.setAccessKey(System.getProperties()
				.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_AK));
		result.setSecretKey(System.getProperties()
				.getProperty(SentinelDataSourceConstants.NACOS_DATASOURCE_SK));
		result.setDataType("json");
		result.setDataId(System.getProperties()
				.getProperty(SentinelDataSourceConstants.PROJECT_NAME) + "-" + type);
		result.setGroupId("nacos-sentinel");
		if (type.equals(RuleType.FLOW.getName())) {
			result.setRuleType(RuleType.FLOW);
		}
		else {
			result.setRuleType(RuleType.DEGRADE);
		}
		return result;
	}
}
