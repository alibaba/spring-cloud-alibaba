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

package com.alibaba.alicloud.context.sms;

import com.alibaba.alicloud.context.AliCloudContextAutoConfiguration;
import com.alibaba.alicloud.context.ans.AnsContextAutoConfiguration;
import com.alibaba.alicloud.context.edas.EdasContextAutoConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author xiaolongzuo
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = { AliCloudContextAutoConfiguration.class,
		EdasContextAutoConfiguration.class, AnsContextAutoConfiguration.class,
		SmsContextAutoConfiguration.class }, properties = {
				"spring.cloud.alicloud.sms.reportQueueName=q1",
				"spring.cloud.alicloud.sms.upQueueName=q2",
				"spring.cloud.alicloud.sms.connect-timeout=20",
				"spring.cloud.alicloud.sms.read-timeout=30" })
public class SmsPropertiesLoadTests {

	@Autowired
	private SmsProperties smsProperties;

	@Test
	public void test() {
		assertThat(smsProperties.getReportQueueName()).isEqualTo("q1");
		assertThat(smsProperties.getUpQueueName()).isEqualTo("q2");
		assertThat(smsProperties.getConnectTimeout()).isEqualTo("20");
		assertThat(smsProperties.getReadTimeout()).isEqualTo("30");
	}
}
