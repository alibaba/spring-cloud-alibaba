/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.example;

import java.util.LinkedList;
import java.util.List;

import com.aliyun.mns.model.Message;

import org.springframework.stereotype.Component;

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
