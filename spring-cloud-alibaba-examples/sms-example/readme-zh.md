# SMS Example

## 项目说明

如果您的应用是 Spring Cloud/Boot 应用，且需要使用阿里云的 SMS 服务来发送短信，例如登录验证码，那么您可以使用 SMS starter 完成 Spring Cloud/Boot 应用的短信发送。

短信服务（Short Message Service）是阿里云为用户提供的一种通信服务的能力。支持国内和国际快速发送验证码、短信通知和推广短信，服务范围覆盖全球200多个国家和地区。更多可参考 [官网文档](https://help.aliyun.com/document_detail/60704.html?spm=5176.8195934.1283918.6.18924183bHPct2)

## 示例

### 接入 SMS

在启动示例进行演示之前，我们先了解一下如何接入 SMS。

**注意：本节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您只需修改 accessKey、secretKey 即可。**

1. 修改 pom.xml 文件，引入 alicloud-sms starter。
	
	```xml
	<dependency>
	    <groupId>com.alibaba.cloud</groupId>
	    <artifactId>spring-cloud-starter-alicloud-sms</artifactId>
	</dependency>
	```
	
1. 在配置文件中配置 SMS 服务对应的 accessKey、secretKey 。

	```properties
	spring.cloud.alicloud.access-key=your-ak
	spring.cloud.alicloud.secret-key=your-sk
	```
		  
	以阿里云 accessKey、secretKey 为例，获取方式如下。

	i. 在阿里云控制台界面，单击右上角头像，选择 accesskeys，或者直接登录[用户信息管理界面](https://usercenter.console.aliyun.com/)：
	      
	  ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535371973274-3ebec90a-ebde-4eb7-96ed-5372f6b32fe0.png) 
	
	ii. 获取 accessKey、secretKey：
	
	  ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535372168883-b94a3d77-3f81-4938-b409-611945a9e21c.png) 

	**注意：**如果您使用了阿里云 [STS服务](https://help.aliyun.com/document_detail/28756.html) 进行短期访问权限管理，则除了 accessKey、secretKey 以外，还需配置 securityToken。
	
1. 注入 ISmsService 实例并进行短信发送等操作。

	```java
	@RestController
    public class SmsController {
        @Autowired
        private ISmsService smsService ;
        
        @RequestMapping("/send.do")
        public SendSmsResponse sendMessage(String telphone,String code) {
            // 组装请求对象-具体描述见控制台-文档部分内容
            SendSmsRequest request = new SendSmsRequest();
            // 必填:待发送手机号
            request.setPhoneNumbers(telphone);
            // 必填:短信签名-可在短信控制台中找到
            request.setSignName("******");
            // 必填:短信模板-可在短信控制台中找到
            request.setTemplateCode("******");
            // 可选:模板中的变量替换JSON串,如模板内容为"【企业级分布式应用服务】,您的验证码为${code}"时,此处的值为
            request.setTemplateParam("{\"code\":\"" + code + "\"}");
            SendSmsResponse sendSmsResponse ;
            try {
                sendSmsResponse = smsService.sendSmsRequest(request);
            }
            catch (ClientException e) {
                e.printStackTrace();
                sendSmsResponse = new SendSmsResponse();
            }
            return sendSmsResponse ;
        }
    }
	```

  **说明：** 直接注入 ISmsService 方式即可。

### 启动应用


1. 在应用的 /src/main/resources/application.properties 中添加基本配置信息。
	
	```properties	
	spring.application.name=sms-example
	server.port=18084
	spring.cloud.alicloud.access-key=your-ak
	spring.cloud.alicloud.secret-key=your-sk
	```	
		
2. 通过 IDE 直接启动或者编译打包后启动应用。

	- IDE直接启动：找到主类 `SMSApplication`，执行 main 方法启动应用。
	- 打包编译后启动：
	  1. 执行 `mvn clean package` 将工程编译打包；
	  2. 执行 `java -jar sms-example.jar`启动应用。
	  
应用启动后访问 http://localhost:18084/send.do?telphone=******&code=6580。 查看返回的 SendSmsResponse 的 json 串结果。 


### 批量短信发送

参考以下的 Example ，来快速开发一个具有批量短信发送的功能。在 Controller 中或者新建一个 Controler 新增如下代码：


```java
@RequestMapping("/batch-sms-send.do")
public SendBatchSmsResponse batchsendCheckCode(
        @RequestParam(name = "code") String code) {
    // 组装请求对象
    SendBatchSmsRequest request = new SendBatchSmsRequest();
    // 使用 GET 提交
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
    SendBatchSmsResponse sendSmsResponse ;
    try {
        sendSmsResponse = smsService
                .sendSmsBatchRequest(request);
        return sendSmsResponse;
    }
    catch (ClientException e) {
        e.printStackTrace();
        sendSmsResponse =  new SendBatchSmsResponse();
    }
    return sendSmsResponse ;
}
```

### 短信查询
  
参考以下的 Example ，可以快速开发根据某个指定的号码查询短信历史发送状态。在 Controller 中或者新建一个 Controler 新增如下代码：   
    
```java
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
    
```   
 
查询成功后的返回结果如下所示：

   ```plain
    {
      "requestId": "0030EE65-25B1-43EE-BA90-D8FDACC45DC7",
      "code": "OK",
      "message": "OK",
      "totalCount": "3",
      "smsSendDetailDTOs": [
        {
          "phoneNum": "152********",
          "sendStatus": 3,
          "errCode": "DELIVRD",
          "templateCode": "SMS_******",
          "content": "【企业级分布式应用服务】验证码为：1080，您正在注册成为平台会员，感谢您的支持！",
          "sendDate": "2019-01-03 22:09:09",
          "receiveDate": "2019-01-03 22:09:21",
          "outId": "edasTraceId"
        },
        {
          "phoneNum": "152********",
          "sendStatus": 3,
          "errCode": "DELIVRD",
          "templateCode": "SMS_******",
          "content": "【企业级分布式应用服务】验证码为：1865，您正在注册成为平台会员，感谢您的支持！",
          "sendDate": "2019-01-03 21:13:30",
          "receiveDate": "2019-01-03 21:13:37",
          "outId": "edasTraceId"
        },
        {
          "phoneNum": "152********",
          "sendStatus": 3,
          "errCode": "DELIVRD",
          "templateCode": "SMS_*******",
          "content": "【企业级分布式应用服务】验证码为：9787，您正在注册成为平台会员，感谢您的支持！",
          "sendDate": "2019-01-03 17:19:11",
          "receiveDate": "2019-01-03 17:19:15",
          "outId": "edasTraceId"
        }
      ]
    }
   ``` 
    
### 短信回执消息

通过订阅 SmsReport 短信状态报告，可以获知每条短信的发送情况，了解短信是否达到终端用户的状态与相关信息。这些工作已经都被 Spring Cloud AliCloud SMS 封装在内部了。你只需要完成以下两步即可。

1. 在 **application.properties** 配置文件中(也可以是 application.yaml)配置 SmsReport 的队列名称。

	```properties
	spring.cloud.alicloud.sms.report-queue-name=Alicom-Queue-********-SmsReport
	```

2. 实现 SmsReportMessageListener 接口，并初始化一个 Spring Bean .

	```java
	/**
	 * 如果需要监听短信是否被对方成功接收，只需实现这个接口并初始化一个 Spring Bean 即可。
	 */
	@Component
	public class SmsReportMessageListener
			implements SmsReportMessageListener {
	
		@Override
		public boolean dealMessage(Message message) {
		    // 在这里添加你的处理逻辑
	
		    //do something
	
			System.err.println(this.getClass().getName() + "; " + message.toString());
			return true;
		}
	}
	```

发送状态的回执消息如下所示：

```plain
SmsReportMessageListener; MessageID:9F3CFCE6BB3B2C8F-2-1682D84D9AD-20000000A,MessageMD5:C6AFEE0EE560BBC3380252337AC36985,RequestID:5C349CCEB8C115CCF344A3EB,MessageBody:"{"send_time":"2019-01-08 20:51:40","report_time":"2019-01-08 20:51:47","success":true,"err_msg":"用户接收成功","err_code":"DELIVERED","phone_number":"152********","sms_size":"1","biz_id":"667618746951900475^0","out_id":"edasTraceId"}",ReceiptHandle:"1-ODU4OTkzNDYwMi0xNTQ2OTUxOTM3LTItOA==",DequeueCount:"1",EnqueueTime:"Tue Jan 08 20:51:47 CST 2019",FirstDequeueTime:"Tue Jan 08 20:51:47 CST 2019",NextVisibleTime:"Tue Jan 08 20:52:17 CST 2019",Priority:"8"
```

### 上行短信消息   
 

通过订阅SmsUp上行短信消息，可以获知终端用户回复短信的内容。这些工作也已经被 Spring Cloud AliCloud SMS 封装好了。你只需要完成以下两步即可。 
    	
1. 在 **application.properties** 配置文件中(也可以是 application.yaml)配置 SmsReport 的队列名称。

	```properties
	spring.cloud.alicloud.sms.up-queue-name=Alicom-Queue-********-SmsUp
	```

1. 实现 SmsUpMessageListener 接口，并初始化一个 Spring Bean 。

	```java
	/**
	 * 如果发送的短信需要接收对方回复的状态消息，只需实现该接口并初始化一个 Spring Bean 即可。
	 */
	@Component
	public class SmsUpMessageListener
			implements SmsUpMessageListener {
	
		@Override
		public boolean dealMessage(Message message) {
		    // 在这里添加你的处理逻辑
	
	    	//do something
	
			System.err.println(this.getClass().getName() + "; " + message.toString());
			return true;
		}
	}
	```

短信成功恢复后，上行短信消息 SmsUpMessageListener 回调后的信息如下所示：

```plain
SmsUpMessageListener; MessageID:BF030215BA85BB41-1-1682D85425F-400000003,MessageMD5:D1AF5C2D7410EF190532CBF8E17FE2B7,RequestID:5C349CEE36AF628D2A847D50,MessageBody:"{"dest_code":"2493559","send_time":"2019-01-08 20:52:14","sign_name":"【企业级分布式应用服务】","sequence_id":568585703,"phone_number":"152********","content":"5279"}",ReceiptHandle:"1-MTcxNzk4NjkxODctMTU0Njk1MTk2NC0xLTg=",DequeueCount:"1",EnqueueTime:"Tue Jan 08 20:52:14 CST 2019",FirstDequeueTime:"Tue Jan 08 20:52:14 CST 2019",NextVisibleTime:"Tue Jan 08 20:52:44 CST 2019",Priority:"8"
```

## 查看 Endpoint 信息

Spring Boot 应用支持通过 Endpoint 来暴露相关信息，SMS Starter 也支持这一点。

**前提条件：**

在 maven 中添加 `spring-boot-starter-actuator`依赖，并在配置中允许 Endpoints 的访问。

- Spring Boot1.x 中添加配置 `management.security.enabled=false`
- Spring Boot2.x 中添加配置 `management.endpoints.web.exposure.include=*`

Spring Boot1.x 可以通过访问 http://127.0.0.1:18084/sms-info 来查看 SMS Endpoint 的信息。

Spring Boot2.x 可以通过访问 http://127.0.0.1:18084/actuator/sms-info 来访问。

Endpoint 内部会显示最近 20 条单个短信发送的记录和批量短信发送的记录，以及当前短信消息的配置信息(包括是**SmsReport** 还是 **SmsUp**，**队列名称**，以及对应的 **MessageListener** )。


如果您对 Spring Cloud SMS Starter 有任何建议或想法，欢迎提交 issue 中或者通过其他社区渠道向我们反馈。
