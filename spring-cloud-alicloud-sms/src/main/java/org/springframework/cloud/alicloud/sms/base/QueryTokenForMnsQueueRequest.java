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