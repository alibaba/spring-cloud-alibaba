/*
 * Copyright 2013-2023 the original author or authors.
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

package com.alibaba.cloud.example.ai.rag.loader;

import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreProperties;
import org.springframework.ai.reader.JsonReader;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Component
public class RAGDataLoader implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(RAGDataLoader.class);

	private static final String[] KEYS = { "name", "abv", "ibu", "description" };

	@Value("classpath:/data/beers.json.gz")
	private Resource data;

	private final RedisVectorStore vectorStore;

	private final RedisVectorStoreProperties properties;

	public RAGDataLoader(RedisVectorStore vectorStore, RedisVectorStoreProperties properties) {

		this.vectorStore = vectorStore;
		this.properties = properties;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		Map<String, Object> indexInfo = vectorStore.getJedis().ftInfo(properties.getIndex());
		int numDocs = Integer.parseInt((String) indexInfo.getOrDefault("num_docs", "0"));
		if (numDocs > 20000) {
			logger.info("Embeddings already loaded. Skipping");
			return;
		}

		Resource file = data;
		if (Objects.requireNonNull(data.getFilename()).endsWith(".gz")) {
			GZIPInputStream inputStream = new GZIPInputStream(data.getInputStream());
			file = new InputStreamResource(inputStream, "beers.json.gz");
		}

		logger.info("Creating Embeddings...");
		JsonReader loader = new JsonReader(file, KEYS);
		vectorStore.add(loader.get());
		logger.info("Embeddings created.");
	}

}
