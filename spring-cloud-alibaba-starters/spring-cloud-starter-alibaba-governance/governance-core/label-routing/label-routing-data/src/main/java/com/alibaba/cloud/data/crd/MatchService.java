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

package com.alibaba.cloud.data.crd;

import java.util.List;
import java.util.Objects;

import com.alibaba.cloud.data.crd.rule.RouteRule;

/**
 * @author HH
 */
public class MatchService {

	private List<RouteRule> ruleList;

	private String version;

	private int weight;

	public List<RouteRule> getRuleList() {
		return ruleList;
	}

	public void setRuleList(List<RouteRule> ruleList) {
		this.ruleList = ruleList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
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
		return getWeight() == that.getWeight() && Objects
				.equals(getRuleList(), that.getRuleList()) && Objects.equals(getVersion(), that.getVersion());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getRuleList(), getVersion(), getWeight());
	}

	@Override
	public String toString() {
		return "MatchService{" +
				"ruleList=" + ruleList +
				", version='" + version + '\'' +
				", weight=" + weight +
				'}';
	}

}
