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

import com.alibaba.dubbo.rpc.RpcContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

/**
 * Default {@link RestService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@com.alibaba.dubbo.config.annotation.Service(version = "1.0.0", protocol = {"dubbo", "rest"})
@RestController
@Path("/")
public class StandardRestService implements RestService {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    @GetMapping(value = "/param")
    @Path("/param")
    @GET
    public String param(@RequestParam @QueryParam("param") String param) {
        log("/param", param);
        return param;
    }

    @Override
    @PostMapping("/params")
    @Path("/params")
    @POST
    public String params(@RequestParam @QueryParam("a") int a, @RequestParam @QueryParam("b") String b) {
        log("/params", a + b);
        return a + b;
    }

    @Override
    @GetMapping("/header")
    @Path("/header")
    @GET
    public String header(@RequestHeader("h") @HeaderParam("h") String header) {
        return String.valueOf(header);
    }

    @Override
    @PostMapping("/form")
    @Path("/form")
    @POST
    public String form(@RequestParam("f") @FormParam("f") String form) {
        return String.valueOf(form);
    }

    @Override
    @PostMapping(value = "/request/body/map", produces = APPLICATION_JSON_UTF8_VALUE)
    @Path("/request/setBody/map")
    @POST
    @Produces(APPLICATION_JSON_VALUE)
    public User requestBody(@RequestBody Map<String, Object> data) {
        User user = new User();
        user.setId(((Integer) data.get("id")).longValue());
        user.setName((String) data.get("name"));
        user.setAge((Integer) data.get("age"));
        return user;
    }

    @PostMapping(value = "/request/body/user", consumes = APPLICATION_JSON_UTF8_VALUE)
    @Path("/request/setBody/user")
    @POST
    @Override
    @Consumes(APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> requestBody(@RequestBody User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("age", user.getAge());
        return map;
    }

    @Override
    @GetMapping("/cookie")
    @Path("/cookie")
    @GET
    public String cookie(@CookieParam("User-Agent") @CookieValue("User-Agent") String userAgent) {
        return userAgent;
    }

    private void log(String url, Object result) {
        String message = String.format("The client[%s] uses '%s' protocol to call %s : %s",
                RpcContext.getContext().getRemoteHostName(),
                RpcContext.getContext().getUrl() == null ? "N/A" : RpcContext.getContext().getUrl().getProtocol(),
                url,
                result
        );
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }
}
