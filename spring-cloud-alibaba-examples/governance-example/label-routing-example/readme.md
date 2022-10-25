# label route example

## Project Description

This project demonstrates how to use the spring cloud ailbaba governance labelrouting module to complete the label routing function.

## module structure

This module includes a consumer instance and a provider cluster, which contains two instances.

## Example

### How to access

**Note that this section is only for your convenience in understanding the access method. The access work has been completed in this sample code, and you do not need to modify it.**
1. First, modify the pom XML file, which introduces the spring cloud ailbaba governance labelrouting dependency.

      <dependency>
          <groupId>com.alibaba.cloud</groupId>
          <artifactId>spring-cloud-starter-alibaba-governance-labelrouting</artifactId>
      </dependency>

2. Secondly, introduce the load balancer defined by the governance module on the startup class.
   
       @RibbonClient(name = "label-route", configuration = LabelRouteRule.class)

### Application Start

Start a startup class of three modules, ConsumerApplication and two ProviderApplications, and inject them into the Nacos registry.

### Effect demonstration

#### Rule Description
The rules set in the instance are as follows:

		@GetMapping("/add")
		public void getDataFromControlPlaneTest() {
List<RouteRule> routeRules = new ArrayList<>();
List<MatchService> matchServices = new ArrayList<>();

			UntiedRouteDataStructure untiedRouteDataStructure = new UntiedRouteDataStructure();
			untiedRouteDataStructure.setTargetService("service-provider");

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

			untiedRouteDataStructure.setLabelRouteRule(labelRouteData);

			List<UntiedRouteDataStructure> untiedRouteDataStructureList = new ArrayList<>();
			untiedRouteDataStructureList.add(untiedRouteDataStructure);
			controlPlaneConnection.pushRouteData(untiedRouteDataStructureList);
		}
The rules corresponding to the code are as follows:
If the request parameter contains tag=gray and the request header contains id and the value is greater than 10, uri is /router-test at the same time, the traffic is routed to the v2 version. If one of the request parameters does not meet the requirement, the traffic is routed to the v1 version.

Rules also support dynamic modification. The rules for testing dynamic modification are as follows:

		@GetMapping("/add")
		public void getDataFromControlPlaneTest() {
			List<RouteRule> routeRules = new ArrayList<>();
			List<MatchService> matchServices = new ArrayList<>();

			UntiedRouteDataStructure untiedRouteDataStructure = new UntiedRouteDataStructure();
			untiedRouteDataStructure.setTargetService("service-provider");

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

			untiedRouteDataStructure.setLabelRouteRule(labelRouteData);

			List<UntiedRouteDataStructure> untiedRouteDataStructureList = new ArrayList<>();
			untiedRouteDataStructureList.add(untiedRouteDataStructure);
			controlPlaneConnection.pushRouteData(untiedRouteDataStructureList);
		}
The rules corresponding to the code are as follows:
If the request parameter contains tag=gray, and the request header contains id and the value is greater than 10,uri is /router-test, 50% of the traffic is routed to the v2 version, and the rest is routed to the v1 version. If one of the traffic does not meet the requirements, the traffic is routed to the v1 version.

#####  demonstrate Steps
1. visit http://localhost:18083/add Push the routing rules from the control surface interface to the routing rule warehouse
   visit http://localhost:18083/router -The test does not meet the routing rules. When the test is routed to the v1 version, the v1 version instance prints and returns the following results:
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   visit http://localhost:18083/router-test?id=11 and the test value set in the request header is gray, which meets the routing rules. The route is to the v2 version. The v2 version instance prints and returns the following results:
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v2}, registerEnabled=true, ip='XXX', networkInterface='', port=18081, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}

2. visit http://localhost:18083/update Simulate dynamic modification of routing rules.
   visit http://localhost:18083/router  The test does not meet the routing rules. When the test is routed to the v1 version, the v1 version instance prints and returns the following results:
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   visit http://localhost:18083/router-test?id=11 and the test value set in the request header is gray, which meets the routing rules. 50% of the routes are routed to the v2 version. The v2 version instance prints the following results:
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v2}, registerEnabled=true, ip='XXX', networkInterface='', port=18081, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}
   50% of them are routed to the v1 version, and the following results are returned when the v1 version instance is printed:
   NacosRegistration{nacosDiscoveryProperties=NacosDiscoveryProperties{serverAddr='127.0.0.1:8848', endpoint='', namespace='', watchDelay=30000, logName='', service='service-provider', weight=1.0, clusterName='DEFAULT', group='DEFAULT_GROUP', namingLoadCacheAtStart='false', metadata={preserved.register.source=SPRING_CLOUD, version=v1}, registerEnabled=true, ip='XXX', networkInterface='', port=18082, secure=false, accessKey='', secretKey='', heartBeatInterval=null, heartBeatTimeout=null, ipDeleteTimeout=null, failFast=true}}

3. If you don't push rule,it will load balance by common rule you set.

