/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sentinel.datasource.factorybean;

import java.io.File;
import java.nio.charset.Charset;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;

import org.springframework.beans.factory.FactoryBean;

/**
 * A {@link FactoryBean} for creating {@link FileRefreshableDataSource} instance.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
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

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

}
