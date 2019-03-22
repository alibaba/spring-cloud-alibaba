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
package org.springframework.cloud.alibaba.dubbo.registry.handler;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.util.Objects;

import static java.lang.System.getProperty;
import static org.apache.dubbo.common.Constants.CONSUMERS_CATEGORY;
import static org.apache.dubbo.common.Constants.PROVIDERS_CATEGORY;
import static org.springframework.util.StringUtils.startsWithIgnoreCase;

/**
 * The Standard {@link DubboRegistryServiceIdHandler}
 * <p>
 * The service ID pattern is "${category}:${interface}:${version}:${group}"
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class StandardDubboRegistryServiceIdHandler implements DubboRegistryServiceIdHandler {

    /**
     * The separator for service name that could be changed by the Java Property "dubbo.service.name.separator".
     */
    protected static final String SERVICE_NAME_SEPARATOR = getProperty("dubbo.service.name.separator", ":");

    private final ConfigurableApplicationContext context;

    public StandardDubboRegistryServiceIdHandler(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean supports(String serviceId) {
        return startsWithIgnoreCase(serviceId, PROVIDERS_CATEGORY) ||
                startsWithIgnoreCase(serviceId, CONSUMERS_CATEGORY);
    }

    @Override
    public String createServiceId(URL url) {
        String category = url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
        if (!Objects.equals(category, PROVIDERS_CATEGORY) && !Objects.equals(category, CONSUMERS_CATEGORY)) {
            category = PROVIDERS_CATEGORY;
        }
        return createServiceId(url, category);
    }

    @Override
    public ConfigurableApplicationContext getContext() {
        return context;
    }

    /**
     * This method maybe override by sub-class.
     *
     * @param url      The Dubbo's {@link URL}
     * @param category The category
     * @return
     */
    protected String createServiceId(URL url, String category) {
        StringBuilder serviceNameBuilder = new StringBuilder(category);
        appendIfPresent(serviceNameBuilder, url, Constants.INTERFACE_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.VERSION_KEY);
        appendIfPresent(serviceNameBuilder, url, Constants.GROUP_KEY);
        return serviceNameBuilder.toString();
    }

    private static void appendIfPresent(StringBuilder target, URL url, String parameterName) {
        String parameterValue = url.getParameter(parameterName);
        appendIfPresent(target, parameterValue);
    }

    private static void appendIfPresent(StringBuilder target, String parameterValue) {
        if (StringUtils.hasText(parameterValue)) {
            target.append(SERVICE_NAME_SEPARATOR).append(parameterValue);
        }
    }
}
