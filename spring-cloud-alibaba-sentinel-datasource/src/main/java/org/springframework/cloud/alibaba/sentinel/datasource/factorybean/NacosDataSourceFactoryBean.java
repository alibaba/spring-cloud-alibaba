package org.springframework.cloud.alibaba.sentinel.datasource.factorybean;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.nacos.api.PropertyKeyConst;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.util.StringUtils;

import java.util.Properties;

/**
 * A {@link FactoryBean} for creating {@link NacosDataSource} instance.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see NacosDataSource
 */
public class NacosDataSourceFactoryBean implements FactoryBean<NacosDataSource> {

	private String serverAddr;
	private String groupId;
	private String dataId;
	private Converter converter;

	private String endpoint;
	private String namespace;
	private String accessKey;
	private String secretKey;

	@Override
	public NacosDataSource getObject() throws Exception {
		Properties properties = new Properties();
		if (!StringUtils.isEmpty(this.serverAddr)) {
			properties.setProperty(PropertyKeyConst.SERVER_ADDR, this.serverAddr);
		}
		else {
			properties.setProperty(PropertyKeyConst.ACCESS_KEY, this.accessKey);
			properties.setProperty(PropertyKeyConst.SECRET_KEY, this.secretKey);
			properties.setProperty(PropertyKeyConst.ENDPOINT, this.endpoint);
		}
		if (!StringUtils.isEmpty(this.namespace)) {
			properties.setProperty(PropertyKeyConst.NAMESPACE, this.namespace);
		}
		return new NacosDataSource(properties, groupId, dataId, converter);
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

	public void setConverter(Converter converter) {
		this.converter = converter;
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
}
