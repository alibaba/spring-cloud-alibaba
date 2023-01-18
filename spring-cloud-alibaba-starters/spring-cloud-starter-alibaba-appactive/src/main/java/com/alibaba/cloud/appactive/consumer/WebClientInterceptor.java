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

package com.alibaba.cloud.appactive.consumer;

import com.alibaba.cloud.appactive.common.ModifyHttpServletRequestWrapper;
import com.alibaba.cloud.appactive.common.UriContext;
import com.alibaba.cloud.appactive.constant.AppactiveConstants;
import io.appactive.java.api.base.AppContextClient;
import io.appactive.support.log.LogUtil;
import org.slf4j.Logger;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author yuluo
 */
public class WebClientInterceptor extends HandlerInterceptorAdapter {

    private static final Logger logger = LogUtil.getLogger();

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler
    ) {

        ModifyHttpServletRequestWrapper requestWrapper = new ModifyHttpServletRequestWrapper(request);
        requestWrapper.addHeader(AppactiveConstants.ROUTER_ID_HEADER_KEY, AppContextClient.getRouteId());

        logger.info("ReqResInterceptor uri {} for request {} got cleared",
                UriContext.getUriPath(), AppContextClient.getRouteId());

        UriContext.clearContext();

        return true;
    }
}
