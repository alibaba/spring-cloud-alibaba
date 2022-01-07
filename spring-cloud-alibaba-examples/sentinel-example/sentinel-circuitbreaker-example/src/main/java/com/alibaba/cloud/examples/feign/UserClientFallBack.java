package com.alibaba.cloud.examples.feign;

import org.springframework.stereotype.Component;

/**
 *
 *
 * @author freeman
 */
@Component
public class UserClientFallBack implements UserClient {
    @Override
    public String feignMethod(boolean ok) {
        return "user fallback";
    }

    @Override
    public String feign(boolean ok) {
        return "user fallback";
    }
}
