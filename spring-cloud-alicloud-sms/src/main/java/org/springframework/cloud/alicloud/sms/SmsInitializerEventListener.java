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
package org.springframework.cloud.alicloud.sms;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.alicloud.context.sms.SmsProperties;
import org.springframework.cloud.alicloud.sms.base.MessageListener;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author pbting
 */
@Component
public class SmsInitializerEventListener
		implements ApplicationListener<ApplicationStartedEvent> {

	private final AtomicBoolean isCalled = new AtomicBoolean(false);

	private SmsProperties msConfigProperties;

	private ISmsService smsService;

	public SmsInitializerEventListener(SmsProperties msConfigProperties,
			ISmsService smsService) {
		this.msConfigProperties = msConfigProperties;
		this.smsService = smsService;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		if (!isCalled.compareAndSet(false, true)) {
			return;
		}

		// 整个application context refreshed then do
		// 可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout",
				msConfigProperties.getConnectTimeout());
		System.setProperty("sun.net.client.defaultReadTimeout",
				msConfigProperties.getReadTimeout());
		// 初始化acsClient,暂不支持region化
		try {
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou",
					SmsProperties.smsProduct, SmsProperties.smsDomain);
			Collection<MessageListener> messageListeners = event.getApplicationContext()
					.getBeansOfType(MessageListener.class).values();
			if (messageListeners.isEmpty()) {
				return;
			}

			for (MessageListener messageListener : messageListeners) {
				if (SmsReportMessageListener.class.isInstance(messageListener)) {
					if (msConfigProperties.getReportQueueName() != null
							&& msConfigProperties.getReportQueueName().trim()
									.length() > 0) {
						smsService.startSmsReportMessageListener(
								(SmsReportMessageListener) messageListener);
						continue;
					}

					throw new IllegalArgumentException("the SmsReport queue name for "
							+ messageListener.getClass().getCanonicalName()
							+ " must be set.");
				}

				if (SmsUpMessageListener.class.isInstance(messageListener)) {

					if (msConfigProperties.getUpQueueName() != null
							&& msConfigProperties.getUpQueueName().trim().length() > 0) {
						smsService.startSmsUpMessageListener(
								(SmsUpMessageListener) messageListener);
						continue;
					}

					throw new IllegalArgumentException("the SmsUp queue name for "
							+ messageListener.getClass().getCanonicalName()
							+ " must be set.");
				}
			}
		}
		catch (ClientException e) {
			throw new RuntimeException(
					"initialize sms profile end point cause an exception");
		}
	}
}