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

package com.alibaba.cloud.ai.example.tongyi.context;

import java.util.List;

import org.springframework.ai.chat.messages.Message;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public interface MessageContextHolder {

	/**
	 * Th default session id key.
	 * Can use session_id request_id &etc.
	 */
	String SCA_SESSION_ID = "SCA_SESSION_ID";

	void addMsg(String sessionId, Message msg);

	void removeMsg(String sessionId);

	List<Message> getMsg(String sessionId);

	default String getSCASessionId() {

		return SCA_SESSION_ID;
	}

}
