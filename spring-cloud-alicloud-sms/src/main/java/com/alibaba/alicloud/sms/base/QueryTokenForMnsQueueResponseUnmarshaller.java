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
package com.alibaba.alicloud.sms.base;

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