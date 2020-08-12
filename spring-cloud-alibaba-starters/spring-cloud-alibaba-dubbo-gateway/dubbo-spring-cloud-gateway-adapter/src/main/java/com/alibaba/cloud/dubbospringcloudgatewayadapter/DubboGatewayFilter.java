package com.alibaba.cloud.dubbospringcloudgatewayadapter;

import com.alibaba.cloud.dubbospringcloudgatewayadapter.gateway.GatewayDubboInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBetween;

public class DubboGatewayFilter implements GlobalFilter, Ordered {

    private final int orderValue;

   private final ServerWebExchangeRequestParser requestParser = new ServerWebExchangeRequestParser();

    public DubboGatewayFilter(){
        this(Ordered.HIGHEST_PRECEDENCE);
    }

    public DubboGatewayFilter(int orderValue){
        this.orderValue = orderValue;
    }

    Log log = LogFactory.getLog(getClass());

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {


        String gatewayMethod = requestParser.requestMethod(exchange);

        String gatewayHeader = requestParser.requestHeader(exchange,"Host");

        String gatewayPath = requestParser.requestPath(exchange);

        String gatewayParams = requestParser.requestParams(exchange,"name");

        log.info("The method is: " + gatewayMethod);
        log.info("The header is: " + gatewayHeader);
        log.info("The path is: " + gatewayPath);
        log.info("The param value is: " + gatewayParams);

        // [gatewayHeader]/{temp}/{service-name}/{rest-path}
        String temp = substringBetween(gatewayPath,"/","/");

        String[] requestList = new String[4];

        requestList[0] = gatewayMethod;
        requestList[1] = gatewayHeader;
        requestList[2] = gatewayPath;
        requestList[3] = gatewayParams;

        if(temp.equals("placeholder")){

            String serviceExtract = substringAfter(gatewayPath,"placeholder");
            String serviceName = substringBetween(serviceExtract,"/","/");

            if(serviceName == null){
              serviceName = substringBetween(serviceExtract,"/","?");

              if(serviceName == null){
                  serviceName = substringAfter(serviceExtract,"/");
              }
            }

            GatewayDubboInvocation rpcInvocation = new GatewayDubboInvocation(requestList,serviceName);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return orderValue;
    }
}
