/*
 * Copyright 2013-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.sentinel.datasource;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.alibaba.cloud.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class FileRefreshableDataSourceFactoryBeanTests {

	@Test
	public void testFile() throws Exception {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(
				TestConfig.class);
		assertThat(annotationConfigApplicationContext.getBean("fileBean")).isNotNull();
		FileRefreshableDataSource fileRefreshableDataSource = annotationConfigApplicationContext
				.getBean("fileBean", FileRefreshableDataSource.class);
		assertThat(((List<FlowRule>) fileRefreshableDataSource.loadConfig()).size())
				.isEqualTo(1);
		FileRefreshableDataSourceFactoryBean factoryBean = annotationConfigApplicationContext
				.getBean("&fileBean", FileRefreshableDataSourceFactoryBean.class);
		assertThat(factoryBean.getBufSize()).isEqualTo(1024);
		assertThat(factoryBean.getCharset()).isEqualTo("utf-8");
		assertThat(factoryBean.getRecommendRefreshMs()).isEqualTo(2000);
		assertThat(factoryBean.getFile()).isNotNull();
		assertThat(factoryBean.getConverter()).isNotNull();
	}

	@Configuration
	public static class TestConfig {

		@Bean
		public FileRefreshableDataSourceFactoryBean fileBean() {
			FileRefreshableDataSourceFactoryBean factoryBean = new FileRefreshableDataSourceFactoryBean();
			factoryBean.setBufSize(1024);
			factoryBean.setCharset("utf-8");
			factoryBean.setRecommendRefreshMs(2000);
			try {
				factoryBean.setFile(ResourceUtils.getFile("classpath:flowrule.json")
						.getAbsolutePath());
			}
			catch (FileNotFoundException e) {
				// ignore
			}
			factoryBean.setConverter(buildConverter());
			return factoryBean;
		}

		private Converter buildConverter() {
			return new Converter<String, List<FlowRule>>() {
				ObjectMapper objectMapper = new ObjectMapper();

				@Override
				public List<FlowRule> convert(String source) {
					try {
						return objectMapper.readValue(source,
								new TypeReference<List<FlowRule>>() {
								});
					}
					catch (IOException e) {
						// ignore
					}
					return null;
				}
			};
		}

	}

}
