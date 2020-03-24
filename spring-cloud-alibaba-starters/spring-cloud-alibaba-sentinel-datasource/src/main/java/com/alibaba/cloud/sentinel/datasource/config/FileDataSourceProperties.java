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

package com.alibaba.cloud.sentinel.datasource.config;

import java.io.IOException;

import javax.validation.constraints.NotEmpty;

import com.alibaba.cloud.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

/**
 * File Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link FileRefreshableDataSourceFactoryBean}.
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class FileDataSourceProperties extends AbstractDataSourceProperties {

	@NotEmpty
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

	@Override
	public void preCheck(String dataSourceName) {
		super.preCheck(dataSourceName);
		try {
			this.setFile(
					ResourceUtils.getFile(StringUtils.trimAllWhitespace(this.getFile()))
							.getAbsolutePath());
		}
		catch (IOException e) {
			throw new RuntimeException("[Sentinel Starter] DataSource " + dataSourceName
					+ " handle file [" + this.getFile() + "] error: " + e.getMessage(),
					e);
		}

	}

}
