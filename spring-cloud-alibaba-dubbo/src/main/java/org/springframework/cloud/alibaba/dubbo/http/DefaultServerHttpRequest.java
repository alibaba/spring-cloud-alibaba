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
package org.springframework.cloud.alibaba.dubbo.http;


import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.ServerHttpAsyncRequestControl;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;

/**
 * Default {@link ServerHttpRequest} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class DefaultServerHttpRequest implements ServerHttpRequest {

    private final HttpMethod httpMethod;

    private final URI uri;

    private final HttpHeaders httpHeaders;

    private final HttpInputMessage httpInputMessage;

    public DefaultServerHttpRequest(HttpRequest httpRequest, byte[] body) {
        this.httpMethod = httpRequest.getMethod();
        this.uri = httpRequest.getURI();
        this.httpHeaders = httpRequest.getHeaders();
        this.httpInputMessage = new ByteArrayHttpInputMessage(body);
    }

    @Override
    public InputStream getBody() throws IOException {
        return httpInputMessage.getBody();
    }

    @Override
    public HttpMethod getMethod() {
        return httpMethod;
    }

    // Override method since Spring Framework 5.0
    public String getMethodValue() {
        return httpMethod.name();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public HttpHeaders getHeaders() {
        return httpHeaders;
    }

    @Override
    public Principal getPrincipal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServerHttpAsyncRequestControl getAsyncRequestControl(ServerHttpResponse response) {
        throw new UnsupportedOperationException();
    }
}
