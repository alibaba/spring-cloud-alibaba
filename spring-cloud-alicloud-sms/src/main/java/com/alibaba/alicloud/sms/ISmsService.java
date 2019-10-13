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

import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;

/**
 * @author pbting
 */
public interface ISmsService {

	IAcsClient getHangZhouRegionClientProfile(String accessKeyId, String secret);

	SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest)
			throws ServerException, ClientException;

	SendBatchSmsResponse sendSmsBatchRequest(SendBatchSmsRequest sendBatchSmsRequest)
			throws ServerException, ClientException;

	SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest, String accessKeyId,
			String accessKeySecret) throws ServerException, ClientException;

	SendBatchSmsResponse sendSmsBatchRequest(SendBatchSmsRequest sendSmsRequest,
			String accessKeyId, String accessKeySecret)
			throws ServerException, ClientException;

	boolean startSmsReportMessageListener(
			SmsReportMessageListener smsReportMessageListener);

	boolean startSmsUpMessageListener(SmsUpMessageListener smsUpMessageListener);

	QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request,
			String accessKeyId, String accessKeySecret) throws ClientException;

	QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request)
			throws ClientException;

}
