package com.alibaba.cloud.dubbodemospringcloudgateway;

import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContext;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import com.alibaba.cloud.dubbospringcloudgatewayadapter.DubboGatewayFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class DubboDemoSpringCloudGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(DubboDemoSpringCloudGatewayApplication.class, args);

		/*ApplicationContext applicationContext = SpringApplication.run(DubboDemoSpringCloudGatewayApplication.class, args);
		for(GatewayFilter f : applicationContext.getBeanDefinitionNames())
		applicationContext.getBeanDefinitionNames(); */

		/*ApplicationContext ctx = new App
		ctx.register(GatewayClass.class);
		ctx.refresh();
		ctx.getBean(GlobalFilter.class);
		ctx.getBean(RouteLocator.class); */

		/*AnnotationConfigReactiveWebServerApplicationContext s = new AnnotationConfigReactiveWebServerApplicationContext();
		s.register(DubboServiceMetadataRepository.class);
		s.register(DubboGenericServiceFactory.class);
		s.register(DubboGenericServiceExecutionContextFactory.class);
		s.refresh();
		s.getBean(DubboServiceMetadataRepository.class);
		s.getBean(DubboGenericServiceFactory.class);
		s.getBean(DubboGenericServiceExecutionContextFactory.class);
		*/
	}


}
