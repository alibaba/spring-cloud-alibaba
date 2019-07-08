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

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;

/**
 * @author pbting
 */
public interface ISmsService {

	/**
	 * 
	 * @param accessKeyId
	 * @param secret
	 * @return IAcsClient
	 */
	IAcsClient getHangZhouRegionClientProfile(String accessKeyId, String secret);

	/**
	 *
	 * @param sendSmsRequest
	 * @throws ServerException
	 * @throws ClientException
	 * @return SendSmsResponse
	 */
	SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest)
			throws ServerException, ClientException;

	/**
	 *
	 * @param sendBatchSmsRequest
	 * @throws ServerException
	 * @throws ClientException
	 * @return SendBatchSmsResponse
	 */
	SendBatchSmsResponse sendSmsBatchRequest(SendBatchSmsRequest sendBatchSmsRequest)
			throws ServerException, ClientException;

	/**
	 * 因为阿里云支持多个
	 * accessKeyId/accessKeySecret,当不想使用默认的配置accessKeyId/accessKeySecret时，可以使用这个方法来支持额外
	 * 的accessKeyId/accessKeySecret 发送
	 * @param sendSmsRequest
	 * @param accessKeyId
	 * @param accessKeySecret
	 * @return
	 * @throws ServerException
	 * @throws ClientException
	 */
	SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest, String accessKeyId,
			String accessKeySecret) throws ServerException, ClientException;

	/**
	 *
	 * @param sendSmsRequest
	 * @param accessKeyId
	 * @param accessKeySecret
	 * @throws ServerException
	 * @throws ClientException
	 * @return SendBatchSmsResponse
	 */
	SendBatchSmsResponse sendSmsBatchRequest(SendBatchSmsRequest sendSmsRequest,
			String accessKeyId, String accessKeySecret)
			throws ServerException, ClientException;

	/**
	 *
	 * @param smsReportMessageListener
	 * @return boolean
	 */
	boolean startSmsReportMessageListener(
			SmsReportMessageListener smsReportMessageListener);

	/**
	 *
	 * @param smsUpMessageListener
	 * @return boolean
	 */
	boolean startSmsUpMessageListener(SmsUpMessageListener smsUpMessageListener);

	/**
	 * 
	 * @param request
	 * @param accessKeyId
	 * @param accessKeySecret
	 * @return QuerySendDetailsResponse
	 */
	QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request,
			String accessKeyId, String accessKeySecret) throws ClientException;

	/**
	 *
	 * @param request
	 * @return QuerySendDetailsResponse
	 */
	QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request)
			throws ClientException;
}