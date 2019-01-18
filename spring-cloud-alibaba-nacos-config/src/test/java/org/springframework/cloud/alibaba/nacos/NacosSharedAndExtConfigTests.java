/*
 * Copyright (C) 2019 the original author or authors.
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
package org.springframework.cloud.alibaba.nacos;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author pbting
 * @date 2019-01-17 11:46 AM
 */
public class NacosSharedAndExtConfigTests extends NacosPowerMockitBaseTests {
	private final static Logger log = LoggerFactory
			.getLogger(NacosSharedAndExtConfigTests.class);

	@Test
	public void testSharedConfigPriority() {
		String userName = this.context.getEnvironment().getProperty("user.address");
		assertThat(userName).isEqualTo("zhejiang-ningbo");
	}

	@Test
	public void testSharedConfigRefresh() {

		while (true) {
			// ContextRefresher contextRefresher = this.context
			// .getBean(ContextRefresher.class);
			// contextRefresher.refresh();
			String userName = this.context.getEnvironment().getProperty("user.address");
			try {
				assertThat(userName).isEqualTo("zhejiang-ningbo");
				TimeUnit.SECONDS.sleep(1);
				log.info("user name is {}", userName);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 真实测试时将这里 注释掉
			break;
		}
	}

	@Test
	public void testExtConfigPriority() {
		String extKey = this.context.getEnvironment().getProperty("ext.key");
		assertThat(extKey).isEqualTo("ext.value02");
	}

	@Test
	public void testExtOtherGroup() {
		String userExt = this.context.getEnvironment().getProperty("user.ext");
		assertThat(userExt).isEqualTo("EXT01_GROUP-value");
	}

	@Test
	public void testExtRefresh() {
		while (true) {
			// ContextRefresher contextRefresher = this.context
			// .getBean(ContextRefresher.class);
			// contextRefresher.refresh();
			String userExt = this.context.getEnvironment().getProperty("user.ext");
			try {
				assertThat(userExt).isEqualTo("EXT01_GROUP-value");
				TimeUnit.SECONDS.sleep(1);
				log.info("user name is {}", userExt);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			break;
		}
	}
}