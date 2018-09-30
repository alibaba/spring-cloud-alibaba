# OSS Example

## 项目说明

如果您的应用是 Spring Cloud 应用，且需要使用阿里云的 OSS 服务进行云端的文件存储，例如电商业务中常见的商品图片存储，那么您可以使用 OSS starter 完成 Spring Cloud 应用的对象存储。

阿里云对象存储服务（Object Storage Service，简称 OSS），是阿里云提供的海量、安全、低成本、高可靠的云存储服务。您可以在任何应用、任何时间、任何地点存储和访问任意类型的数据。更多 OSS 相关的信息，请参考 [OSS官网](https://www.aliyun.com/product/oss)。

## 示例

### 接入 OSS
在启动示例进行演示之前，我们先了解一下如何接入 OSS。

**注意：本节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您只需修改 accessKeyId、secretAccessKey、region 即可。**

1. 修改 pom.xml 文件，引入 alicloud-oss starter。	

	    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-alicloud-oss</artifactId>
        </dependency>
	
2. 在配置文件中配置 OSS 服务对应的 accessKeyId、secretAccessKey 和 region。

		// application.properties
		spring.cloud.alibaba.oss.accessKeyId=your-ak
		spring.cloud.alibaba.oss.secretAccessKey=your-sk
		spring.cloud.alibaba.oss.region=cn-beijing
		  
    以阿里云 accessKeyId、secretAccessKey 为例，获取方式如下。

    i. 在阿里云控制台界面，单击右上角头像，选择 accesskeys，或者直接登录[用户信息管理界面](https://usercenter.console.aliyun.com/)：
		  
      ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535371973274-3ebec90a-ebde-4eb7-96ed-5372f6b32fe0.png) 

    ii. 获取 accessKeyId、secretAccessKey：

      ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535372168883-b94a3d77-3f81-4938-b409-611945a9e21c.png) 
 
   **注意：**如果您使用了阿里云 [STS服务](https://help.aliyun.com/document_detail/28756.html) 进行短期访问权限管理，则除了 accessKeyId、secretAccessKey、region 以外，还需配置 securityToken。
	
3. 注入 OSSClient 并进行文件上传下载等操作。

		@Service
		public class YourService {
			@Autowired
			private OSSClient ossClient;

			public void saveFile() {
				// download file to local
				ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File("pathOfYourLocalFile"));
			}
		}

  **说明：**直接注入OSSClient 方式通常用于大量文件对象操作的场景。如果仅仅是需要读取文件对象内容，OSS Starter 也支持以 Resource 方式读取文件，详情请参考[下文](#1)。

### 启动应用


1. 在应用的 /src/main/resources/application.properties 中添加基本配置信息和 OSS 配置。
	
		spring.application.name=oss-example
		server.port=18084
		spring.cloud.alibaba.oss.accessKeyId=your-ak
		spring.cloud.alibaba.oss.secretAccessKey=your-sk
		spring.cloud.alibaba.oss.region=cn-beijing
		
2. 通过 IDE 直接启动或者编译打包后启动应用。

	- IDE直接启动：找到主类 `OSSApplication`，执行 main 方法启动应用。
	- 打包编译后启动：
	  1. 执行 `mvn clean package` 将工程编译打包；
	  2. 执行 `java -jar oss-example.jar`启动应用。
	  
应用启动后会自动在 OSS 上创建一个名为 `spring-cloud-alibaba` 的 Bucket。	 

### 上传或下载文件

#### 上传文件
使用 curl 调用上传接口 upload。该接口会上传 classpath 下的的 oss-test.json 文件。文件内容是一段 json:

    curl http://localhost:18084/upload
	
显示结果：
	
	// 如果配置正确，则输出
	upload success
	// 如果上传的过程中发生异常，则会输出 upload fail: fail reason。比如accessKeyId配置错误的时候，fail reason内容如下
	upload fail: The OSS Access Key Id you provided does not exist in our records. [ErrorCode]: InvalidAccessKeyId [RequestId]: RequestId [HostId]: xxx.oss-cn-beijing.aliyuncs.com [ResponseError]: InvalidAccessKeyId The OSS Access Key Id you provided does not exist in our records. RequestId xxx.oss-cn-beijing.aliyuncs.com xxx-accessKeyId

#### 下载文件
使用 curl 调用下载接口 download。该接口会下载刚才用 upload 接口上传的 oss-test.json 文件，并打印文件内容到结果中:

    curl http://localhost:18084/download
	
显示结果：
	
	// 如果配置正确，则输出
	download success, content: { "name": "spring-cloud-alibaba", "github": "https://github.com/spring-cloud-incubator/spring-cloud-alibaba", "authors": ["Jim", "flystar32"], "emails": ["fangjian0423@gmail.com", "flystar32@163.com"] }
	// 下载的过程中如果发生异常，则会输出download fail: fail reason。比如accessKeyId配置错误，则fail reason内容如下
	download fail: The OSS Access Key Id you provided does not exist in our records. [ErrorCode]: InvalidAccessKeyId [RequestId]: RequestId [HostId]: xxx.oss-cn-beijing.aliyuncs.com [ResponseError]: InvalidAccessKeyId The OSS Access Key Id you provided does not exist in our records. RequestId sxxx.oss-cn-beijing.aliyuncs.com xxx-accessKeyId
	


### 在 OSS 上验证结果

完成文件上传或者下载操作后，可以登录 OSS 控制台进行验证。

1. 登陆[OSS控制台](https://oss.console.aliyun.com/)，可以看到左侧 Bucket 列表新增一个名字为`spring-cloud-alibaba`的 Bucket。

   ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535369224513-387afdf9-6078-4a42-9f18-d9fe9926a9cd.png) 

2. 单击`spring-cloud-alibaba` Bucket，选择 `文件管理` 页签，发现上传的 oss-test 文件在 custom-dir 目录中。上传的 objectName 为`custom-dir/oss-test`。目录和文件以'/'符号分割。

   ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535615378605-df1381e9-c5ff-4da1-b3b3-ce9acfef313f.png) 
    	

## 查看 Endpoint 信息

Spring Boot 应用支持通过 Endpoint 来暴露相关信息，OSS Starter 也支持这一点。

**前提条件：**

在 maven 中添加 `spring-boot-starter-actuator`依赖，并在配置中允许 Endpoints 的访问。

- Spring Boot1.x 中添加配置 `management.security.enabled=false`
- Spring Boot2.x 中添加配置 `management.endpoints.web.exposure.include=*`

Spring Boot1.x 可以通过访问 http://127.0.0.1:18084/oss 来查看 OSS Endpoint 的信息。

Spring Boot2.x 可以通过访问 http://127.0.0.1:18084/acutator/oss 来访问。

Endpoint 内部会显示所有的 OSSClient 配置信息，以及该 OSSClient 对应的 Bucket 列表。

![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535373658171-20674565-6fe1-4e1e-a596-1dd6f4159ec3.png) 


## 多个 OSSClient 场景

如果您需要配置多个 OSSClient，类似多数据源的配置，则可以先构造 `OSSProperties`，再构造 `OSSClient`，并分别为每个 OSSClient 配置相应的 accessKeyId、secretAccessKey 等信息。
    
	  @Bean
	  @ConfigurationProperties(prefix = "spring.cloud.alibaba.oss1")
	  public OSSProperties ossProperties1() {
		  return new OSSProperties();
	  }

	  @Bean
	  public OSS ossClient1(@Qualifier("ossProperties1") OSSProperties ossProperties) {
		  return new OSSClientBuilder().build(ossProperties.getEndpoint(),
				  ossProperties.getAccessKeyId(), ossProperties.getSecretAccessKey(),
				  ossProperties.getSecurityToken(), ossProperties.getConfiguration());
	  }

<h2 id="1"> 以 Resource 的形式读取文件 </h2>

OSS Starter 支持以 Resource 的形式得到文件对象。如果只需读取少量文件，您可以使用这种方式。

**使用方法：**

只需配置 OSS 协议对应的 Resource 即可：

	  @Value("oss://spring-cloud-alibaba/oss-test")
	  private Resource file;
	  
	  // 文件内容的读取
	  StreamUtils.copyToString(file.getInputStream(), Charset.forName(CharEncoding.UTF_8))

## 关闭 OSSClient 服务
如果您不再需要 OSS 服务，您也无需手动关闭 OSSClient。在 ApplicationContext close 的时候，OSS Starter 会在 `OSSApplicationListener` 中调用所有 OSSClient 的 shutdown 方法。


如果您对 Spring Cloud OSS Starter 有任何建议或想法，欢迎提交 issue 中或者通过其他社区渠道向我们反馈。

