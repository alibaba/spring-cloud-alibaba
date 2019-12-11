/*
 * Copyright 2013-2018 the original author or authors.
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

package com.alibaba.cloud.dubbo.http.matcher;

/**
 * A contract for {@code "name!=value"} style expression used to specify request
 * parameters and request header in HTTP request
 * <p>
 * The some source code is scratched from
 * org.springframework.web.servlet.mvc.condition.NameValueExpression.
 *
 * @param <T> the value type
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
interface NameValueExpression<T> {

	String getName();

	T getValue();

	boolean isNegated();

}
