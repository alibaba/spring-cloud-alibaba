package com.alibaba.cloud.seata.web.reactive;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yangfengwei
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveSeataHandlerAutoConfiguration {

    @Bean
    public ReactiveSeataHandlerFilter seataHandlerReactiveFilter() {
        return new ReactiveSeataHandlerFilter();
    }

}
