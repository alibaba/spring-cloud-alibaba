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
package org.springframework.cloud.alibaba.dubbo.metadata;

import java.util.Objects;

/**
 * Dubbo Service Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DubboServiceMetadata {

    private final ServiceRestMetadata serviceRestMetadata;

    private final RestMethodMetadata restMethodMetadata;

    public DubboServiceMetadata(ServiceRestMetadata serviceRestMetadata, RestMethodMetadata restMethodMetadata) {
        this.serviceRestMetadata = serviceRestMetadata;
        this.restMethodMetadata = restMethodMetadata;
    }

    public ServiceRestMetadata getServiceRestMetadata() {
        return serviceRestMetadata;
    }

    public RestMethodMetadata getRestMethodMetadata() {
        return restMethodMetadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DubboServiceMetadata)) return false;
        DubboServiceMetadata that = (DubboServiceMetadata) o;
        return Objects.equals(serviceRestMetadata, that.serviceRestMetadata) &&
                Objects.equals(restMethodMetadata, that.restMethodMetadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceRestMetadata, restMethodMetadata);
    }
}
