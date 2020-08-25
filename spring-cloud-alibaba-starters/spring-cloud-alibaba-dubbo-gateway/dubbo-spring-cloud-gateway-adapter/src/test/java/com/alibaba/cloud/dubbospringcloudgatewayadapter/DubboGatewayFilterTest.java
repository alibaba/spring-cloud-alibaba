package com.alibaba.cloud.dubbospringcloudgatewayadapter;


import com.alibaba.cloud.dubbo.metadata.RequestMetadata;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;


import java.util.*;


@RunWith(MockitoJUnitRunner.class)
public class DubboGatewayFilterTest {

     @Mock
     ServerHttpRequest request;

     @Mock
     RequestPath requestPath;

     @Mock
     PathContainer pathContainer;


    DubboGatewayFilter dubboGatewayFilter = new DubboGatewayFilter();

    @Test
    public void testResolveServiceName(){

        Mockito.when(request.getPath()).thenReturn(requestPath);
        Mockito.when(requestPath.value()).thenReturn("/g/app-name/rest-path");
        Mockito.when(requestPath.contextPath()).thenReturn(pathContainer);
        Mockito.when(pathContainer.value()).thenReturn("/g");

        assertEquals("app-name",dubboGatewayFilter.resolveServiceName(request));
    }

    @Test
    public void testBuildRequestMetadata(){

        RequestMetadata requestMetadata = new RequestMetadata();

        Map<String,List<String>> parameters = new HashMap<String, List<String>>();
        List<String> temp = new ArrayList<>();

        temp.add("anyName");
        parameters.put("param",temp);

        requestMetadata.setPath("/param?param=anyName");
        requestMetadata.setMethod("GET");
        requestMetadata.setParams(parameters);

        parameters.clear();
        temp.clear();

        temp.add("localhost");
        parameters.put("Host",temp);

        requestMetadata.setHeaders(parameters);
    }

    /*public void genericInvocationTest(RequestMetadata testClientMetadata){


        testRepository.initializeMetadata("spring-cloud-alibaba-dubbo-provider");

    }*/
}
