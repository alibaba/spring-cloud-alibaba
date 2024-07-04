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

package com.alibaba.cloud.ai.tongyi.images;

import com.alibaba.cloud.ai.tongyi.image.TongYiImagesModel;
import com.alibaba.cloud.ai.tongyi.image.TongYiImagesOptions;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.utils.Constants;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.1.0
 */

class TongYiImagesOptionsTests {

	@Test
	public void testChatOptions() {

		ImageSynthesis mockClient = Mockito.mock(ImageSynthesis.class);
		Constants.apiKey = "test";

		var tongYiImagesClient = new TongYiImagesModel(mockClient,
				TongYiImagesOptions.
						builder()
						.withModel("test")
						.withN(1)
						.withWidth(1000)
						.withHeight(100)
						.build()
		);

		ImageSynthesisParam imageSynthesisParam = tongYiImagesClient.merge(null);

		assertThat(imageSynthesisParam.getModel()).isEqualTo("test");
		assertThat(imageSynthesisParam.getN()).isEqualTo(1);
		assertThat(imageSynthesisParam.getSize()).isEqualTo("100*1000");


		var tongYiImagesOptions = tongYiImagesClient.toTingYiImageOptions(TongYiImagesOptions.builder()
				.withModel(ImageSynthesis.Models.WANX_V1)
				.withN(1000)
				.withHeight(1222)
				.build()
		);

		assertThat(tongYiImagesOptions.getModel()).isEqualTo(ImageSynthesis.Models.WANX_V1);
		assertThat(tongYiImagesOptions.getN()).isEqualTo(1000);
		assertThat(tongYiImagesOptions.getHeight()).isEqualTo(1222);

	}

}
