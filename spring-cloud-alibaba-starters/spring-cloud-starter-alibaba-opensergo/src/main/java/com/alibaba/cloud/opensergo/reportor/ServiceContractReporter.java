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

package com.alibaba.cloud.opensergo.reportor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.alibaba.cloud.opensergo.description.HandlerMappingDescriptionProvider;
import com.alibaba.cloud.opensergo.description.RequestMappingInfoHandlerMappingDescriptionProvider;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opensergo.proto.service_contract.v1.MetadataServiceGrpc;
import io.opensergo.proto.service_contract.v1.ReportMetadataRequest;
import io.opensergo.proto.service_contract.v1.ServiceContract;
import io.opensergo.proto.service_contract.v1.ServiceDescriptor;
import io.opensergo.proto.service_contract.v1.ServiceMetadata;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.core.StandardWrapper;

import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

public class ServiceContractReporter {
	private MetadataServiceGrpc.MetadataServiceBlockingStub blockingStub;
	private ApplicationContext applicationContext;

	public ServiceContractReporter(String endpoint) {
		ManagedChannel channel = ManagedChannelBuilder.forTarget(endpoint).usePlaintext()
				.build();
		blockingStub = MetadataServiceGrpc.newBlockingStub(channel);
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;

		String appName = applicationContext.getId();
		if (appName == null || appName.isEmpty()) {
			appName = "unknown_app";
		}
		ReportMetadataRequest.Builder reportBuilder = ReportMetadataRequest.newBuilder()
				.setAppName(appName);

		ServiceContract.Builder contractBuilder = ServiceContract.newBuilder();
		ServiceDescriptor.Builder descBuilder = ServiceDescriptor.newBuilder()
				.setName(appName);

		if (applicationContext instanceof WebApplicationContext) {
			determineDispatcherServlets((WebApplicationContext) applicationContext)
					.forEach((name, dispatcherServlet) -> {
						processHandlerMappings(dispatcherServlet, name, descBuilder);
					});
		}
		contractBuilder.addServices(descBuilder.build());
		ServiceMetadata.Builder metadataBuilder = ServiceMetadata.newBuilder();
		metadataBuilder.setServiceContract(contractBuilder.build()).addProtocols("http");
		reportBuilder.addServiceMetadata(metadataBuilder.build());

		blockingStub.reportMetadata(reportBuilder.build());
	}

	private Map<String, DispatcherServlet> determineDispatcherServlets(
			WebApplicationContext context) {
		Map<String, DispatcherServlet> dispatcherServlets = new LinkedHashMap<>();
		context.getBeansOfType(ServletRegistrationBean.class).values()
				.forEach((registration) -> {
					Servlet servlet = registration.getServlet();
					if (servlet instanceof DispatcherServlet
							&& !dispatcherServlets.containsValue(servlet)) {
						dispatcherServlets.put(registration.getServletName(),
								(DispatcherServlet) servlet);
					}
				});
		context.getBeansOfType(DispatcherServlet.class)
				.forEach((name, dispatcherServlet) -> {
					if (!dispatcherServlets.containsValue(dispatcherServlet)) {
						dispatcherServlets.put(name, dispatcherServlet);
					}
				});
		return dispatcherServlets;
	}

	void processHandlerMappings(DispatcherServlet dispatcherServlet, String name,
			ServiceDescriptor.Builder descBuilder) {
		List<HandlerMapping> handlerMappings = dispatcherServlet.getHandlerMappings();
		HandlerMappingDescriptionProvider<RequestMappingInfoHandlerMapping> descriptionProvider = new RequestMappingInfoHandlerMappingDescriptionProvider();

		if (handlerMappings == null) {
			initializeDispatcherServletIfPossible(name);
			handlerMappings = dispatcherServlet.getHandlerMappings();
			for (HandlerMapping handlerMapping : handlerMappings) {
				if (descriptionProvider.getMappingClass().isInstance(handlerMapping)) {
					descriptionProvider.process(
							(RequestMappingInfoHandlerMapping) handlerMapping,
							descBuilder);
				}
			}
		}
	}

	private void initializeDispatcherServletIfPossible(String name) {
		if (!(this.applicationContext instanceof ServletWebServerApplicationContext)) {
			return;
		}
		WebServer webServer = ((ServletWebServerApplicationContext) this.applicationContext)
				.getWebServer();
		if (webServer instanceof UndertowServletWebServer) {
			// UndertowServletInitializer is not supported yet.
			// new UndertowServletInitializer((UndertowServletWebServer)
			// webServer).initializeServlet(this.name);
		}
		else if (webServer instanceof TomcatWebServer) {
			new TomcatServletInitializer((TomcatWebServer) webServer)
					.initializeServlet(name);
		}
	}

	private static final class TomcatServletInitializer {

		private final TomcatWebServer webServer;

		private TomcatServletInitializer(TomcatWebServer webServer) {
			this.webServer = webServer;
		}

		void initializeServlet(String name) {
			findContext().ifPresent((context) -> initializeServlet(context, name));
		}

		private Optional<Context> findContext() {
			return Stream.of(this.webServer.getTomcat().getHost().findChildren())
					.filter(Context.class::isInstance).map(Context.class::cast)
					.findFirst();
		}

		private void initializeServlet(Context context, String name) {
			Container child = context.findChild(name);
			if (child instanceof StandardWrapper) {
				try {
					StandardWrapper wrapper = (StandardWrapper) child;
					wrapper.deallocate(wrapper.allocate());
				}
				catch (ServletException ex) {
					// Continue
				}
			}
		}
	}
}
