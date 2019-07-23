package com.alibaba.cloud.example;

import org.springframework.stereotype.Component;

import com.aliyun.mns.model.Message;

/**
 * @author 如果发送的短信需要接收对方回复的状态消息，只需实现该接口并初始化一个 Spring Bean 即可。
 */
@Component
public class SmsUpMessageListener
		implements com.alibaba.alicloud.sms.SmsUpMessageListener {

	@Override
	public boolean dealMessage(Message message) {
		System.err.println(this.getClass().getName() + "; " + message.toString());
		return true;
	}
}