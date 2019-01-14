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

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Contract;
import feign.jaxrs2.JAXRS2Contract;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.alibaba.dubbo.rest.feign.RestMetadataResolver;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.Path;

/**
 * Spring Boot Auto-Configuration class for Dubbo REST
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Configuration
public class DubboRestAutoConfiguration {

//    /**
//     * A Feign Contract bean for JAX-RS if available
//     */
//    @ConditionalOnClass(Path.class)
//    @Bean
//    public Contract jaxrs2Contract() {
//        return new JAXRS2Contract();
//    }

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

//    /**
//     * A Feign Contract bean for Spring MVC if available
//     */
//    @ConditionalOnClass(RequestMapping.class)
//    @Bean
//    public Contract springMvcContract() {
//        return new SpringMvcContract();
//    }

    @Bean
    public RestMetadataResolver metadataJsonResolver(ObjectMapper objectMapper) {
        return new RestMetadataResolver(objectMapper);
    }
}