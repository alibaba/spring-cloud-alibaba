package com.alibaba.cloud.example;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.aliyun.mns.model.Message;

/**
 * @author 如果需要监听短信是否被对方成功接收，只需实现这个接口并初始化一个 Spring Bean 即可。
 */
@Component
public class SmsReportMessageListener
		implements com.alibaba.alicloud.sms.SmsReportMessageListener {
	private List<Message> smsReportMessageSet = new LinkedList<>();

	@Override
	public boolean dealMessage(Message message) {
		smsReportMessageSet.add(message);
		System.err.println(this.getClass().getName() + "; " + message.toString());
		return true;
	}

	public List<Message> getSmsReportMessageSet() {

		return smsReportMessageSet;
	}
}