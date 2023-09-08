/*
 * Copyright 2022-2023 the original author or authors.
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

package com.alibaba.cloud.routing.properties;

import java.io.Serializable;

import javax.annotation.PostConstruct;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.cloud.routing.constant.RoutingConstants;
import com.alibaba.cloud.routing.util.LoadBalanceUtil;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@ConfigurationProperties(prefix = RoutingConstants.PROPERTY_PREFIX)
public class RoutingProperties implements Serializable {

	private static final long serialVersionUID = 7157091468155324288L;

	/**
	 * Load Balance Rule.
	 */
	private String rule;

	@PostConstruct
	public void init() {
		if (StringUtils.isEmpty(rule)) {
			rule = LoadBalanceUtil.ZONE_AVOIDANCE_RULE;
		}
	}

	public String getRule() {
		return rule;
	}

	public void setRule(String rule) {
		this.rule = rule;
	}

	/**
	 * Region staining.
	 */
	private String region;

	/**
	 * Zone staining.
	 */
	private String zone;

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

}
