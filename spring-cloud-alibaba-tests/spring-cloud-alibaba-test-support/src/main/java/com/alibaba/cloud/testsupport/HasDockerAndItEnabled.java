/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.testsupport;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Disables test execution if Docker is unavailable.
 * <p>
 * We don't want to run integration tests on local machine, but still give a chance to run
 * it.
 * <p>
 * Typically, used for CI and local integration test.
 * <p>
 * Set system property
 * {@link HasDockerAndItEnabledCondition#RUN_INTEGRATION_TESTS_PROPERTY} to 'true'
 * <p>
 * general usage: {@code mvn -Dit.enabled=true test}
 * <p>
 * `it` means integration test
 *
 * @author freeman
 * @since 2021.0.1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ExtendWith(HasDockerAndItEnabledCondition.class)
public @interface HasDockerAndItEnabled {

}
