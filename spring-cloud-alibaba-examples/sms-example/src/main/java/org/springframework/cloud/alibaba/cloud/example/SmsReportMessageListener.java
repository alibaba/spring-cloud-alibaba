package org.springframework.cloud.alibaba.cloud.example;

import com.aliyun.mns.model.Message;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class SmsReportMessageListener
		implements org.springframework.cloud.alicloud.sms.SmsReportMessageListener {
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