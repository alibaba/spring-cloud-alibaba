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

package com.alibaba.cloud.commons.governance.routing;

import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.commons.governance.routing.rule.RoutingRule;

/**
 * @author HH
 */
public class MatchService {

	private List<RoutingRule> routingRuleList;

	private String version;

	private Integer weight;

	private String fallbackVersion;

	public String getFallback() {
		return fallbackVersion;
	}

	public void setFallback(String fallbackVersion) {
		this.fallbackVersion = fallbackVersion;
	}

	public List<RoutingRule> getRoutingRuleList() {
		return routingRuleList;
	}

	public void setRoutingRuleList(List<RoutingRule> routingRuleList) {
		this.routingRuleList = routingRuleList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MatchService that = (MatchService) o;
		return getWeight().equals(that.getWeight())
				&& Objects.equals(getRoutingRuleList(), that.getRoutingRuleList())
				&& Objects.equals(getFallback(), that.getFallback())
				&& Objects.equals(getVersion(), that.getVersion());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRoutingRuleList(), getVersion(), getWeight(),
				getFallback());
	}

	@Override
	public String toString() {
		return "MatchService{" + "ruleList=" + routingRuleList + ", version='" + version
				+ '\'' + ", weight=" + weight + ", getFallback=" + fallbackVersion + '}';
	}

}
