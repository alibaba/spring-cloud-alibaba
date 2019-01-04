package org.springframework.cloud.alicloud.sms.base;

import com.aliyuncs.transform.UnmarshallerContext;

public class QueryTokenForMnsQueueResponseUnmarshaller {

	public QueryTokenForMnsQueueResponseUnmarshaller() {
	}

	public static QueryTokenForMnsQueueResponse unmarshall(
			QueryTokenForMnsQueueResponse queryTokenForMnsQueueResponse,
			UnmarshallerContext context) {
		queryTokenForMnsQueueResponse.setRequestId(
				context.stringValue("QueryTokenForMnsQueueResponse.RequestId"));
		queryTokenForMnsQueueResponse
				.setCode(context.stringValue("QueryTokenForMnsQueueResponse.Code"));
		queryTokenForMnsQueueResponse
				.setMessage(context.stringValue("QueryTokenForMnsQueueResponse.Message"));
		QueryTokenForMnsQueueResponse.MessageTokenDTO messageTokenDTO = new QueryTokenForMnsQueueResponse.MessageTokenDTO();
		messageTokenDTO.setAccessKeyId(context.stringValue(
				"QueryTokenForMnsQueueResponse.MessageTokenDTO.AccessKeyId"));
		messageTokenDTO.setAccessKeySecret(context.stringValue(
				"QueryTokenForMnsQueueResponse.MessageTokenDTO.AccessKeySecret"));
		messageTokenDTO.setSecurityToken(context.stringValue(
				"QueryTokenForMnsQueueResponse.MessageTokenDTO.SecurityToken"));
		messageTokenDTO.setCreateTime(context
				.stringValue("QueryTokenForMnsQueueResponse.MessageTokenDTO.CreateTime"));
		messageTokenDTO.setExpireTime(context
				.stringValue("QueryTokenForMnsQueueResponse.MessageTokenDTO.ExpireTime"));
		queryTokenForMnsQueueResponse.setMessageTokenDTO(messageTokenDTO);
		return queryTokenForMnsQueueResponse;
	}
}