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
package org.springframework.cloud.alibaba.dubbo.registry.netflix.eureka;

import com.netflix.appinfo.HealthCheckHandler;
import com.netflix.discovery.EurekaClientConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.alibaba.dubbo.registry.AbstractRegistrationFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.cloud.netflix.eureka.CloudEurekaInstanceConfig;
import org.springframework.cloud.netflix.eureka.EurekaInstanceConfigBean;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaRegistration;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * {@link EurekaRegistration} Factory
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class EurekaRegistrationFactory extends AbstractRegistrationFactory<EurekaRegistration> {

    @Override
    public EurekaRegistration create(ServiceInstance serviceInstance, ConfigurableApplicationContext applicationContext) {
        CloudEurekaInstanceConfig cloudEurekaInstanceConfig = applicationContext.getBean(CloudEurekaInstanceConfig.class);
        ObjectProvider<HealthCheckHandler> healthCheckHandler = applicationContext.getBeanProvider(HealthCheckHandler.class);
        EurekaClientConfig eurekaClientConfig = applicationContext.getBean(EurekaClientConfig.class);
        InetUtils inetUtils = applicationContext.getBean(InetUtils.class);
        EurekaInstanceConfigBean eurekaInstanceConfigBean = new EurekaInstanceConfigBean(inetUtils);
        BeanUtils.copyProperties(cloudEurekaInstanceConfig, eurekaInstanceConfigBean);
        String serviceId = serviceInstance.getServiceId();
        eurekaInstanceConfigBean.setInstanceId(serviceInstance.getInstanceId());
        eurekaInstanceConfigBean.setVirtualHostName(serviceId);
        eurekaInstanceConfigBean.setSecureVirtualHostName(serviceId);
        eurekaInstanceConfigBean.setAppname(serviceId);
        eurekaInstanceConfigBean.setHostname(serviceInstance.getHost());
        eurekaInstanceConfigBean.setMetadataMap(serviceInstance.getMetadata());

        return EurekaRegistration.builder(eurekaInstanceConfigBean)
                .with(healthCheckHandler)
                .with(eurekaClientConfig, applicationContext)
                .build();
    }
}
