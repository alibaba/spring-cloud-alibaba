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
	 * @return
	 */
	IAcsClient getHangZhouRegionClientProfile(String accessKeyId, String secret);

	/**
	 *
	 * @param sendSmsRequest
	 * @return
	 * @throws ServerException
	 * @throws ClientException
	 */
	SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest)
			throws ServerException, ClientException;

	/**
	 *
	 * @param sendBatchSmsRequest
	 * @return
	 * @throws ServerException
	 * @throws ClientException
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
	 * @return
	 * @throws ServerException
	 * @throws ClientException
	 */
	SendBatchSmsResponse sendSmsBatchRequest(SendBatchSmsRequest sendSmsRequest,
			String accessKeyId, String accessKeySecret)
			throws ServerException, ClientException;

	/**
	 *
	 * @param smsReportMessageListener
	 * @return
	 */
	boolean startSmsReportMessageListener(
			SmsReportMessageListener smsReportMessageListener);

	/**
	 *
	 * @param smsUpMessageListener
	 * @return
	 */
	boolean startSmsUpMessageListener(SmsUpMessageListener smsUpMessageListener);

	/**
	 * 
	 * @param request
	 * @return
	 */
	QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request,
			String accessKeyId, String accessKeySecret) throws ClientException;

	/**
	 *
	 * @param request
	 * @return
	 */
	QuerySendDetailsResponse querySendDetails(QuerySendDetailsRequest request)
			throws ClientException;
}