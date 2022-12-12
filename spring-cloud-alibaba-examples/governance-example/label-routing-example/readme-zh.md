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

### 应用启动

启动一个三个模块的启动类，分别为ConsumerApplication，两个ProviderApplication，将其注入到Nacos注册中心中。

### 效果演示

#### 规则说明
实例中设置的规则如下：

		@GetMapping("/add")
		public void getDataFromControlPlaneTest() {
List<RouteRule> routeRules = new ArrayList<>();
List<MatchService> matchServices = new ArrayList<>();

			UnifiedRouteDataStructure unifiedRouteDataStructure = new UntiedRouteDataStructure();
			unifiedRouteDataStructure.setTargetService("service-provider");

			LabelRouteRule labelRouteData = new LabelRouteRule();
			labelRouteData.setDefaultRouteVersion("v1");

			RouteRule routeRule = new HeaderRule();
			routeRule.setType("header");
			routeRule.setCondition("=");
			routeRule.setKey("tag");
			routeRule.setValue("gray");
			RouteRule routeRule1 = new UrlRule.Parameter();
			routeRule1.setType("parameter");
			routeRule1.setCondition(">");
			routeRule1.setKey("id");
			routeRule1.setValue("10");
			RouteRule routeRule2 = new UrlRule.Path();
			routeRule2.setType("path");
			routeRule2.setCondition("=");
			routeRule2.setValue("/router-test");
			routeRules.add(routeRule);
			routeRules.add(routeRule1);
			routeRules.add(routeRule2);

			MatchService matchService = new MatchService();
			matchService.setVersion("v2");
			matchService.setWeight(100);
			matchService.setRuleList(routeRules);
			matchServices.add(matchService);

			labelRouteData.setMatchRouteList(matchServices);

			unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);

			List<UntiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
			unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
			controlPlaneConnection.pushRouteData(unifiedRouteDataStructureList);
		}
代码对应的规则如下：
若同时满足请求参数中含有tag=gray，请求头中含有id且值小于10，uri为/router-test则流量全部路由到v2版本中，若有一条不满足，则流量路由到v1版本中。

规则也支持动态修改，测试动态修改的规则如下：

		@GetMapping("/add")
		public void getDataFromControlPlaneTest() {
			List<RouteRule> routeRules = new ArrayList<>();
			List<MatchService> matchServices = new ArrayList<>();

			UntiedRouteDataStructure unifiedRouteDataStructure = new UntiedRouteDataStructure();
			unifiedRouteDataStructure.setTargetService("service-provider");

			LabelRouteRule labelRouteData = new LabelRouteRule();
			labelRouteData.setDefaultRouteVersion("v1");

			RouteRule routeRule = new HeaderRule();
			routeRule.setType("header");
			routeRule.setCondition("=");
			routeRule.setKey("tag");
			routeRule.setValue("gray");
			RouteRule routeRule1 = new UrlRule.Parameter();
			routeRule1.setType("parameter");
			routeRule1.setCondition(">");
			routeRule1.setKey("id");
			routeRule1.setValue("10");
			RouteRule routeRule2 = new UrlRule.Path();
			routeRule2.setType("path");
			routeRule2.setCondition("=");
			routeRule2.setValue("/router-test");
			routeRules.add(routeRule);
			routeRules.add(routeRule1);
			routeRules.add(routeRule2);

			MatchService matchService = new MatchService();
			matchService.setVersion("v2");
			matchService.setWeight(50);
			matchService.setRuleList(routeRules);
			matchServices.add(matchService);

			labelRouteData.setMatchRouteList(matchServices);

			unifiedRouteDataStructure.setLabelRouteRule(labelRouteData);

			List<UntiedRouteDataStructure> unifiedRouteDataStructureList = new ArrayList<>();
			unifiedRouteDataStructureList.add(unifiedRouteDataStructure);
			controlPlaneConnection.pushRouteData(unifiedRouteDataStructureList);
		}
代码对应的规则如下：
若同时满足请求参数中含有tag=gray，请求头中含有id且值小于10，uri为/router-test，则50%流量路由到v2版本中，剩下的流量路由到v1版本中，若有一条不满足，则流量路由到v1版本中。

##### 演示步骤
1. 访问 http://localhost:18083/add 将路由规则由控制面接口推入路由规则仓库中。
   访问 http://localhost:18083/router-test 不满足路由规则，路由到v1版本中，v1版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   访问 http://localhost:18083/router-test?id=11 且请求头设置test值为gray 满足路由规则，路由到v2版本中，v2版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v2}, registerEnabled=true, ip='XXX', networkInterface='', port=18081, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}

2. 访问 http://localhost:18083/update 模拟动态修改路由规则。
   访问 http://localhost:18083/router-test 不满足路由规则，路由到v1版本中，v1版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   访问 http://localhost:18083/router-test?id=11 且请求头设置test值为gray 满足路由规则，50%路由到v2版本中，v2版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v2}, registerEnabled=true, ip='XXX', networkInterface='', port=18081, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   50%路由到v1版本中，v1版本实例打印返回如下结果：
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   
3. 如果不推送规则，走正常路由

## 集成Istio
**注意 本章节只是为了便于您理解接入方式，本示例代码中已经完成接入工作，您无需再进行修改。**
1. 首先，修改pom.xml 文件，引入 spring cloud ailbaba governance labelrouting依赖。同时引入Spring Cloud Alibaba的istio-resource-transform模块
```
   <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>spring-cloud-starter-alibaba-governance-labelrouting</artifactId>
   </dependency>
   <dependency>
      <groupId>com.alibaba.cloud</groupId>
      <artifactId>istio-resource-transform</artifactId>
   </dependency>
```
2. 在`src/main/resources/application.yml`配置文件中配置Istio控制面的相关信息
```
server:
  port: 18084
spring:
  main:
    allow-bean-definition-overriding: true
  application:
    name: service-consumer
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
        fail-fast: true
        username: nacos
        password: nacos
    governance:
      auth:
        # 是否开启鉴权
        enabled: ${ISTIO_AUTH_ENABLE:false}
    istio:
      config:
        # 是否开启Istio配置转换
        enabled: ${ISTIO_CONFIG_ENABLE:true}
        # Istiod ip
        host: ${ISTIOD_ADDR:127.0.0.1}
        # Istiod 端口
        port: ${ISTIOD_PORT:15010}
        # 轮询Istio线程池大小
        polling-pool-size: ${POLLING_POOL_SIZE:10}
        # 轮询Istio时间间隔
        polling-time: ${POLLING_TIME:10}
        # Istiod鉴权token(访问Istiod 15012端口时可用)
        istiod-token: ${ISTIOD_TOKEN:}
        # 是否打印xds相关日志
        log-xds: ${LOG_XDS:true}
```
### 应用启动
启动一个三个模块的启动类，分别为IstioConsumerApplication，两个ProviderApplication，将其注入到Nacos注册中心中。

### 下发配置
我们通过Istio控制面下发标签路由规则，首先下发DestinationRule规则
```
kubectl apply -f - << EOF
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: my-destination-rule
spec:
  host: sca-virtual-service
  subsets:
  - name: v1
    labels:
      version: v1
  - name: v2
    labels:
      version: v2
EOF
```
此规则将后端服务拆分为两个版本，label为v1的pod被分到v1版本，label为v2的pod被分到v2版本
之后，我们下发VirtualService规则
```
kubectl apply -f - << EOF
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: sca-virtual-service
spec:
  hosts:
    - service-provider
  http:
  - match:
    - headers:
        tag:
          exact: gray
      uri:
        exact: /istio-label-routing

    route:
    - destination:
        host: service-provider
        subset: v2
  - route:
    - destination:
        host: service-provider
        subset: v1
EOF
```
这条VirtualService指定了一条最简单的标签路由规则，将请求头tag为gray，请求路径为/istio-label-routing的HTTP请求路由到v2版本，其余的流量都路由到v1版本
### 效果演示
我们发送一条不带请求头的HTTP请求至IstioConsumerApplication
```
curl --location --request GET '127.0.0.1:18084/istio-label-routing'
```
因为请求头不为gray，所以请求将会被路由到v1版本，返回如下
```
Route in 30.221.132.228: 18081,version is v1.
```
之后我们发送一条请求头tag为gray，且请求路径为/istio-label-routing的HTTP请求
```
curl --location --request GET '127.0.0.1:18084/istio-label-routing' --header 'tag: gray'
```
因为满足路由规则，所以请求会被路由至v2版本
```
Route in 30.221.132.228: 18081,version is v2.
```
