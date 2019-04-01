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
package org.springframework.cloud.alibaba.dubbo.autoconfigure;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.spring.ServiceBean;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;

import com.ecwid.consul.v1.agent.model.NewService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.alibaba.dubbo.registry.event.ServiceInstancePreRegisteredEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.consul.serviceregistry.ConsulAutoRegistration;
import org.springframework.cloud.consul.serviceregistry.ConsulRegistration;
import org.springframework.cloud.zookeeper.serviceregistry.ServiceInstanceRegistration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.List;

import static org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.CONSUL_AUTO_CONFIGURATION_CLASS_NAME;
import static org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboServiceRegistrationAutoConfiguration.ZOOKEEPER_AUTO_CONFIGURATION_CLASS_NAME;

/**
 * Dubbo Service Registration Auto-{@link Configuration} for Non-Web application
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
@ConditionalOnNotWebApplication
@ConditionalOnProperty(value = "spring.cloud.service-registry.auto-registration.enabled", matchIfMissing = true)
@AutoConfigureAfter(DubboServiceRegistrationAutoConfiguration.class)
@Aspect
public class DubboServiceRegistrationNonWebApplicationAutoConfiguration {

    private static final String REST_PROTOCOL = "rest";

    @Autowired
    private ServiceRegistry serviceRegistry;

    @Autowired
    private Registration registration;

    private volatile Integer webPort = null;

    private volatile boolean registered = false;

    @Around("execution(* org.springframework.cloud.client.serviceregistry.Registration.getPort())")
    public Object getPort(ProceedingJoinPoint pjp) throws Throwable {
        return webPort != null ? webPort : pjp.proceed();
    }

    @EventListener(ServiceBeanExportedEvent.class)
    public void onServiceBeanExported(ServiceBeanExportedEvent event) {
        setWebPort(event.getServiceBean());
        register();
    }

    private void register() {
        if (registered) {
            return;
        }
        serviceRegistry.register(registration);
        registered = true;
    }

    /**
     * Set web port from {@link ServiceBean#getExportedUrls() exported URLs}  if "rest" protocol is present.
     *
     * @param serviceBean {@link ServiceBean}
     */
    private void setWebPort(ServiceBean serviceBean) {
        if (webPort == null) {
            List<URL> urls = serviceBean.getExportedUrls();
            urls.stream()
                    .filter(url -> REST_PROTOCOL.equalsIgnoreCase(url.getProtocol()))
                    .findFirst()
                    .ifPresent(url -> {
                        webPort = url.getPort();
                    });
        }
    }

    @Configuration
    @ConditionalOnBean(name = ZOOKEEPER_AUTO_CONFIGURATION_CLASS_NAME)
    class ZookeeperConfiguration implements SmartInitializingSingleton {

        @Autowired
        private ServiceInstanceRegistration registration;

        @EventListener(ServiceInstancePreRegisteredEvent.class)
        public void onServiceInstancePreRegistered(ServiceInstancePreRegisteredEvent event) {
            registration.setPort(webPort);
        }

        @Override
        public void afterSingletonsInstantiated() {
            // invoke getServiceInstance() method to trigger the ServiceInstance building before register
            registration.getServiceInstance();
        }
    }

    @Configuration
    @ConditionalOnBean(name = CONSUL_AUTO_CONFIGURATION_CLASS_NAME)
    class ConsulConfiguration {

        /**
         * Handle the pre-registered event of {@link ServiceInstance} for Consul
         *
         * @param event {@link ServiceInstancePreRegisteredEvent}
         */
        @EventListener(ServiceInstancePreRegisteredEvent.class)
        public void onServiceInstancePreRegistered(ServiceInstancePreRegisteredEvent event) {
            Registration registration = event.getSource();
            ConsulAutoRegistration consulRegistration = (ConsulAutoRegistration) registration;
            setPort(consulRegistration);
        }

        /**
         * Set port on Non-Web Application
         *
         * @param consulRegistration {@link ConsulRegistration}
         */
        private void setPort(ConsulAutoRegistration consulRegistration) {
            int port = consulRegistration.getPort();
            NewService newService = consulRegistration.getService();
            if (newService.getPort() == null) {
                newService.setPort(port);
            }
        }
    }

}