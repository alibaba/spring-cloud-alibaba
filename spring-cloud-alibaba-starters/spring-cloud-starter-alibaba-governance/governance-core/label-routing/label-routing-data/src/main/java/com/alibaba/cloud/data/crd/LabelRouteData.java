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

import com.alibaba.cloud.data.rule.RouteRule;

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
	private List<Map<List<RouteRule>, ServiceMetadata>> matchRouteList;

}
