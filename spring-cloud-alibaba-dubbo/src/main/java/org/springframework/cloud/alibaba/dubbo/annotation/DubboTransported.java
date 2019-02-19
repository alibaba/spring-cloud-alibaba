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
package org.springframework.cloud.alibaba.dubbo.annotation;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.client.RestTemplate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link DubboTransported @DubboTransported} annotation indicates that the traditional Spring Cloud Service-to-Service call is transported
 * by Dubbo under the hood, there are two main scenarios:
 * <ol>
 * <li>{@link FeignClient @FeignClient} annotated classes:
 * <ul>
 * If {@link DubboTransported @DubboTransported} annotated classes, the invocation of all methods of
 * {@link FeignClient @FeignClient} annotated classes.
 * </ul>
 * <ul>
 * If {@link DubboTransported @DubboTransported} annotated methods of {@link FeignClient @FeignClient} annotated classes.
 * </ul>
 * </li>
 * <li>{@link LoadBalanced @LoadBalanced} {@link RestTemplate} annotated field, method and parameters</li>
 * </ol>
 * <p>
 *
 * @see FeignClient
 * @see LoadBalanced
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Documented
public @interface DubboTransported {

    /**
     * The protocol of Dubbo transport whose value could be used the placeholder "dubbo.transport.protocol"
     *
     * @return the default protocol is "dubbo"
     */
    String protocol() default "${dubbo.transport.protocol:dubbo}";

    /**
     * The cluster of Dubbo transport whose value could be used the placeholder "dubbo.transport.cluster"
     *
     * @return the default cluster is "failover"
     */
    String cluster() default "${dubbo.transport.cluster:failover}";
}
