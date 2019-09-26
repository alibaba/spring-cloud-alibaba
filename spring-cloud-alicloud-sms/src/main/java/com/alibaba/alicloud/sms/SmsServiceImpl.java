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

package com.alibaba.alicloud.sms;

import java.text.ParseException;

import com.alibaba.alicloud.context.AliCloudProperties;
import com.alibaba.alicloud.context.sms.SmsProperties;
import com.alibaba.alicloud.sms.base.DefaultAlicomMessagePuller;
import com.alibaba.alicloud.sms.endpoint.EndpointManager;
import com.alibaba.alicloud.sms.endpoint.ReceiveMessageEntity;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author pbting
 */
public final class SmsServiceImpl extends AbstractSmsService {

	private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

	/**
	 * will expose user to call this method send sms message.
	 */
	private SmsProperties smsProperties;

	private AliCloudProperties aliCloudProperties;

	public SmsServiceImpl(AliCloudProperties aliCloudProperties,
			SmsProperties smsProperties) {
		this.aliCloudProperties = aliCloudProperties;
		this.smsProperties = smsProperties;
	}

	@Override
	public SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest)
			throws ClientException {

		return sendSmsRequest(sendSmsRequest, aliCloudProperties.getAccessKey(),
				aliCloudProperties.getSecretKey());
	}

	@Override
	public SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest,
			String accessKeyId, String accessKeySecret)
			throws ServerException, ClientException {
		EndpointManager.addSendSmsRequest(sendSmsRequest);
		// hint 此处可能会抛出异常，注意catch
		return getHangZhouRegionClientProfile(accessKeyId, accessKeySecret)
				.getAcsResponse(sendSmsRequest);
	}

	@Override
	public boolean startSmsReportMessageListener(
			SmsReportMessageListener smsReportMessageListener) {
		// 短信回执：SmsReport，短信上行：SmsUp
		String messageType = "SmsReport";
		String queueName = smsProperties.getReportQueueName();
		return startReceiveMsg(messageType, queueName, smsReportMessageListener);
	}

	@Override
	public boolean startSmsUpMessageListener(SmsUpMessageListener smsUpMessageListener) {
		// 短信回执：SmsReport，短信上行：SmsUp
		String messageType = "SmsUp";
		String queueName = smsProperties.getUpQueueName();
		return startReceiveMsg(messageType, queueName, smsUpMessageListener);
	}

	private boolean startReceiveMsg(String messageType, String queueName,
			SmsMessageListener messageListener) {
		String accessKeyId = aliCloudProperties.getAccessKey();
		String accessKeySecret = aliCloudProperties.getSecretKey();
		boolean result = true;
		try {
			new DefaultAlicomMessagePuller().startReceiveMsg(accessKeyId, accessKeySecret,
					messageType, queueName, messageListener);
			EndpointManager.addReceiveMessageEntity(
					new ReceiveMessageEntity(messageType, queueName, messageListener));
		}
		catch (ClientException e) {
			log.error("start sms report message listener cause an exception", e);
			result = false;
		}
		catch (ParseException e) {
			log.error("start sms report message listener cause an exception", e);
			result = false;
		}
		return result;
	}

	@Override
	public SendBatchSmsResponse sendSmsBatchRequest(
			SendBatchSmsRequest sendBatchSmsRequest)
			throws ServerException, ClientException {

		return sendSmsBatchRequest(sendBatchSmsRequest, aliCloudProperties.getAccessKey(),
				aliCloudProperties.getSecretKey());
	}

	@Override
	public SendBatchSmsResponse sendSmsBatchRequest(
			SendBatchSmsRequest sendBatchSmsRequest, String accessKeyId,
			String accessKeySecret) throws ClientException {
		EndpointManager.addSendBatchSmsRequest(sendBatchSmsRequest);
		return getHangZhouRegionClientProfile(accessKeyId, accessKeySecret)
				.getAcsResponse(sendBatchSmsRequest);
	}

	@Override
	public QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request,
			String accessKeyId, String accessKeySecret) throws ClientException {
		return getHangZhouRegionClientProfile(accessKeyId, accessKeySecret)
				.getAcsResponse(request);
	}

	@Override
	public QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request)
			throws ClientException {
		return querySendDetails(request, aliCloudProperties.getAccessKey(),
				aliCloudProperties.getSecretKey());
	}

}
