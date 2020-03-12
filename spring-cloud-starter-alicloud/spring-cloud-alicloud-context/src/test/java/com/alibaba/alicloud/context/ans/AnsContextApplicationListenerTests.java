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

package com.alibaba.alicloud.context.ans;

import com.alibaba.alicloud.context.BaseAliCloudSpringApplication;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * @author xiaolongzuo
 */
public class AnsContextApplicationListenerTests extends BaseAliCloudSpringApplication {

	@Test
	public void testAnsContextApplicationListenerDefault() {
		assertThat(System
				.getProperty("com.alibaba.ans.shaded.com.taobao.vipserver.serverlist"))
						.isEqualTo("192.168.1.100");
		assertThat(System.getProperty("vipserver.server.port")).isEqualTo("8888");
	}

}
