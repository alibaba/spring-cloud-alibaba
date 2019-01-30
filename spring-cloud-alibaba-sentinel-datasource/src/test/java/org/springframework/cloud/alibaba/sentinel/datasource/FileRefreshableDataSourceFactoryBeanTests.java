/*
 * Copyright (C) 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.alibaba.sentinel.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Test;
import org.springframework.cloud.alibaba.sentinel.datasource.factorybean.FileRefreshableDataSourceFactoryBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ResourceUtils;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.FileRefreshableDataSource;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
public class FileRefreshableDataSourceFactoryBeanTests {

	@Test
	public void testFile() throws Exception {
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext(
				TestConfig.class);
		assertNotNull("FileRefreshableDataSourceFactoryBean was not created",
				annotationConfigApplicationContext.getBean("fileBean"));
		FileRefreshableDataSource fileRefreshableDataSource = annotationConfigApplicationContext
				.getBean("fileBean", FileRefreshableDataSource.class);
		assertEquals("FileRefreshableDataSourceFactoryBean flow rule size was wrong", 1,
				((List<FlowRule>) fileRefreshableDataSource.loadConfig()).size());
		FileRefreshableDataSourceFactoryBean factoryBean = annotationConfigApplicationContext
				.getBean("&fileBean", FileRefreshableDataSourceFactoryBean.class);
		assertEquals("FileRefreshableDataSourceFactoryBean buf size was wrong", 1024,
				factoryBean.getBufSize());
		assertEquals("FileRefreshableDataSourceFactoryBean charset was wrong", "utf-8",
				factoryBean.getCharset());
		assertEquals("FileRefreshableDataSourceFactoryBean recommendRefreshMs was wrong",
				2000, factoryBean.getRecommendRefreshMs());
		assertNotNull("FileRefreshableDataSourceFactoryBean file was null",
				factoryBean.getFile());
		assertNotNull("FileRefreshableDataSourceFactoryBean converter was null",
				factoryBean.getConverter());
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
