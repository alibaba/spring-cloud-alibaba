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
package org.springframework.cloud.alibaba.dubbo.openfeign;

import feign.Feign;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboOpenFeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Dubbo {@link Configuration} for {@link FeignClient FeignClients}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DubboOpenFeignAutoConfiguration
 * @see org.springframework.cloud.openfeign.FeignContext#setConfigurations(List)
 * @see FeignClientsConfiguration
 */
@Configuration
public class DubboFeignClientsConfiguration {

    @Bean
    public BeanPostProcessor beanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof Feign.Builder) {
                    Feign.Builder builder = (Feign.Builder) bean;
                    Feign feign = builder.build();
                }
                return bean;
            }
        };
    }


}
