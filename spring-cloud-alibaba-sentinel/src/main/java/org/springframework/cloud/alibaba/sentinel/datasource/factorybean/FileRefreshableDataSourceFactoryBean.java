package org.springframework.cloud.alibaba.sentinel.datasource.factorybean;

import java.io.File;
import java.nio.charset.Charset;

import org.springframework.beans.factory.FactoryBean;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;

/**
 * @author fangjian
 * @see FileRefreshableDataSource
 */
public class FileRefreshableDataSourceFactoryBean
		implements FactoryBean<FileRefreshableDataSource> {

	private String file;
	private String charset;
	private long recommendRefreshMs;
	private int bufSize;
	private Converter converter;

	@Override
	public FileRefreshableDataSource getObject() throws Exception {
		return new FileRefreshableDataSource(new File(file), converter,
				recommendRefreshMs, bufSize, Charset.forName(charset));
	}

	@Override
	public Class<?> getObjectType() {
		return FileRefreshableDataSource.class;
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

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter Converter) {
		this.converter = Converter;
	}
}
