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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.alibaba.dubbo.metadata.repository.DubboServiceMetadataRepository;
import org.springframework.cloud.alibaba.dubbo.openfeign.TargeterBeanPostProcessor;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceExecutionContextFactory;
import org.springframework.cloud.alibaba.dubbo.service.DubboGenericServiceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import static org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboOpenFeignAutoConfiguration.TARGETER_CLASS_NAME;


/**
 * Dubbo Feign Auto-{@link Configuration Configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@ConditionalOnClass(name = {"feign.Feign", TARGETER_CLASS_NAME})
@AutoConfigureAfter(name = {"org.springframework.cloud.openfeign.FeignAutoConfiguration"})
@Configuration
public class DubboOpenFeignAutoConfiguration {

    public static final String TARGETER_CLASS_NAME = "org.springframework.cloud.openfeign.Targeter";

    @Bean
    public TargeterBeanPostProcessor targeterBeanPostProcessor(Environment environment,
                                                               DubboServiceMetadataRepository dubboServiceMetadataRepository,
                                                               DubboGenericServiceFactory dubboGenericServiceFactory,
                                                               DubboGenericServiceExecutionContextFactory contextFactory) {
        return new TargeterBeanPostProcessor(environment, dubboServiceMetadataRepository,
                dubboGenericServiceFactory, contextFactory);
    }

}