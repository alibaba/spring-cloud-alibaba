package org.springframework.cloud.alibaba.sentinel.datasource.config;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.util.ObjectUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Using By ConfigurationProperties.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 * @see NacosDataSourceProperties
 * @see ApolloDataSourceProperties
 * @see ZookeeperDataSourceProperties
 * @see FileDataSourceProperties
 */
public class DataSourcePropertiesConfiguration {

	private FileDataSourceProperties file;

	private NacosDataSourceProperties nacos;

	private ZookeeperDataSourceProperties zk;

	private ApolloDataSourceProperties apollo;

	public FileDataSourceProperties getFile() {
		return file;
	}

	public void setFile(FileDataSourceProperties file) {
		this.file = file;
	}

	public NacosDataSourceProperties getNacos() {
		return nacos;
	}

	public void setNacos(NacosDataSourceProperties nacos) {
		this.nacos = nacos;
	}

	public ZookeeperDataSourceProperties getZk() {
		return zk;
	}

	public void setZk(ZookeeperDataSourceProperties zk) {
		this.zk = zk;
	}

	public ApolloDataSourceProperties getApollo() {
		return apollo;
	}

	public void setApollo(ApolloDataSourceProperties apollo) {
		this.apollo = apollo;
	}

	@JsonIgnore
	public List<String> getInvalidField() {
		return Arrays.stream(this.getClass().getDeclaredFields()).map(field -> {
			try {
				if (!ObjectUtils.isEmpty(field.get(this))) {
					return field.getName();
				}
				return null;
			}
			catch (IllegalAccessException e) {
				// won't happen
			}
			return null;
		}).filter(Objects::nonNull).collect(Collectors.toList());
	}

}
