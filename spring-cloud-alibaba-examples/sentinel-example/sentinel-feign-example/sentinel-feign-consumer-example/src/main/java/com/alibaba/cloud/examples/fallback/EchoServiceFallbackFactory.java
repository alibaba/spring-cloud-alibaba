package com.alibaba.cloud.examples.fallback;

import feign.hystrix.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * @author lengleng
 * @date 2019-08-01
 */
@Component
public class EchoServiceFallbackFactory implements FallbackFactory<EchoServiceFallback> {
    @Override
    public EchoServiceFallback create(Throwable throwable) {
        return new EchoServiceFallback(throwable);
    }
}
