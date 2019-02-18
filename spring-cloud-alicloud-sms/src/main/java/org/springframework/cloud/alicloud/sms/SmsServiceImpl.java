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

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.alicloud.context.AliCloudProperties;
import org.springframework.cloud.alicloud.context.sms.SmsProperties;
import org.springframework.cloud.alicloud.sms.base.DefaultAlicomMessagePuller;
import org.springframework.cloud.alicloud.sms.endpoint.EndpointManager;
import org.springframework.cloud.alicloud.sms.endpoint.ReceiveMessageEntity;

import com.aliyuncs.dysmsapi.model.v20170525.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;

/**
 * @author pbting
 */
public final class SmsServiceImpl extends AbstractSmsService {

	private static final Log log = LogFactory.getLog(SmsServiceImpl.class);
	/**
	 * will expose user to call this method send sms message
	 * @param sendSmsRequest
	 * @return
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

	/**
	 * 因为阿里云支持多个
	 * accessKeyId/accessKeySecret,当不想使用默认的配置accessKeyId/accessKeySecret时，可以使用这个方法来支持额外
	 * 的accessKeyId/accessKeySecret 发送
	 * @param sendSmsRequest
	 * @param accessKeyId
	 * @param accessKeySecret
	 * @throws ServerException
	 * @throws ClientException
	 * @return SendSmsResponse
	 */
	@Override
	public SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest,
			String accessKeyId, String accessKeySecret)
			throws ServerException, ClientException {
		EndpointManager.addSendSmsRequest(sendSmsRequest);
		// hint 此处可能会抛出异常，注意catch
		return getHangZhouRegionClientProfile(accessKeyId, accessKeySecret)
				.getAcsResponse(sendSmsRequest);
	}

	/**
	 * 
	 * @param smsReportMessageListener
	 * @return boolean
	 */
	@Override
	public boolean startSmsReportMessageListener(
			SmsReportMessageListener smsReportMessageListener) {
		// 短信回执：SmsReport，短信上行：SmsUp
		String messageType = "SmsReport";
		String queueName = smsProperties.getReportQueueName();
		return startReceiveMsg(messageType, queueName, smsReportMessageListener);
	}

	/**
	 * 
	 * @param smsUpMessageListener
	 * @return boolean
	 */
	@Override
	public boolean startSmsUpMessageListener(SmsUpMessageListener smsUpMessageListener) {
		// 短信回执：SmsReport，短信上行：SmsUp
		String messageType = "SmsUp";
		String queueName = smsProperties.getUpQueueName();
		return startReceiveMsg(messageType, queueName, smsUpMessageListener);
	}

	/**
	 * 
	 * @param messageType
	 * @param queueName
	 * @param messageListener
	 * @return boolean
	 */
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

	/**
	 * 
	 * @param sendBatchSmsRequest
	 * @throws ServerException
	 * @throws ClientException
	 * @return SendBatchSmsResponse
	 */
	@Override
	public SendBatchSmsResponse sendSmsBatchRequest(
			SendBatchSmsRequest sendBatchSmsRequest)
			throws ServerException, ClientException {

		return sendSmsBatchRequest(sendBatchSmsRequest, aliCloudProperties.getAccessKey(),
				aliCloudProperties.getSecretKey());
	}

	/**
	 * 
	 * @param sendBatchSmsRequest
	 * @param accessKeyId
	 * @param accessKeySecret
	 * @throws ClientException
	 * @return SendBatchSmsResponse
	 */
	@Override
	public SendBatchSmsResponse sendSmsBatchRequest(
			SendBatchSmsRequest sendBatchSmsRequest, String accessKeyId,
			String accessKeySecret) throws ClientException {
		EndpointManager.addSendBatchSmsRequest(sendBatchSmsRequest);
		return getHangZhouRegionClientProfile(accessKeyId, accessKeySecret)
				.getAcsResponse(sendBatchSmsRequest);
	}

	/**
	 * 
	 * @param request
	 * @param accessKeyId
	 * @param accessKeySecret
	 * @throws ClientException
	 * @return QuerySendDetailsResponse
	 */
	@Override
	public QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request,
			String accessKeyId, String accessKeySecret) throws ClientException {
		return getHangZhouRegionClientProfile(accessKeyId, accessKeySecret)
				.getAcsResponse(request);
	}

	/**
	 * 
	 * @param request
	 * @throws ClientException
	 * @return QuerySendDetailsResponse
	 */
	@Override
	public QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request)
			throws ClientException {
		return querySendDetails(request, aliCloudProperties.getAccessKey(),
				aliCloudProperties.getSecretKey());
	}
}