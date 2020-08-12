package com.alibaba.cloud.dubbospringcloudgatewayadapter.gateway;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GatewayDubboInvocation {

    private String methodName;
    private String headerName;
    private String pathName;
    private String parameterNames;
    private String serviceName;

    public GatewayDubboInvocation(String[] requestInformation, String serviceName){

        methodName = requestInformation[0];
        headerName = requestInformation[1];
        pathName = requestInformation[2];
        parameterNames = requestInformation[3];

        this.serviceName = serviceName;
    }

}
