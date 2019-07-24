/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sentinel.datasource.config;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import javax.validation.constraints.NotEmpty;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import com.alibaba.cloud.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean;

/**
 * File Properties class Using by {@link DataSourcePropertiesConfiguration} and
 * {@link FileRefreshableDataSourceFactoryBean}
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class FileDataSourceProperties extends AbstractDataSourceProperties {

	@NotEmpty
	private String file;
	private String charset = "utf-8";
	private long recommendRefreshMs = 3000L;
	private int bufSize = 1024 * 1024;

	private boolean inJar;
	private String jarName;
	private String fileInJarName;

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

	public boolean isInJar() {
		return inJar;
	}

	public void setInJar(boolean inJar) {
		this.inJar = inJar;
	}

	public String getJarName() {
		return jarName;
	}

	public void setJarName(String jarName) {
		this.jarName = jarName;
	}

	public String getFileInJarName() {
		return fileInJarName;
	}

	public void setFileInJarName(String fileInJarName) {
		this.fileInJarName = fileInJarName;
	}

	@Override
	public void preCheck(String dataSourceName) {
		super.preCheck(dataSourceName);
		try {
			this.inJar = true;
			URL jarFileURL = ResourceUtils
					.getURL(StringUtils.trimAllWhitespace(this.getFile()));
			if (jarFileURL.getProtocol().equals("jar")) {
				String URLString = jarFileURL.toString();
				if (!URLString.startsWith("jar:file:")) {
					throw new IOException("No such file" + this.getFile());
				}
				String filePath = URLString.replace("jar:file:", "");
				String[] separated = filePath.split("!/");
				this.jarName = separated[0];
				this.fileInJarName = String.join("/",
						Arrays.copyOfRange(separated, 1, separated.length));
			}
			else {
				this.setFile(ResourceUtils
						.getFile(StringUtils.trimAllWhitespace(this.getFile()))
						.getAbsolutePath());
			}
		}
		catch (IOException e) {
			throw new RuntimeException("[Sentinel Starter] DataSource " + dataSourceName
					+ " handle file [" + this.getFile() + "] error: " + e.getMessage(),
					e);
		}

	}
}
