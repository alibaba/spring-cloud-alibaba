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

package com.alibaba.cloud.nacos.ribbon;

import java.util.List;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.client.naming.core.Balancer;

/**
 * @author itmuch.com
 */
public class ExtendBalancer extends Balancer {
	/**
	 * 根据权重，随机选择实例
	 *
	 * @param instances 实例列表
	 * @return 选择的实例
	 */
	public static Instance getHostByRandomWeight2(List<Instance> instances) {
		return getHostByRandomWeight(instances);
	}
}
