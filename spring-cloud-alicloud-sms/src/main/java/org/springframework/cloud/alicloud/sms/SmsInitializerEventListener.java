package org.springframework.cloud.alicloud.sms;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cloud.alicloud.context.sms.SmsConfigProperties;
import org.springframework.cloud.alicloud.sms.base.MessageListener;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author pbting
 */
@Component
public class SmsInitializerEventListener
		implements ApplicationListener<ApplicationStartedEvent> {

	private SmsConfigProperties msConfigProperties;

	private ISmsService smsService;

	public SmsInitializerEventListener(SmsConfigProperties msConfigProperties,
			ISmsService smsService) {
		this.msConfigProperties = msConfigProperties;
		this.smsService = smsService;
	}

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		// 整个application context refreshed then do
		// 可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout",
				msConfigProperties.getConnnectTimeout());
		System.setProperty("sun.net.client.defaultReadTimeout",
				msConfigProperties.getReadTimeout());
		// 初始化acsClient,暂不支持region化
		try {
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou",
					SmsConfigProperties.smsProduct, SmsConfigProperties.smsDomain);
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