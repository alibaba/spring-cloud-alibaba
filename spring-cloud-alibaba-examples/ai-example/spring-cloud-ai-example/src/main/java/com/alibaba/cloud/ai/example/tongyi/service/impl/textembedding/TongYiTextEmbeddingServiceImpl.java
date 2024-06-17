/*
 * Copyright 2023-2024 the original author or authors.
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

package com.alibaba.cloud.ai.example.tongyi.service.impl.textembedding;

import java.util.List;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

/**
 * @author why_ohh
 * @author <a href="mailto:550588941@qq.com">why_ohh</a>
 */

@Service
public class TongYiTextEmbeddingServiceImpl extends AbstractTongYiServiceImpl {

	private final EmbeddingModel embeddingModel;

	public TongYiTextEmbeddingServiceImpl(EmbeddingModel embeddingModel) {

		this.embeddingModel = embeddingModel;
	}

	@Override
	public List<Double> textEmbedding(String text) {

		return embeddingModel.embed(text);
	}

}
