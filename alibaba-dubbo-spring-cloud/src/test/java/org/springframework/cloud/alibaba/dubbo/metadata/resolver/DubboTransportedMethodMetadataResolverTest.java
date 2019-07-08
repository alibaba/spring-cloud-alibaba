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
package org.springframework.cloud.alibaba.dubbo.metadata.resolver;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;
import org.springframework.cloud.alibaba.dubbo.metadata.DubboTransportedMethodMetadata;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.mock.env.MockEnvironment;

import java.util.Set;

/**
 * {@link DubboTransportedMethodMetadataResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboTransportedMethodMetadataResolverTest {

    private DubboTransportedMethodMetadataResolver resolver;

    private MockEnvironment environment;

    @Before
    public void init() {
        environment = new MockEnvironment();
        resolver = new DubboTransportedMethodMetadataResolver(environment, new SpringMvcContract());
    }

    @Test
    public void testResolve() {
        Set<DubboTransportedMethodMetadata> metadataSet = resolver.resolveDubboTransportedMethodMetadataSet(TestDefaultService.class);
        Assert.assertEquals(1, metadataSet.size());
    }


    @DubboTransported
    interface TestDefaultService {

        String test(String message);

    }
}
