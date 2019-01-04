package org.springframework.cloud.alicloud.sms.base;

import com.aliyun.mns.model.Message;

public interface MessageListener {

	boolean dealMessage(Message message);

}
