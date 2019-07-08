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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link HttpRequest} {@link URI} {@link HttpRequestMatcher matcher}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class HttpRequestPathMatcher extends AbstractHttpRequestMatcher {

    private final Set<String> patterns;

    private final PathMatcher pathMatcher;

    public HttpRequestPathMatcher(String... patterns) {
        this.patterns = Collections.unmodifiableSet(prependLeadingSlash(patterns));
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public boolean match(HttpRequest request) {
        List<String> matches = getMatchingPatterns(request);
        return !matches.isEmpty();
    }

    public List<String> getMatchingPatterns(HttpRequest request) {
        String path = getPath(request);
        List<String> matches = getMatchingPatterns(path);
        return matches;
    }

    public List<String> getMatchingPatterns(String lookupPath) {
        List<String> matches = new ArrayList<>();
        for (String pattern : this.patterns) {
            String match = getMatchingPattern(pattern, lookupPath);
            if (match != null) {
                matches.add(match);
            }
        }
        if (matches.size() > 1) {
            matches.sort(this.pathMatcher.getPatternComparator(lookupPath));
        }
        return matches;
    }

    private String getMatchingPattern(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) {
            return pattern;
        }
        boolean hasSuffix = pattern.indexOf('.') != -1;
        if (!hasSuffix && this.pathMatcher.match(pattern + ".*", lookupPath)) {
            return pattern + ".*";
        }
        if (this.pathMatcher.match(pattern, lookupPath)) {
            return pattern;
        }

        if (!pattern.endsWith("/") && this.pathMatcher.match(pattern + "/", lookupPath)) {
            return pattern + "/";
        }
        return null;
    }

    private String getPath(HttpRequest request) {
        URI uri = request.getURI();
        return uri.getPath();
    }

    private static Set<String> prependLeadingSlash(String[] patterns) {
        Set<String> result = new LinkedHashSet<>(patterns.length);
        for (String pattern : patterns) {
            if (StringUtils.hasLength(pattern) && !pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            result.add(pattern);
        }
        return result;
    }

    @Override
    protected Collection<String> getContent() {
        return this.patterns;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }
}
