package org.springframework.cloud.alicloud.context.sms;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author pbting
 * @author xiaolongzuo
 */
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
@ConditionalOnClass(name = "com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest")
@ConditionalOnProperty(value = "spring.cloud.alibaba.deshao.enable.sms", matchIfMissing = true)
public class SmsContextAutoConfiguration {

}