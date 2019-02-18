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
package org.springframework.cloud.alibaba.dubbo.context;

import org.springframework.cloud.alibaba.dubbo.registry.SpringCloudRegistryFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The Dubbo services will be registered as the specified Spring cloud applications that will not be considered
 * normal ones, but only are used to Dubbo's service discovery even if it is based on Spring Cloud Commons abstraction.
 * However, current application will be registered by other DiscoveryClientAutoConfiguration.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboServiceRegistrationApplicationContextInitializer implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        // Set ApplicationContext into SpringCloudRegistryFactory before Dubbo Service Register
        SpringCloudRegistryFactory.setApplicationContext(applicationContext);
    }
}
