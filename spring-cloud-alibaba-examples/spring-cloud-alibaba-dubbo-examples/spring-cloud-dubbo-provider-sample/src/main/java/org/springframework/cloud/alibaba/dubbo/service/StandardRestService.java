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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.util.HashMap;
import java.util.Map;

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
    @GetMapping("/headers")
    @Path("/headers")
    @GET
    public String headers(@RequestHeader("h") @HeaderParam("h") String header,
                          @RequestHeader("h2") @HeaderParam("h2") String header2,
                          @RequestParam("v") @QueryParam("v") Integer param) {
        String result = header + " , " + header2 + " , " + param;
        log("/headers", result);
        return result;
    }

    @Override
    @GetMapping("/path-variables/{p1}/{p2}")
    @Path("/path-variables/{p1}/{p2}")
    @GET
    public String pathVariables(@PathVariable("p1") @PathParam("p1") String path1,
                                @PathVariable("p2") @PathParam("p2") String path2,
                                @RequestParam("v") @QueryParam("v") String param) {
        String result = path1 + " , " + path2 + " , " + param;
        log("/path-variables", result);
        return result;
    }

    // @CookieParam does not support : https://github.com/OpenFeign/feign/issues/913
    // @CookieValue also does not support

    @Override
    @PostMapping("/form")
    @Path("/form")
    @POST
    public String form(@RequestParam("f") @FormParam("f") String form) {
        return String.valueOf(form);
    }

    @Override
    @PostMapping(value = "/request/body/map", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/request/body/map")
    @POST
    @Produces(APPLICATION_JSON_VALUE)
    public User requestBodyMap(@RequestBody Map<String, Object> data, @RequestParam("param") @QueryParam("param") String param) {
        User user = new User();
        user.setId(((Integer) data.get("id")).longValue());
        user.setName((String) data.get("name"));
        user.setAge((Integer) data.get("age"));
        log("/request/body/map", param);
        return user;
    }

    @PostMapping(value = "/request/body/user", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Path("/request/body/user")
    @POST
    @Override
    @Consumes(MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Object> requestBodyUser(@RequestBody User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("name", user.getName());
        map.put("age", user.getAge());
        return map;
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
