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

import org.springframework.http.MediaType;

import java.util.List;

/**
 * Parses and matches a single media type expression to a request's 'Accept' header.
 * <p>
 * The source code is scratched from
 * org.springframework.web.servlet.mvc.condition.ProducesRequestCondition.ProduceMediaTypeExpression
 *
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 */
class ProduceMediaTypeExpression extends AbstractMediaTypeExpression {

    ProduceMediaTypeExpression(String expression) {
        super(expression);
    }

    ProduceMediaTypeExpression(MediaType mediaType, boolean negated) {
        super(mediaType, negated);
    }

    public final boolean match(List<MediaType> acceptedMediaTypes) {
        boolean match = matchMediaType(acceptedMediaTypes);
        return (!isNegated() ? match : !match);
    }

    private boolean matchMediaType(List<MediaType> acceptedMediaTypes) {
        for (MediaType acceptedMediaType : acceptedMediaTypes) {
            if (getMediaType().isCompatibleWith(acceptedMediaType)) {
                return true;
            }
        }
        return false;
    }
}
