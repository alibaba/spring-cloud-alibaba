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
 * @description
 * @date 2022/8/15
 */
@ConfigurationProperties("spring.cloud.appactive.filter")
public class AppactiveProperties {

	private String[] unitPath;

	private String[] centerPath;

	private String[] normalPath;

	@Autowired
	private Environment environment;

	public String[] getUnitPath() {
		return unitPath;
	}

	public void setUnitPath(String[] unitPath) {
		this.unitPath = unitPath;
	}

	public String[] getCenterPath() {
		return centerPath;
	}

	public void setCenterPath(String[] centerPath) {
		this.centerPath = centerPath;
	}

	public String[] getNormalPath() {
		return normalPath;
	}

	public void setNormalPath(String[] normalPath) {
		this.normalPath = normalPath;
	}

	@PostConstruct
	public void init() {
		this.overrideFromEnv(environment);
	}

	public void overrideFromEnv(Environment env) {

		if (StringUtils.isEmpty(this.getUnitPath())) {
			String unitValue = env
					.resolvePlaceholders("${spring.cloud.appactive.filter.unit-path:}");
			String[] units = unitValue.split(",");
			this.setUnitPath(units);
		}
		if (StringUtils.isEmpty(this.getCenterPath())) {
			String centerValue = env
					.resolvePlaceholders("${spring.cloud.appactive.filter.center-path:}");
			String[] centers = centerValue.split(",");
			this.setCenterPath(centers);
		}
		if (StringUtils.isEmpty(this.getNormalPath())) {
			String centerValue = env
					.resolvePlaceholders("${spring.cloud.appactive.filter.normal-path:}");
			String[] centers = centerValue.split(",");
			this.setCenterPath(centers);
		}
	}

}
