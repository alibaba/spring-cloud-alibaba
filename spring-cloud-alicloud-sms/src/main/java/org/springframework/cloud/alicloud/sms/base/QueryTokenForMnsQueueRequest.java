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
package org.springframework.cloud.alicloud.sms.base;

import com.aliyuncs.RpcAcsRequest;

public class QueryTokenForMnsQueueRequest
		extends RpcAcsRequest<QueryTokenForMnsQueueResponse> {
	private String resourceOwnerAccount;
	private String messageType;
	private Long resourceOwnerId;
	private Long ownerId;

	public QueryTokenForMnsQueueRequest() {
		super("Dybaseapi", "2017-05-25", "QueryTokenForMnsQueue");
	}

	public String getResourceOwnerAccount() {
		return this.resourceOwnerAccount;
	}

	public void setResourceOwnerAccount(String resourceOwnerAccount) {
		this.resourceOwnerAccount = resourceOwnerAccount;
		if (resourceOwnerAccount != null) {
			this.putQueryParameter("ResourceOwnerAccount", resourceOwnerAccount);
		}

	}

	public String getMessageType() {
		return this.messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
		if (messageType != null) {
			this.putQueryParameter("MessageType", messageType);
		}

	}

	public Long getResourceOwnerId() {
		return this.resourceOwnerId;
	}

	public void setResourceOwnerId(Long resourceOwnerId) {
		this.resourceOwnerId = resourceOwnerId;
		if (resourceOwnerId != null) {
			this.putQueryParameter("ResourceOwnerId", resourceOwnerId.toString());
		}

	}

	public Long getOwnerId() {
		return this.ownerId;
	}

	public void setOwnerId(Long ownerId) {
		this.ownerId = ownerId;
		if (ownerId != null) {
			this.putQueryParameter("OwnerId", ownerId.toString());
		}

	}

	public Class<QueryTokenForMnsQueueResponse> getResponseClass() {
		return QueryTokenForMnsQueueResponse.class;
	}
}