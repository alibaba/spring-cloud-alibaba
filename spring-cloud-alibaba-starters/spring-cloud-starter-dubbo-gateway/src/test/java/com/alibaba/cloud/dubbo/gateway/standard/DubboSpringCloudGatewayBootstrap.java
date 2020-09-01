/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.cloud.dubbo.gateway.standard;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * The bootstrap class of Dubbo Spring Cloud Gateway
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@SpringBootApplication
@EnableDiscoveryClient
public class DubboSpringCloudGatewayBootstrap {

	public static void main(String[] args) {
		new SpringApplicationBuilder(DubboSpringCloudGatewayBootstrap.class)
				.properties("spring.profiles.active=zookeeper").run(args);
	}

}
