package com.alibaba.cloud.stream.binder.rocketmq.support;

import org.springframework.messaging.MessageHeaders;

import java.util.Map;

/**
* header value mapper for RocketMQ
*
* @author caotc
* @date 2019-08-22
* @since 2.1.1
*/
public interface RocketMQHeaderMapper {

    void fromHeaders(Map<String,Object> headers, Map<String,String> target);

    void toHeaders(Map<String,String> source, Map<String,Object> target);
}
