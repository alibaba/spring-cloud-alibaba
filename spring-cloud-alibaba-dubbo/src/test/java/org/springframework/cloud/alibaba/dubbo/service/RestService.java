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
package org.springframework.cloud.alibaba.dubbo.service;

import java.util.Map;

/**
 * Echo Service
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public interface RestService {

    String param(String message);

    String params(int a, String b);

    User requestBody(Map<String, Object> data);

    Map<String, Object> requestBody(User user);

    String header(String header);

    String form(String form);

    String cookie(String userAgent);
}
