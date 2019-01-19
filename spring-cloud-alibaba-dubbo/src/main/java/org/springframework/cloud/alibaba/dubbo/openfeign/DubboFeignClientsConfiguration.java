/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.alibaba.dubbo.openfeign;

import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboOpenFeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Dubbo {@link Configuration} for {@link FeignClient FeignClients}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DubboOpenFeignAutoConfiguration
 * @see org.springframework.cloud.openfeign.FeignContext#setConfigurations(List)
 * @see FeignClientsConfiguration
 */
@Configuration
public class DubboFeignClientsConfiguration {

    @Bean
    public BeanPostProcessor beanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof Feign.Builder) {
                    Feign.Builder builder = (Feign.Builder) bean;
                    BuilderWrapper wrapper = new BuilderWrapper(builder);
                    return wrapper;
                }
                return bean;
            }
        };
    }

    private static class BuilderWrapper extends Feign.Builder {

        private final Feign.Builder delegate;

        private BuilderWrapper(Feign.Builder delegate) {
            this.delegate = delegate;
        }

        @Override
        public Feign.Builder logLevel(Logger.Level logLevel) {
            return delegate.logLevel(logLevel);
        }

        @Override
        public Feign.Builder contract(Contract contract) {
            delegate.contract(contract);
            return this;
        }

        @Override
        public Feign.Builder client(Client client) {
            delegate.client(client);
            return this;
        }

        @Override
        public Feign.Builder retryer(Retryer retryer) {
            delegate.retryer(retryer);
            return this;
        }

        @Override
        public Feign.Builder logger(Logger logger) {
            delegate.logger(logger);
            return this;
        }

        @Override
        public Feign.Builder encoder(Encoder encoder) {
            delegate.encoder(encoder);
            return this;
        }

        @Override
        public Feign.Builder decoder(Decoder decoder) {
            delegate.decoder(decoder);
            return this;
        }

        @Override
        public Feign.Builder queryMapEncoder(QueryMapEncoder queryMapEncoder) {
            delegate.queryMapEncoder(queryMapEncoder);
            return this;
        }

        @Override
        public Feign.Builder mapAndDecode(ResponseMapper mapper, Decoder decoder) {
            delegate.mapAndDecode(mapper, decoder);
            return this;
        }

        @Override
        public Feign.Builder decode404() {
            delegate.decode404();
            return this;
        }

        @Override
        public Feign.Builder errorDecoder(ErrorDecoder errorDecoder) {
            delegate.errorDecoder(errorDecoder);
            return this;
        }

        @Override
        public Feign.Builder options(Request.Options options) {
            delegate.options(options);
            return this;
        }

        @Override
        public Feign.Builder requestInterceptor(RequestInterceptor requestInterceptor) {
            delegate.requestInterceptor(requestInterceptor);
            return this;
        }

        @Override
        public Feign.Builder requestInterceptors(Iterable<RequestInterceptor> requestInterceptors) {
            delegate.requestInterceptors(requestInterceptors);
            return this;
        }

        @Override
        public Feign.Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
            delegate.invocationHandlerFactory(invocationHandlerFactory);
            return this;
        }

        @Override
        public Feign.Builder doNotCloseAfterDecode() {
            delegate.doNotCloseAfterDecode();
            return this;
        }

        @Override
        public <T> T target(Class<T> apiType, String url) {
            return delegate.target(apiType, url);
        }

        @Override
        public <T> T target(Target<T> target) {
            return delegate.target(target);
        }

        @Override
        public Feign build() {
            return delegate.build();
        }
    }


}
