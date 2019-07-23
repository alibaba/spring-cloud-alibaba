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

import com.aliyuncs.AcsResponse;
import com.aliyuncs.transform.UnmarshallerContext;

public class QueryTokenForMnsQueueResponse extends AcsResponse {
	private String requestId;
	private String code;
	private String message;
	private QueryTokenForMnsQueueResponse.MessageTokenDTO messageTokenDTO;

	public QueryTokenForMnsQueueResponse() {
	}

	public String getRequestId() {
		return this.requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public QueryTokenForMnsQueueResponse.MessageTokenDTO getMessageTokenDTO() {
		return this.messageTokenDTO;
	}

	public void setMessageTokenDTO(
			QueryTokenForMnsQueueResponse.MessageTokenDTO messageTokenDTO) {
		this.messageTokenDTO = messageTokenDTO;
	}

	@Override
	public QueryTokenForMnsQueueResponse getInstance(UnmarshallerContext context) {
		return QueryTokenForMnsQueueResponseUnmarshaller.unmarshall(this, context);
	}

	public static class MessageTokenDTO {
		private String accessKeyId;
		private String accessKeySecret;
		private String securityToken;
		private String createTime;
		private String expireTime;

		public MessageTokenDTO() {
		}

		public String getAccessKeyId() {
			return this.accessKeyId;
		}

		public void setAccessKeyId(String accessKeyId) {
			this.accessKeyId = accessKeyId;
		}

		public String getAccessKeySecret() {
			return this.accessKeySecret;
		}

		public void setAccessKeySecret(String accessKeySecret) {
			this.accessKeySecret = accessKeySecret;
		}

		public String getSecurityToken() {
			return this.securityToken;
		}

		public void setSecurityToken(String securityToken) {
			this.securityToken = securityToken;
		}

		public String getCreateTime() {
			return this.createTime;
		}

		public void setCreateTime(String createTime) {
			this.createTime = createTime;
		}

		public String getExpireTime() {
			return this.expireTime;
		}

		public void setExpireTime(String expireTime) {
			this.expireTime = expireTime;
		}
	}
}