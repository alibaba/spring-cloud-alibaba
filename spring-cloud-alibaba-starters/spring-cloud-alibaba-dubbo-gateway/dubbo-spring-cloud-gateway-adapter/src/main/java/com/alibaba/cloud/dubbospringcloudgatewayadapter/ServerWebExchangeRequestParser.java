package com.alibaba.cloud.dubbospringcloudgatewayadapter;

import com.alibaba.cloud.dubbospringcloudgatewayadapter.api.RequestParser;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class ServerWebExchangeRequestParser implements RequestParser<ServerWebExchange> {

    @Override
    public String requestMethod(ServerWebExchange request) {
        return request.getRequest().getMethodValue();
    }

    @Override
    public String requestHeader(ServerWebExchange request, String key) {
        return request.getRequest().getHeaders().getFirst(key);
    }

    @Override
    public String requestPath(ServerWebExchange request) {
        return request.getRequest().getPath().value();
    }

    @Override
    public String requestParams(ServerWebExchange request, String keyName) {
        return request.getRequest().getQueryParams().getFirst(keyName);
    }
}
