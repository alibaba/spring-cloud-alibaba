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

package com.alibaba.cloud.ai.example.tongyi.service.impl.images;

import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;

import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * gen images example.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0
 */

@Service
public class TongYiImagesServiceImpl extends AbstractTongYiServiceImpl {

	private final ImageModel imageModel;

	@Autowired
	public TongYiImagesServiceImpl(ImageModel imageModel) {

		this.imageModel = imageModel;
	}

	@Override
	public ImageResponse genImg(String imgPrompt) {

		var prompt = new ImagePrompt(imgPrompt);
		return imageModel.call(prompt);
	}

}
