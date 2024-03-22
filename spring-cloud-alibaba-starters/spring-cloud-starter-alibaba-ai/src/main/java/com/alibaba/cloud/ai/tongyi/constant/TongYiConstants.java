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

package com.alibaba.cloud.ai.tongyi.constant;

import com.alibaba.dashscope.aigc.generation.Generation;

/**
 * TongYi models constants.
 *
 * @author yuluo
 * @since 2023.0.0.0
 */

public final class TongYiConstants {

	private TongYiConstants() {
	}

	public static final class Model {

		private Model() {
		}

		/**
		 * Tongyi Thousand Questions mega-language model supports input in different languages such as Chinese and English.
		 */
		public static final String QWEN_TURBO = Generation.Models.QWEN_TURBO;

		/**
		 * Enhanced version of Tongyi Thousand Questions mega-language model to support input in different languages such as Chinese and English.
		 */
		public static final String QWEN_PLUS = Generation.Models.QWEN_PLUS;

		/**
		 * Tongyi Qianqi 100 billion level super large-scale language model, support Chinese, English and other different language input.
		 * As the model is upgraded, qwen-max will be updated and upgraded on a rolling basis. If you wish to use a stable version, please use qwen-max-1201.
		 */
		public static final String QWEN_MAX = Generation.Models.QWEN_MAX;

		/**
		 * Tongyi Qianqi 100 billion level mega-scale language model, supporting different language inputs such as Chinese and English.
		 * The model is a snapshot stabilized version of qwen-max, and is expected to be maintained until one month after the release time of the next snapshot version (TBD).
		 */
		public static final String QWEN_MAX_1021 = "qwen-max-1201";

		/**
		 * Tongyi Thousand Questions is a 100 billion level super large-scale language model that supports different language inputs such as Chinese and English.
		 */
		public static final String QWEN_MAX_LONG_CONTEXT = "qwen-max-longcontext";
	}

	public static final class Role {

		private Role() {
		}

		/**
		 * system: indicates a system-level message that can only be used for the first entry in the conversation history (messages[0]).
		 * The use of the system role is optional and, if present, must be at the beginning of the list
		 */
		public static final String SYSTEM = "system";

		/**
		 * user and assistant: represent the dialog between the user and the model.
		 * They should appear alternately in the dialog to simulate the actual dialog flow.
		 */
		public static final String USER = "user";

		/**
		 * user and assistant: represent the dialog between the user and the model.
		 * They should appear alternately in the dialog to simulate the actual dialog flow.
		 */
		public static final String ASSISTANT = "assistant";

		/**
		 * When using the function_call function, if you want to pass in the result of a function,
		 * this message takes the form {"content": "Boston is raining.", "name": "get_current_weather", "role": "tool"}, where name is the name of the function,
		 * which needs to be consistent with the tool_calls[i].function.name parameter in the previous round's response, and content is the output of the function.
		 * Examples are given for multiple rounds of calls in the reference code.
		 */
		public static final String TOOL = "tool";

	}

}
