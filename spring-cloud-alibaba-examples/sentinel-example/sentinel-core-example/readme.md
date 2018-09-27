# Sentinel Example
## Project Instruction

This example illustrates how to use Sentinel starter to implement flow control for Spring Cloud applications.

[Sentinel](https://github.com/alibaba/Sentinel) is an open-source project of Alibaba. Sentinel takes "traffic flow" as the breakthrough point, and provides solutions in areas such as flow control, concurrency, circuit breaking, and load protection to protect service stability.


## Demo

### Connect to Sentinel
Before we start the demo, let's learn how to connect Sentinel to a Spring Cloud application.
**Note: This section is to show you how to connect to Sentinel. The configurations have been completed in the following example, so you don't need modify the code any more.**

1. Add dependency spring-cloud-starter-alibaba-sentinel in the pom.xml file in your Spring Cloud project.

	    <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
        </dependency>
	
2. Define Resources

	1. Define HTTP Resources    
		Sentinel starter defines all HTTP URLS as resources by relative paths. If you only want to add flow control for your HTTP services, you do not need to modify your code.  
		
	2. Define Custom Resources   
		If you want to implement flow control or degradation for a specific method, you can add an @SentinelResource annotation to the method, as shown in the code below.
	
			@SentinelResource("resource")
			public String hello() {
				return "Hello";
			}
		  
3. Configure flow control rules 
	
	Sentinel provides two ways to configure flow control rules, init from code or configure by dashboard.

	1. Init rule from code: See the code below for a simple flow rule. See [Sentinel Docs](https://github.com/alibaba/Sentinel/wiki/How-to-Use#define-rules) for more information about flow rules.
	
			List<FlowRule> rules = new ArrayList<FlowRule>();
			FlowRule rule = new FlowRule();
			rule.setResource(str);
			// set limit qps to 10
			rule.setCount(10);
			rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
			rule.setLimitApp("default");
			rules.add(rule);
			FlowRuleManager.loadRules(rules);
  
	2. Config by dashboard: See the following section.

### Start Sentinel Dashboard

1. Install Sentinel dashboard by downloading a fatjar or build from source code.

	1. Download: [Download Sentinel Dashboard](http://edas-public.oss-cn-hangzhou.aliyuncs.com/install_package/demo/sentinel-dashboard.jar) 
	2. Build from source code: Get source code by `git clone git@github.com:alibaba/Sentinel.git` from [Github Sentinel](https://github.com/alibaba/Sentinel) and build your code. See [build reference](https://github.com/alibaba/Sentinel/tree/master/sentinel-dashboard) for details.

2. Start the dashboard by running the `java -jar sentinel-dashboard.jar` command.
	The default port of Sentinel dashboard is 8080. Sentinel dashboard is a Spring Boot project. If you want to use another port, see [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-customizing-embedded-containers).

### Start Application

1. Add necessary configurations to file `/src/main/resources/application.properties`.
	
		spring.application.name=sentinel-example
		server.port=18083
		spring.cloud.sentinel.transport.dashboard=localhost:8080
		
2. Start the application in IDE or by building a fatjar.

	1. Start in IDE: Find main class  `ServiceApplication`, and execute the main method.
	2. Build a fatjar：Execute command `mvn clean package` to build a fatjar，and run command `java -jar sentinel-core-example.jar` to start the application.

### Invoke Service

Execute command `curl http://127.0.0.1:18083/hello`    
Execute command `curl http://127.0.0.1:18083/test`

The screenshot belows shows invoke success:

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532084640137-8f4bc16c-4336-4c1b-9ddd-4582b967717a.png" width="240" heigh='180' ></p>

### Configure Flow Control

1. Open http://localhost:8080 in browser, and you can find a Sentinel-Example Application has been registered to the dashboard. 

	**Note: If you can't find your application in the dashboard, invoke a method that has been defined as a Sentinel Resource，for Sentinel uses lazy load strategy.**
	
<p align="center"><img src="https://cdn.nlark.com/lark/0/2018/png/54319/1532315951819-9ffd959e-0547-4f61-8f06-91374cfe7f21.png" width="1000" heigh='400' ></p>


2. Configure HTTP Resource Flow Rule：Click **流控规则(Flow Rule)** on the left-side navigation pane and **新增流控规则(Create Flow Rule)**. On the Create Flow Rule dialogbox, type the URL relative path in the **资源名(Resource Name)** field , enter **单机阈值(Threshold)** value, then click **新增(OK)**. Here we set threshold to 1 for demonstration purpose.

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532078717483-62ab74cd-e5da-4241-a45d-66166b1bde99.png" width="480" heigh='180' ></p>

3. Configure Custom Resource Flow Rule：Click **流控规则(Flow Rule)** on the left-side navigation pane and **新增流控规则(Create Flow Rule)**. type the value() of @SentinelResource in the **资源名(Resource Name)** field , enter **单机阈值(Threshold)** value, then click **新增(OK)**.Here we set threshold to 1 for demonstration purpose.

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532080384317-2943ce0a-daaf-495d-8afc-79a0248a119a.png" width="480" heigh='180' ></p>

4. Visit the URL in your browser again. When QPS is more than 1, we can see that flow control takes effect.

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532080652178-be119c4a-2a08-4f67-be70-fe5ed9a248a3.png" width="480" heigh='180' ></p>

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532080661437-b84ee161-6c2d-4df2-bdb7-7cf0d5be92fb.png" width="480" heigh='180' ></p>


## Customize Flow Control Logic

1. When a URL resource is blocked by Sentinel, the default logic is return HTTP response "Blocked by Sentinel (flow limiting)".
   
	If you want to customize your flow control logic, see the code below:

		public class CustomUrlBlockHandler implements UrlBlockHandler {
			@Override
			public void blocked(HttpServletRequest httpServletRequest,
			HttpServletResponse httpServletResponse) throws IOException {
				// todo add your logic
			}
		}
		
		WebCallbackManager.setUrlBlockHandler(new CustomUrlBlockHandler());


2. When a custom resource is blocked by Sentinel, the default logic is throw BlockException.
   
    If you want to customize your flow control logic, implement interface `SentinelExceptionHandler`, set @SentinelResource's blockHandler() and blockHandlerClass(). See the code below:
    
        @SentinelResource(value = "resource", blockHandler = "", blockHandlerClass = ExceptionUtil.class)
        public String hello() {
            return "Hello";
        }
        
        // ExceptionUtil.java
        public class ExceptionUtil {
            public static void handleException(BlockException ex) {
                System.out.println("Oops: " + ex.getClass().getCanonicalName());
            }
        }

## Endpoint 

Sentinel starter also supports the implementation of Spring Boot actuator endpoints.

**Prerequisite:**

Add dependency `spring-boot-starter-actuator` to your pom.xml file, and configure your endpoint security strategy.

* Spring Boot1.x: Add configuration `management.security.enabled=false`    
* Spring Boot2.x: Add configuration `management.endpoints.web.exposure.include=*`

To view the endpoint information, visit the following URLS:
* Spring Boot1.x: Sentinel Endpoint URL is http://127.0.0.1:18083/sentinel.
* Spring Boot2.x: Sentinel Endpoint URL is http://127.0.0.1:18083/actuator/sentinel.

<p align="center"><img src="https://cdn.yuque.com/lark/0/2018/png/54319/1532084199224-1a41591d-7a06-4680-be8a-5de319ac635d.png" width="480" heigh='360'></p>

## Metrics
You can view metrics information on Sentinel Dashboard.

To see the metrics, click **实时监控(Real-time Monitoring)** in the left-side navigation pane.    
 
`p_qps` stands for passed requests per second, `b_qps` stands for blocked requests per second.

<p align="center"><img src="https://cdn.nlark.com/lark/0/2018/png/54319/1532313595369-8428cd7d-9eb7-4786-a149-acf0da4a2daf.png" width="480" heigh='180'></p>

## DataSource

Sentinel provide [DataSource](https://github.com/alibaba/Sentinel/blob/master/sentinel-extension/sentinel-datasource-extension/src/main/java/com/alibaba/csp/sentinel/datasource/DataSource.java) to manage dynamic rules.

Sentinel starter integrated 4 DataSources provided by Sentinel. It will be register into Spring Context if you write some configs in `application.properties`.

If you want to define FileRefreshableDataSource:

    spring.cloud.sentinel.datasource.type=file
    spring.cloud.sentinel.datasource.recommendRefreshMs=2000
    spring.cloud.sentinel.datasource.bufSize=2048
    spring.cloud.sentinel.datasource.charset=utf-8
    spring.cloud.sentinel.datasource.configParser=myParser
    spring.cloud.sentinel.datasource.file=/Users/you/rule.json
    
then use `@SentinelDataSource` to annotate DataSource:
 
    @SentinelDataSource("spring.cloud.sentinel.datasource")
    private DataSource dataSource;
    
The value() of `@SentinelDataSource` is not required, it means the prefix of configuration. Default value is `spring.cloud.sentinel.datasource`.

spring.cloud.sentinel.datasource.type means the type of DataSource.

spring.cloud.sentinel.datasource.recommendRefreshMs means the recommendRefreshMs property of specified DataSource.

spring.cloud.sentinel.datasource.configParser means the name of spring bean that type is ConfigParser. If the bean is not exists, will throw exception.
    
Now datasource type support 4 categories: file, nacos, zk, apollo.

### User-defined DataSource

User-defined DataSource need 2 steps.

1. Define DataSource
    
        public class CustomDataSource implements DataSource {
            private String fieldA;
            private String fieldB;
            ...
        }
    
2. Assemble DataSource. There are 2 ways to do this.

    * Construct DataSource directly
        
            @Bean
            public CustomDataSource customDataSource() {
                CustomDataSource customDataSource = new CustomDataSource();
                customDataSource.setFieldA("valueA");
                customDataSource.setFieldB("valueB");
                ...
                return customDataSource;
            }

    * define DataSource metadata in `classpath:/META-INF/sentinel-datasource.properties`
    
            custom = yourpackage.CustomDataSource
       
       define configuration in `application.properties`
       
            spring.cloud.sentinel.datasource.type = custom
            spring.cloud.sentinel.datasource.fieldA = valueA
            spring.cloud.sentinel.datasource.fieldB = valueB
            
Note: The AbstractDataSource of Sentinel need a ConfigParser as a constructor param and the subclass of AbstractDataSource was construct by multi-param constructor.
Now All DataSources in starter was construct by FactoryBean. If you want to do it in this way, you should register FactoryBean by SentinelDataSourceRegistry.

    SentinelDataSourceRegistry.registerFactoryBean("custeom", CustomDataSourceFactoryBean.class);
    
It is no need to using SentinelDataSourceRegistry to register FactoryBean if your User-defined DataSource can inject fields. 
  

## More
For more information about Sentinel, see [Sentinel Project](https://github.com/alibaba/Sentinel).

If you have any ideas or suggestions for Spring Cloud Sentinel starter, please don't hesitate to tell us by submitting github issues.

