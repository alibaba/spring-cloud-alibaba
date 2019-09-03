package com.alibaba.cloud.example;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.alicloud.sms.ISmsService;

import com.aliyun.mns.model.Message;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.QuerySendDetailsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendBatchSmsResponse;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;

@RestController
public class SmsController {

	@Autowired
	private Environment environment;

	@Autowired
	private ISmsService smsService;

	@Autowired
	private SmsReportMessageListener smsReportMessageListener;

	@GetMapping("/report-queue.do")
	public String getSmsReportQueuename() {

		return environment.getProperty("spring.cloud.alicloud.sms.up-queue-name");
	}

	/**
	 * 短信发送 Example
	 * @param code
	 * @return
	 */
	@RequestMapping("/batch-sms-send.do")
	public SendBatchSmsResponse batchsendCheckCode(
			@RequestParam(name = "code") String code) {
		// 组装请求对象
		SendBatchSmsRequest request = new SendBatchSmsRequest();
		// 使用post提交
		request.setMethod(MethodType.GET);
		// 必填:待发送手机号。支持JSON格式的批量调用，批量上限为100个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式
		request.setPhoneNumberJson("[\"177********\",\"130********\"]");
		// 必填:短信签名-支持不同的号码发送不同的短信签名
		request.setSignNameJson("[\"*******\",\"*******\"]");
		// 必填:短信模板-可在短信控制台中找到
		request.setTemplateCode("******");
		// 必填:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
		// 友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
		request.setTemplateParamJson(
				"[{\"code\":\"" + code + "\"},{\"code\":\"" + code + "\"}]");
		// 可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
		// request.setSmsUpExtendCodeJson("[\"90997\",\"90998\"]");
		try {
			SendBatchSmsResponse sendSmsResponse = smsService
					.sendSmsBatchRequest(request);
			return sendSmsResponse;
		}
		catch (ClientException e) {
			e.printStackTrace();
		}
		return new SendBatchSmsResponse();
	}

	/**
	 * 短信发送 Example
	 * @param code
	 * @return
	 */
	@RequestMapping("/sms-send.do")
	public SendSmsResponse sendCheckCode(@RequestParam(name = "code") String code) {
		// 组装请求对象-具体描述见控制台-文档部分内容
		SendSmsRequest request = new SendSmsRequest();
		// 必填:待发送手机号
		request.setPhoneNumbers("******");
		// 必填:短信签名-可在短信控制台中找到
		request.setSignName("******");
		// 必填:短信模板-可在短信控制台中找到
		request.setTemplateCode("******");
		// 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
		request.setTemplateParam("{\"code\":\"" + code + "\"}");

		// 选填-上行短信扩展码(无特殊需求用户请忽略此字段)
		// request.setSmsUpExtendCode("90997");

		// 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
		request.setOutId("****TraceId");
		try {
			SendSmsResponse sendSmsResponse = smsService.sendSmsRequest(request);
			return sendSmsResponse;
		}
		catch (ClientException e) {
			e.printStackTrace();
		}
		return new SendSmsResponse();
	}

	/**
	 *
	 * 短信查询 Example
	 * @param telephone
	 * @return
	 */
	@RequestMapping("/query.do")
	public QuerySendDetailsResponse querySendDetailsResponse(
			@RequestParam(name = "tel") String telephone) {
		// 组装请求对象
		QuerySendDetailsRequest request = new QuerySendDetailsRequest();
		// 必填-号码
		request.setPhoneNumber(telephone);
		// 必填-短信发送的日期 支持30天内记录查询（可查其中一天的发送数据），格式yyyyMMdd
		request.setSendDate("20190103");
		// 必填-页大小
		request.setPageSize(10L);
		// 必填-当前页码从1开始计数
		request.setCurrentPage(1L);
		try {
			QuerySendDetailsResponse response = smsService.querySendDetails(request);
			return response;
		}
		catch (ClientException e) {
			e.printStackTrace();
		}

		return new QuerySendDetailsResponse();
	}

	@RequestMapping("/sms-report.do")
	public List<Message> smsReport() {

		return smsReportMessageListener.getSmsReportMessageSet();
	}
}