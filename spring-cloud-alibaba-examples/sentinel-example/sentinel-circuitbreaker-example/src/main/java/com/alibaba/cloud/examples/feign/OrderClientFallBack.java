package com.alibaba.cloud.examples.feign;

import org.springframework.stereotype.Component;

/**
 *
 *
 * @author freeman
 */
@Component
public class OrderClientFallBack implements OrderClient {
    @Override
    public String defaultConfig(boolean ok) {
        return "order fallback";
    }
}
