# label route example

## 项目说明

本项目演示如何使用 spring cloud ailbaba governance labelrouting 模块完成标签路由功能。

## 模块结构

本模块包括一个消费者实例和一个提供者集群，该集群包含着两个实例。

## 示例

### 如何接入

**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**
1. 首先，修改需要进行路由服务的pom.xml 文件，引入 spring cloud ailbaba governance labelrouting依赖。

      <dependency>
          <groupId>com.alibaba.cloud</groupId>
          <artifactId>spring-cloud-starter-alibaba-governance-labelrouting</artifactId>
      </dependency>

2. 其次，在启动类上引入治理模块定义的负载均衡器
   
       @RibbonClient(name = "label-route", configuration = LabelRouteRule.class)

### 应用启动

启动一个三个模块的启动类，分别为ConsumerApplication，两个ProviderApplication，将其注入到Nacos注册中心中。

### 效果演示

#### 规则说明
实例中设置的规则如下：

		@GetMapping("/add")
		public void getDataFromControlPlaneTest() {
			List<RouteRule> routeRules = new ArrayList<>();
			List<MatchService> matchServices = new ArrayList<>();

			UntiedRouteDataStructure untiedRouteDataStructure = new UntiedRouteDataStructure();
			untiedRouteDataStructure.setTargetService("service-provider");

			LabelRouteData labelRouteData = new LabelRouteData();
			labelRouteData.setDefaultRouteVersion("v1");

			RouteRule routeRule = new HeaderRule();
			routeRule.setType("header");
			routeRule.setCondition("=");
			routeRule.setKey("tag");
			routeRule.setValue("gray");
			RouteRule routeRule1 = new UrlRule.Parameter();
			routeRule1.setType("parameter");
			routeRule1.setCondition("=");
			routeRule1.setKey("test");
			routeRule1.setValue("gray");
			routeRules.add(routeRule);
			routeRules.add(routeRule1);

			MatchService matchService = new MatchService();
			matchService.setVersion("v2");
			matchService.setWeight(100);
			matchService.setRuleList(routeRules);
			matchServices.add(matchService);

			labelRouteData.setMatchRouteList(matchServices);

			untiedRouteDataStructure.setLabelRouteData(labelRouteData);

			List<UntiedRouteDataStructure> untiedRouteDataStructureList=new ArrayList<>();
			untiedRouteDataStructureList.add(untiedRouteDataStructure);
			controlPlaneConnection.getDataFromControlPlane(untiedRouteDataStructureList);
		}
代码对应的规则如下：
若同时满足请求参数中含有tag=gray，请求头中含有test且值为gray，则流量全部路由到v2版本中，若有一条不满足，则流量路由到v1版本中。

规则也支持动态修改，测试动态修改的规则如下：

		@GetMapping("/add")
		public void getDataFromControlPlaneTest() {
			List<RouteRule> routeRules = new ArrayList<>();
			List<MatchService> matchServices = new ArrayList<>();

			UntiedRouteDataStructure untiedRouteDataStructure = new UntiedRouteDataStructure();
			untiedRouteDataStructure.setTargetService("service-provider");

			LabelRouteData labelRouteData = new LabelRouteData();
			labelRouteData.setDefaultRouteVersion("v1");

			RouteRule routeRule = new HeaderRule();
			routeRule.setType("header");
			routeRule.setCondition("=");
			routeRule.setKey("tag");
			routeRule.setValue("gray");
			RouteRule routeRule1 = new UrlRule.Parameter();
			routeRule1.setType("parameter");
			routeRule1.setCondition("=");
			routeRule1.setKey("test");
			routeRule1.setValue("gray");
			routeRules.add(routeRule);
			routeRules.add(routeRule1);

			MatchService matchService = new MatchService();
			matchService.setVersion("v2");
			matchService.setWeight(100);
			matchService.setRuleList(routeRules);
			matchServices.add(matchService);

			labelRouteData.setMatchRouteList(matchServices);

			untiedRouteDataStructure.setLabelRouteData(labelRouteData);

			List<UntiedRouteDataStructure> untiedRouteDataStructureList=new ArrayList<>();
			untiedRouteDataStructureList.add(untiedRouteDataStructure);
			controlPlaneConnection.getDataFromControlPlane(untiedRouteDataStructureList);
		}
代码对应的规则如下：
若同时满足请求参数中含有tag=gray，请求头中含有test且值为gray，则50%流量路由到v2版本中，剩下的流量路由到v1版本中，若有一条不满足，则流量路由到v1版本中。

##### 演示步骤
1. 访问 http://localhost:18083/add 将路由规则由控制面接口推入路由规则仓库中。
   访问 http://localhost:18083/router-test 不满足路由规则，路由到v1版本中，v1版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   访问 http://localhost:18083/router-test?tag=gray 且请求头设置test值为gray 满足路由规则，路由到v2版本中，v2版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v2}, registerEnabled=true, ip='XXX', networkInterface='', port=18081, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}

2. 访问 http://localhost:18083/update 模拟动态修改路由规则。
   访问 http://localhost:18083/router-test 不满足路由规则，路由到v1版本中，v1版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   访问 http://localhost:18083/router-test?tag=gray 且请求头设置test值为gray 满足路由规则，50%路由到v2版本中，v2版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v2}, registerEnabled=true, ip='XXX', networkInterface='', port=18081, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   50%路由到v1版本中，v1版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   
