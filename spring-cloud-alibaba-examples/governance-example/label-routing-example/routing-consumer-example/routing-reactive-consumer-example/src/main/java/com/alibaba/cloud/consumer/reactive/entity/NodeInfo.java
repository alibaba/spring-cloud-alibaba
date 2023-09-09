/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.consumer.reactive.entity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@Component
public final class NodeInfo {

	private static Map<String, List<Map<String, List<String>>>> nodeIno = new ConcurrentHashMap<>();

	private NodeInfo() {
	}

	public static void set(String var1, List<Map<String, List<String>>> var2) {

		nodeIno.put(var1, var2);
	}

	public static List<Map<String, List<String>>> get(String var) {

		return nodeIno.get(var);
	}

	public static Map<String, List<Map<String, List<String>>>> getNodeIno() {

		return nodeIno;
	}

}
