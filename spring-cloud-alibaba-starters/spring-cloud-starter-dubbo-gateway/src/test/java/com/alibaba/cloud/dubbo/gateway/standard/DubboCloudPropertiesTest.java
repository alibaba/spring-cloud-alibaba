package com.alibaba.cloud.dubbo.gateway.standard;

import com.alibaba.cloud.dubbo.gateway.DubboCloudGatewayProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class DubboCloudPropertiesTest {


    DubboCloudGatewayProperties testProperties = new DubboCloudGatewayProperties();

    @Test
    public void testGetDubboProtocols(){

        String test = testProperties.getDubboProtocols("protocol");

        assertEquals("dubbo",test);
        assertEquals("failover",testProperties.getDubboProtocols("cluster"));
    }
}
