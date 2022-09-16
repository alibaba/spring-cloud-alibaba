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

package com.alibaba.cloud.appactive;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

/**
 * @author ChengPu raozihao
 */
@ConfigurationProperties("spring.cloud.appactive.filter")
public class AppactiveProperties {

	private String[] corePath;

	private String[] globalPath;

	private String[] generalPath;

	@Autowired
	private Environment environment;

	public String[] getCorePath() {
		return corePath;
	}

	void setCorePath(String[] corePath) {
		this.corePath = corePath;
	}

	public String[] getGlobalPath() {
		return globalPath;
	}

	void setGlobalPath(String[] globalPath) {
		this.globalPath = globalPath;
	}

	public String[] getGeneralPath() {
		return generalPath;
	}

	void setGeneralPath(String[] generalPath) {
		this.generalPath = generalPath;
	}

	@PostConstruct
	public void init() {
		this.overrideFromEnv(environment);
	}

	public void overrideFromEnv(Environment env) {

		if (StringUtils.isEmpty(this.getCorePath())) {
			String coreValue = env
					.resolvePlaceholders("${spring.cloud.appactive.filter.core-path:}");
			String[] cores = coreValue.split(",");
			this.setCorePath(cores);
		}
		if (StringUtils.isEmpty(this.getGlobalPath())) {
			String globalValue = env
					.resolvePlaceholders("${spring.cloud.appactive.filter.global-path:}");
			String[] globals = globalValue.split(",");
			this.setGlobalPath(globals);
		}
		if (StringUtils.isEmpty(this.getGeneralPath())) {
			String generalValue = env.resolvePlaceholders(
					"${spring.cloud.appactive.filter.general-path:}");
			String[] generals = generalValue.split(",");
			this.setGeneralPath(generals);
		}
	}

}
