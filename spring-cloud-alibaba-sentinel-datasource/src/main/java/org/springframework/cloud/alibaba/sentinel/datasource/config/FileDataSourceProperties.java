package org.springframework.cloud.alibaba.sentinel.datasource.config;

import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean;

/**
 * File Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link FileRefreshableDataSourceFactoryBean}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class FileDataSourceProperties extends AbstractDataSourceProperties {

	private String file;
	private String charset = "utf-8";
	private long recommendRefreshMs = 3000L;
	private int bufSize = 1024 * 1024;

	public FileDataSourceProperties() {
		super(FileRefreshableDataSourceFactoryBean.class.getName());
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public long getRecommendRefreshMs() {
		return recommendRefreshMs;
	}

	public void setRecommendRefreshMs(long recommendRefreshMs) {
		this.recommendRefreshMs = recommendRefreshMs;
	}

	public int getBufSize() {
		return bufSize;
	}

	public void setBufSize(int bufSize) {
		this.bufSize = bufSize;
	}
}
