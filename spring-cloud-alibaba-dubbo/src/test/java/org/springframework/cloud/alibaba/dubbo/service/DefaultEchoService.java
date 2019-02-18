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

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Default {@link EchoService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
@Service(version = "1.0.0", protocol = {"dubbo", "rest"})
@RestController
@Path("/")
public class DefaultEchoService implements EchoService {

    @Override
    @GetMapping(value = "/echo"
//            consumes = MediaType.APPLICATION_JSON_VALUE,
//            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    @Path("/echo")
    @GET
//    @Consumes("application/json")
//    @Produces("application/json;charset=UTF-8")
    public String echo(@RequestParam @QueryParam("message") String message) {
        return RpcContext.getContext().getUrl() + " [echo] : " + message;
    }

    @Override
    @PostMapping("/plus")
    @Path("/plus")
    @POST
    public String plus(@RequestParam @QueryParam("a") int a, @RequestParam @QueryParam("b") int b) {
        return null;
    }
}
