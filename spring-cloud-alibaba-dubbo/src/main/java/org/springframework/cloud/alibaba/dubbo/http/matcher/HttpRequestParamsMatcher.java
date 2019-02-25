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
package org.springframework.cloud.alibaba.dubbo.http.matcher;

import org.springframework.http.HttpRequest;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * {@link HttpRequest} parameters {@link HttpRequestMatcher matcher}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class HttpRequestParamsMatcher extends AbstractHttpRequestMatcher {

    private final Set<ParamExpression> expressions;

    /**
     * @param params The pattern of params :
     *               <ul>
     *               <li>name=value</li>
     *               <li>name</li>
     *               </ul>
     */
    public HttpRequestParamsMatcher(String... params) {
        this.expressions = parseExpressions(params);
    }

    @Override
    public boolean match(HttpRequest request) {
        for (ParamExpression paramExpression : expressions) {
            if (paramExpression.match(request)) {
                return true;
            }
        }
        return false;
    }

    private static Set<ParamExpression> parseExpressions(String... params) {
        Set<ParamExpression> expressions = new LinkedHashSet<>();
        for (String param : params) {
            expressions.add(new ParamExpression(param));
        }
        return expressions;
    }

    @Override
    protected Collection<ParamExpression> getContent() {
        return this.expressions;
    }

    @Override
    protected String getToStringInfix() {
        return " && ";
    }
}
