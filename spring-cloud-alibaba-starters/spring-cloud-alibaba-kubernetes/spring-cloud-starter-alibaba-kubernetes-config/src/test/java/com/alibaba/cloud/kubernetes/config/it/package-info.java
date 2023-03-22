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

/**
 * Package `it` is shorthand for integration testing.
 *
 * <p>
 * The tests under this package will detect whether there is a Kubernetes environment, if
 * there is, the tests will be executed, and if not, the tests will be skipped.
 *
 * <p>
 * How to run integration tests:
 * <p>
 * You must have a Kubernetes cluster, and the current-context in ~/.kube/config has
 * access rights to ConfigMap and Secret. Then you can run the following command:
 * <p>
 * ./mvnw clean test -pl com.alibaba.cloud:spring-cloud-starter-alibaba-kubernetes-config
 * -am
 *
 */
package com.alibaba.cloud.kubernetes.config.it;
