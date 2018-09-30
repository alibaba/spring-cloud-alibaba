# OSS Example
## Introduction

If your applications are Spring Cloud applications and you need to use Alibaba Cloud's OSS service for file storage (for example, storing commodity image for your e-commerce business), you can use OSS starter. This topic provides an example to illustrate how to use OSS starter to implement object storage for Spring Cloud applications.

[Alibaba Cloud Object Storage Service (OSS)](https://www.alibabacloud.com/product/oss) is an encrypted, secure, cost-effective, and easy-to-use object storage service that enables you to store, back up, and archive large amounts of data in the cloud.


## Demo

### Connect to OSS

Before we start the demo, let's learn how to connect OSS to a Spring Cloud application.
**Note: This section is to show you how to connect to oss. The actual configurations have been completed in the following example, and you only need to specify your accessKeyId, secretAccessKey and region.**

1. Add dependency spring-cloud-starter-alicloud-oss in the pom.xml file in your Spring Cloud project.

	    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-alicloud-oss</artifactId>
        </dependency>

2. Configure accessKeyId, secretAccessKey and region in application.properties.

		// application.properties
		spring.cloud.alibaba.oss.accessKeyId=your-ak
		spring.cloud.alibaba.oss.secretAccessKey=your-sk
		spring.cloud.alibaba.oss.region=cn-beijing
		  
    To get accessKeyId, secretAccessKey, follow these steps:

    1. On the Alibaba Cloud console, click your avatar on the upper-right corner and click accesskeys. Or visit [User Management](https://usercenter.console.aliyun.com/) page directly：
		  
       ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535464041257-5c7ae997-daff-45b3-89d4-02d578da4ac7.png) 

    2. Get your accessKeyId、secretAccessKey：

       ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535464098793-517491f6-156b-4a98-a5a4-6113cb3c01a4.png) 
	
	 **Note:** If you are using [STS](https://www.alibabacloud.com/help/doc-detail/28756.html), you should configure securityToken in addition to accessKeyId, secretAccessKey, and region.
	 
3. Inject OSSClient and use it to upload files to the OSS server and download a file from OSS server.

		@Service
		public class YourService {
			@Autowired
			private OSSClient ossClient;

			public void saveFile() {
				// download file to local
				ossClient.getObject(new GetObjectRequest(bucketName, objectName), new File("pathOfYourLocalFile"));
			}
		}

**Note:** Direct injection into the OSSClient mode is typically used for scenarios where you need to handle a large number of file objects. If you only need to read the contents of the file object, OSS Starter also supports reading the file in [Resource mode](#1). 

### Start Application

1. Add necessary configurations to file `/src/main/resources/application.properties`.
	
		spring.application.name=oss-example
		server.port=18084
		spring.cloud.alibaba.oss.accessKeyId=your-ak
		spring.cloud.alibaba.oss.secretAccessKey=your-sk
		spring.cloud.alibaba.oss.region=cn-beijing
		
2. Start the application in IDE or by building a fatjar.

	- Start in IDE: Find main class `OSSApplication`, and execute the main method.
	- Build a fatjar：
	    1. Execute command `mvn clean package` to build a fatjar.
	    2. Run command `java -jar oss-example.jar` to start the application.

After startup, a bucket called 'spring-cloud-alibaba' is automatically created in OSS.

### Upload or download files

#### Upload files
Run `curl` command to upload files. It will upload file 'oss-test.json' in the `classpath` directory. The content of this file is JSON string:

    curl http://localhost:18084/upload
	
Results：
	
	// If configurations are correct, the output will be as follows
	upload success
	// If an error occurs during uploading, the output will be 'upload fail: fail reason'. For example, if accessKeyId is wrong，the output will be
	upload fail: The OSS Access Key Id you provided does not exist in our records. [ErrorCode]: InvalidAccessKeyId [RequestId]: RequestId [HostId]: xxx.oss-cn-beijing.aliyuncs.com [ResponseError]: InvalidAccessKeyId The OSS Access Key Id you provided does not exist in our records. RequestId xxx.oss-cn-beijing.aliyuncs.com xxx-accessKeyId

#### Download files
Use `curl` command to download files. It will download the oss-test.json file that you uploaded just now and print in result):

    curl http://localhost:18084/download
	
Results：
	
	// If configurations are correct, the output will be as follows
	download success, content: { "name": "spring-cloud-alibaba", "github": "https://github.com/spring-cloud-incubator/spring-cloud-alibaba", "authors": ["Jim", "flystar32"], "emails": ["fangjian0423@gmail.com", "flystar32@163.com"] }
	// If an error occurs during downloading, the output will be 'download fail: fail reason'. For example, if accessKeyId is wrong，fail reason will be as follows
	download fail: The OSS Access Key Id you provided does not exist in our records. [ErrorCode]: InvalidAccessKeyId [RequestId]: RequestId [HostId]: xxx.oss-cn-beijing.aliyuncs.com [ResponseError]: InvalidAccessKeyId The OSS Access Key Id you provided does not exist in our records. RequestId sxxx.oss-cn-beijing.aliyuncs.com xxx-accessKeyId
	

### Verify results on OSS

You can verify results on the OSS console when you finish uploading or downloading files.
1. Log on to the [OSS console](https://oss.console.aliyun.com/)，and you will find a bucket named `spring-cloud-alibaba`.

   ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535464204462-ccebb9e0-7233-499c-8dec-8b8348231b2b.png) 

2. Click the `spring-cloud-alibaba` bucket, select the Files tab, and you will find the oss-test file. The file 'oss-test' is located in directory 'custom-dir'. The objectName of the file is 'custom-dir/oss-test'. File directory and file is separated by '/'. 

   ![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535618026281-613a338c-f89c-4c7b-8b04-d404d1320699.png) 
    	

## Endpoint

OSS starter also supports the implmentation of Spring Boot acutator endpoints.

**Prerequisite:**

Add denpendency `spring-boot-starter-actuator` to your pom.xml file, and configure your endpoint security stategy.

- Spring Boot1.x: Add configuration `management.security.enabled=false`
- Spring Boot2.x: Add configuration `management.endpoints.web.exposure.include=*`

To view the endpoint information, visit the following URLs:

Spring Boot1.x: OSS Endpoint URL is http://127.0.0.1:18084/oss.

Spring Boot2.x: OSS Endpoint URL is http://127.0.0.1:18084/acutator/oss.

Endpoint will show the configurations and the list of buckets of all OSSClients.

![undefined](https://cdn.nlark.com/lark/0/2018/png/64647/1535373658171-20674565-6fe1-4e1e-a596-1dd6f4159ec3.png) 

## Multiple OSSClients

If you need multiple OSSClients，like Multi DataSources, build `OSSProperties` first，and then build `OSSClient`. Specify information such as assessKeyId and secrectAccessKey for each OSSClient.
    
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


* OSSClient shutdown：You do not need to shutdown OSSClient. It will be done in `OSSApplicationListener`.

<h2 id="1">Read file using resource mode</h2>

OSS Starter supports getting file objects by `Spring Resource`. Simply configure OSS protocol of the resource：

	  @Value("oss://spring-cloud-alibaba/oss-test")
	  private Resource file;

	  // read file content
	  StreamUtils.copyToString(file.getInputStream(), Charset.forName(CharEncoding.UTF_8))

## Stop OSSClient service
You do not need to manually shut down OSSClient. OSS Starter calls all OSSClient shutdown methods in the `OSSApplicationListener` during ApplicationContext close.  

If you have any feedback or suggestions for Spring Cloud OSS Starter, please don't hesitate to tell us by submitting github issues or via other community channels.

