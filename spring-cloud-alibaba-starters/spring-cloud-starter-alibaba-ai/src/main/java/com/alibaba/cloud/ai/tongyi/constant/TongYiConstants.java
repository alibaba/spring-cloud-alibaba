package com.alibaba.cloud.ai.tongyi.constant;

import com.alibaba.dashscope.aigc.generation.Generation;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

public interface TongYiConstants {

	interface Model {

		/**
		 * Tongyi Thousand Questions mega-language model supports input in different languages such as Chinese and English.
		 */
		String QWEN_TURBO = Generation.Models.QWEN_TURBO;

		/**
		 * Enhanced version of Tongyi Thousand Questions mega-language model to support input in different languages such as Chinese and English.
		 */
		String QWEN_PLUS = Generation.Models.QWEN_PLUS;

		/**
		 * Tongyi Qianqi 100 billion level super large-scale language model, support Chinese, English and other different language input.
		 * As the model is upgraded, qwen-max will be updated and upgraded on a rolling basis. If you wish to use a stable version, please use qwen-max-1201.
		 */
		String QWEN_MAX = Generation.Models.QWEN_MAX;

		/**
		 * Tongyi Qianqi 100 billion level mega-scale language model, supporting different language inputs such as Chinese and English.
		 * The model is a snapshot stabilized version of qwen-max, and is expected to be maintained until one month after the release time of the next snapshot version (TBD).
		 */
		String QWEN_MAX_1021 = "qwen-max-1201";

		/**
		 * Tongyi Thousand Questions is a 100 billion level super large-scale language model that supports different language inputs such as Chinese and English.
		 */
		String QWEN_MAX_LONG_CONTEXT = "qwen-max-longcontext";
	}

	interface Role {

		/**
		 * system: indicates a system-level message that can only be used for the first entry in the conversation history (messages[0]).
		 * The use of the system role is optional and, if present, must be at the beginning of the list
		 */
		String SYSTEM = "system";

		/**
		 * user and assistant: represent the dialog between the user and the model.
		 * They should appear alternately in the dialog to simulate the actual dialog flow.
		 */
		String USER = "user";

		String ASSISTANT = "assistant";

		/**
		 * When using the function_call function, if you want to pass in the result of a function,
		 * this message takes the form {"content": "Boston is raining.", "name": "get_current_weather", "role": "tool"}, where name is the name of the function,
		 * which needs to be consistent with the tool_calls[i].function.name parameter in the previous round's response, and content is the output of the function.
		 * Examples are given for multiple rounds of calls in the reference code.
		 */
		String TOOL = "tool";
	}

}
