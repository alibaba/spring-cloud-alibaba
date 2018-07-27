/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel;

import org.junit.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelAspect;
import org.springframework.cloud.alibaba.sentinel.custom.SentinelCustomAspectAutoConfiguration;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author fangjian
 */
public class SentinelAutoConfigurationTests {

    private WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(
                    AutoConfigurations.of(SentinelCustomAspectAutoConfiguration.class, SentinelWebAutoConfiguration.class))
                    .withPropertyValues("spring.cloud.sentinel.port=8888")
                    .withPropertyValues("spring.cloud.sentinel.filter.order=123")
                    .withPropertyValues("spring.cloud.sentinel.filter.urlPatterns=/*,/test");

    @Test
    public void testSentinelAspect() {
        this.contextRunner.run(context -> assertThat(context).hasSingleBean(SentinelAspect.class));
    }

    @Test
    public void testFilter() {
        this.contextRunner.run(context -> {
            assertThat(context.getBean(
                    "servletRequestListener").getClass() == FilterRegistrationBean.class).isTrue();
        });
    }

    @Test
    public void testProperties() {
        this.contextRunner.run(context -> {
            SentinelProperties sentinelProperties = context.getBean(SentinelProperties.class);
            assertThat(sentinelProperties.getPort()).isEqualTo("8888");
            assertThat(sentinelProperties.getFilter().getUrlPatterns().size()).isEqualTo(2);
            assertThat(sentinelProperties.getFilter().getUrlPatterns().get(0)).isEqualTo("/*");
            assertThat(sentinelProperties.getFilter().getUrlPatterns().get(1)).isEqualTo("/test");
        });
    }


}
