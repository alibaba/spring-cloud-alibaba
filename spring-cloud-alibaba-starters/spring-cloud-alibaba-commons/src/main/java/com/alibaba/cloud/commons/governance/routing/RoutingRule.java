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

package com.alibaba.cloud.commons.governance.routing;

import java.util.List;
import java.util.Objects;

/**
 * @author HH
 * @since 2.2.10-RC1
 */
public class RoutingRule {

	private String defaultRoutingVersion;

	private List<MatchService> matchRouteList;

	public String getDefaultRouteVersion() {
		return defaultRoutingVersion;
	}

	public void setDefaultRouteVersion(String defaultRouteVersion) {
		this.defaultRoutingVersion = defaultRouteVersion;
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
		RoutingRule that = (RoutingRule) o;
		return Objects.equals(defaultRoutingVersion, that.defaultRoutingVersion)
				&& Objects.equals(getMatchRouteList(), that.getMatchRouteList());
	}

	@Override
	public int hashCode() {
		return Objects.hash(defaultRoutingVersion, getMatchRouteList());
	}

	@Override
	public String toString() {
		return "LabelRouteData{" + "defaultRouteVersion='" + defaultRoutingVersion + '\''
				+ ", matchRouteList=" + matchRouteList + '}';
	}

}
