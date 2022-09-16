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
import java.util.Map;
import java.util.Objects;

import com.alibaba.cloud.data.crd.rule.RouteRule;

/**
 * @author HH
 */
public class LabelRouteData {

	/**
	 * if all rule don't match,request route in default.
	 */
	private String defaultRoute;

	/**
	 * route rule list.
	 */
	private List<RouteRule> ruleList;

	/**
	 *list mapping of rule and path.
	 */
	private List<MatchService> matchRouteList;

	public String getDefaultRoute() {
		return defaultRoute;
	}

	public void setDefaultRoute(String defaultRoute) {
		this.defaultRoute = defaultRoute;
	}

	public List<RouteRule> getRuleList() {
		return ruleList;
	}

	public void setRuleList(List<RouteRule> ruleList) {
		this.ruleList = ruleList;
	}

	public List<MatchService> getMatchRouteList() {
		return matchRouteList;
	}

	public void setMatchRouteList(List<MatchService> matchRouteList) {
		this.matchRouteList = matchRouteList;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		LabelRouteData that = (LabelRouteData) o;
		return Objects.equals(getDefaultRoute(), that.getDefaultRoute()) && Objects
				.equals(getRuleList(), that.getRuleList()) && Objects
				.equals(getMatchRouteList(), that.getMatchRouteList());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getDefaultRoute(), getRuleList(), getMatchRouteList());
	}

	@Override
	public String toString() {
		return "LabelRouteData{" +
				"defaultRoute='" + defaultRoute + '\'' +
				", ruleList=" + ruleList +
				", matchRouteList=" + matchRouteList +
				'}';
	}

	class MatchService {
		private List<RouteRule> ruleList;

		private ServiceMetadata serviceMetadata;

		public List<RouteRule> getRuleList() {
			return ruleList;
		}

		public void setRuleList(List<RouteRule> ruleList) {
			this.ruleList = ruleList;
		}

		public ServiceMetadata getServiceMetadata() {
			return serviceMetadata;
		}

		public void setServiceMetadata(ServiceMetadata serviceMetadata) {
			this.serviceMetadata = serviceMetadata;
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
			return Objects.equals(getRuleList(), that.getRuleList()) && Objects
					.equals(getServiceMetadata(), that.getServiceMetadata());
		}

		@Override
		public int hashCode() {
			return Objects.hash(getRuleList(), getServiceMetadata());
		}

		@Override
		public String toString() {
			return "MatchService{" +
					"ruleList=" + ruleList +
					", serviceMetadata=" + serviceMetadata +
					'}';
		}
	}

}
