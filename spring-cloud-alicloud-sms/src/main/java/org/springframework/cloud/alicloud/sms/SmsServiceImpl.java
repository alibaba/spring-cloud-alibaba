package org.springframework.cloud.alicloud.sms;

import com.aliyuncs.dysmsapi.model.v20170525.*;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.alicloud.context.sms.SmsConfigProperties;
import org.springframework.cloud.alicloud.sms.base.DefaultAlicomMessagePuller;

import java.text.ParseException;

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
	private SmsConfigProperties smsConfigProperties;

	public SmsServiceImpl(SmsConfigProperties smsConfigProperties) {
		this.smsConfigProperties = smsConfigProperties;
	}

	public SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest)
			throws ServerException, ClientException {

		return sendSmsRequest(sendSmsRequest, smsConfigProperties.getAccessKeyId(),
				smsConfigProperties.getAccessKeySecret());
	}

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
	public SendSmsResponse sendSmsRequest(SendSmsRequest sendSmsRequest,
			String accessKeyId, String accessKeySecret)
			throws ServerException, ClientException {
		// hint 此处可能会抛出异常，注意catch
		return getHangZhouRegionClientProfile(accessKeyId, accessKeySecret)
				.getAcsResponse(sendSmsRequest);
	}

	public boolean startSmsReportMessageListener(
			SmsReportMessageListener smsReportMessageListener) {
		String messageType = "SmsReport";// 短信回执：SmsReport，短信上行：SmsUp
		String queueName = smsConfigProperties.getReportQueueName();
		return startReceiveMsg(messageType, queueName, smsReportMessageListener);
	}

	public boolean startSmsUpMessageListener(SmsUpMessageListener smsUpMessageListener) {
		String messageType = "SmsUp";// 短信回执：SmsReport，短信上行：SmsUp
		String queueName = ((SmsConfigProperties) smsConfigProperties).getUpQueueName();
		return startReceiveMsg(messageType, queueName, smsUpMessageListener);
	}

	private boolean startReceiveMsg(String messageType, String queueName,
			SmsMessageListener messageListener) {
		String accessKeyId = smsConfigProperties.getAccessKeyId();
		String accessKeySecret = smsConfigProperties.getAccessKeySecret();
		boolean result = true;
		try {
			new DefaultAlicomMessagePuller().startReceiveMsg(accessKeyId, accessKeySecret,
					messageType, queueName, messageListener);
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

		return sendSmsBatchRequest(sendBatchSmsRequest,
				smsConfigProperties.getAccessKeyId(),
				smsConfigProperties.getAccessKeySecret());
	}

	@Override
	public SendBatchSmsResponse sendSmsBatchRequest(
			SendBatchSmsRequest sendBatchSmsRequest, String accessKeyId,
			String accessKeySecret) throws ServerException, ClientException {

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
		return querySendDetails(request, smsConfigProperties.getAccessKeyId(),
				smsConfigProperties.getAccessKeySecret());
	}
}