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
import com.alibaba.cloud.routing.constant.LabelRoutingConstants;
import com.alibaba.cloud.routing.util.LoadBalanceUtil;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yuluo
 * @author <a href="1481556636@qq.com"></a>
 */

@ConfigurationProperties(prefix = LabelRoutingConstants.PROPERTY_PREFIX)
public class LabelRoutingProperties implements Serializable {

	private static final long serialVersionUID = 7157091468155324299L;

	/**
	 * Load Balance Rule.
	 */
	private String rule;

	private boolean enableOutlierDetected;

	private long baseEjectionTime;

	private double minHealthPercent;

	private int recoverInterval;

	@PostConstruct
	public void init() {
		if (StringUtils.isEmpty(rule)) {
			rule = LoadBalanceUtil.ZONE_AVOIDANCE_RULE;
		}
	}

	public boolean isEnableOutlierDetected() {
		return enableOutlierDetected;
	}

	public void setEnableOutlierDetected(boolean enableOutlierDetected) {
		this.enableOutlierDetected = enableOutlierDetected;
	}

	public long getBaseEjectionTime() {
		return baseEjectionTime;
	}

	public void setBaseEjectionTime(long baseEjectionTime) {
		this.baseEjectionTime = baseEjectionTime;
	}

	public double getMinHealthPercent() {
		return minHealthPercent;
	}

	public void setMinHealthPercent(double minHealthPercent) {
		this.minHealthPercent = minHealthPercent;
	}

	public int getRecoverInterval() {
		return recoverInterval;
	}

	public void setRecoverInterval(int recoverInterval) {
		this.recoverInterval = recoverInterval;
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
