/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alicloud.ans.test;

import java.util.Map;

import com.alibaba.ans.shaded.com.taobao.vipserver.client.core.Host;

/**
 * @author xiaojing
 */
public class AnsMockTest {

	public static Host hostInstance(String serviceName, boolean valid,
			Map<String, String> metadata) {
		Host host = new Host();
		host.setHostname(serviceName);
		host.setValid(valid);
		return host;
	}

	public static Host hostInstance(String serviceName, boolean valid, String ip,
			int port, Map<String, String> metadata) {
		Host host = new Host();
		host.setIp(ip);
		host.setPort(port);
		host.setValid(valid);
		host.setHostname(serviceName);
		return host;
	}
}
