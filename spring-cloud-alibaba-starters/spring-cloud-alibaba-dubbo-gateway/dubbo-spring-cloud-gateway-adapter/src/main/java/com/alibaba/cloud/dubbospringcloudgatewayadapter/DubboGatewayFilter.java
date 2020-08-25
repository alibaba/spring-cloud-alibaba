package com.alibaba.cloud.dubbospringcloudgatewayadapter;

import com.alibaba.cloud.dubbo.http.MutableHttpServerRequest;
import com.alibaba.cloud.dubbo.metadata.DubboRestServiceMetadata;
import com.alibaba.cloud.dubbo.metadata.RequestMetadata;
import com.alibaba.cloud.dubbo.metadata.RestMethodMetadata;
import com.alibaba.cloud.dubbo.metadata.repository.DubboServiceMetadataRepository;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContext;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceExecutionContextFactory;
import com.alibaba.cloud.dubbo.service.DubboGenericServiceFactory;
import com.alibaba.cloud.dubbospringcloudgatewayadapter.gateway.GatewayDubboInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.dubbo.rpc.service.GenericException;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBetween;

public class DubboGatewayFilter implements GlobalFilter, Ordered {

    Log log = LogFactory.getLog(getClass());


    private int orderValue;

    private DubboServiceMetadataRepository repository;

    private DubboGenericServiceFactory serviceFactory;

    private DubboGenericServiceExecutionContextFactory contextFactory;

    private final Map<String, Object> dubboTranslatedAttributes = new HashMap<>();


    public DubboGatewayFilter( DubboServiceMetadataRepository repository,
                               DubboGenericServiceFactory serviceFactory,
                               DubboGenericServiceExecutionContextFactory contextFactory) {
        this.repository = repository;
        this.serviceFactory = serviceFactory;
        this.contextFactory = contextFactory;
        this.dubboTranslatedAttributes.put("protocol", "dubbo");
        this.dubboTranslatedAttributes.put("cluster", "failover");
    }

    public DubboGatewayFilter() {

    }

    @Override
    public int getOrder() {
        return orderValue;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        String serviceName = resolveServiceName(request);

        String restPath = substringAfter(request.getPath().value(), serviceName);

        // 初始化 serviceName 的 REST 请求元数据
        repository.initializeMetadata(serviceName);
        // 将 HttpServletRequest 转化为 RequestMetadata
        RequestMetadata clientMetadata = buildRequestMetadata(request, restPath);

        DubboRestServiceMetadata dubboRestServiceMetadata = repository.get(serviceName,
                clientMetadata);

        if (dubboRestServiceMetadata == null) {
            // if DubboServiceMetadata is not found, executes next
            return chain.filter(exchange);
        }

        RestMethodMetadata dubboRestMethodMetadata = dubboRestServiceMetadata
                .getRestMethodMetadata();

        GenericService genericService = serviceFactory.create(dubboRestServiceMetadata,
                dubboTranslatedAttributes);

        // TODO: Get the Request Body from HttpServletRequest
        byte[] body = getRequestBody(request);

        MutableHttpServerRequest httpServerRequest = new MutableHttpServerRequest(request,
                body);

        DubboGenericServiceExecutionContext context = contextFactory
                .create(dubboRestMethodMetadata, httpServerRequest);

        Object result = null;
        GenericException exception = null;

        try {
            result = genericService.$invoke(context.getMethodName(),
                    context.getParameterTypes(), context.getParameters());

            ServerHttpResponse response = exchange.getResponse();

            log.info("The result is " + result);

            log.info("The incoming response is: " + exchange.getRequest());

//			response.writeAndFlushWith(Publisher.)

        }
        catch (GenericException e) {
            exception = e;
        }

        return Mono.empty();
    }

    public String resolveServiceName(ServerHttpRequest request) {
        // /g/{app-name}/{rest-path}
        String requestURI = request.getPath().value();
        // /g/
        String servletPath = request.getPath().contextPath().value();

        String part = StringUtils.substringAfter(requestURI, servletPath);

        String serviceName = substringBetween(part, "/", "/");

        return serviceName ;
    }

    private byte[] getRequestBody(ServerHttpRequest request) {
        return new byte[0];
    }

    private RequestMetadata buildRequestMetadata(ServerHttpRequest request,
                                                 String restPath) {
        RequestMetadata requestMetadata = new RequestMetadata();
        requestMetadata.setPath(restPath);
        requestMetadata.setMethod(request.getMethod().toString());
        requestMetadata.setParams(request.getQueryParams());
        requestMetadata.setHeaders(request.getHeaders());
        return requestMetadata;
    }

}
