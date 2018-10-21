package org.springframework.cloud.alibaba.cloud.examples;

import com.alibaba.csp.sentinel.datasource.Converter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.alibaba.sentinel.annotation.SentinelProtect;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * @author xiaojing
 */
@SpringBootApplication
public class ServiceApplication {

	@Bean
	@SentinelProtect(blockHandler = "handleException", blockHandlerClass = ExceptionUtil.class)
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public RestTemplate restTemplate2() {
		return new RestTemplate();
	}

	@Bean
    public Converter myParser() {
	    return new JsonFlowRuleListParser();
    }

	public static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

}
