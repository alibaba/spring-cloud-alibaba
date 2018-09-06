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

package org.springframework.cloud.alibaba.sentinel.annotation;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;

/**
 * An annotation to inject {@link com.alibaba.csp.sentinel.datasource.DataSource} instance
 * into a Spring Bean. The Properties of DataSource bean get from config files with
 * specific prefix.
 *
 * @author fangjian
 * @see com.alibaba.csp.sentinel.datasource.DataSource
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SentinelDataSource {

    @AliasFor("prefix")
    String value() default "";

    @AliasFor("value")
    String prefix() default "";

    String name() default ""; // spring bean name

}
