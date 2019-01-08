package org.springframework.cloud.alibaba.cloud.example;

import org.springframework.stereotype.Component;

import com.aliyun.mns.model.Message;

@Component
public class SmsUpMessageListener
		implements org.springframework.cloud.alicloud.sms.SmsUpMessageListener {

	@Override
	public boolean dealMessage(Message message) {
		System.err.println(this.getClass().getName() + "; " + message.toString());
		return true;
	}
}