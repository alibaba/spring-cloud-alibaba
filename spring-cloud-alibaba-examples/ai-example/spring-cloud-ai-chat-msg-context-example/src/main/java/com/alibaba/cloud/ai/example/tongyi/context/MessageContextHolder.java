package com.alibaba.cloud.ai.example.tongyi.context;

import java.util.List;

import org.springframework.ai.chat.messages.Message;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

public interface MessageContextHolder {

	String SCA_SESSION_ID = "SCA_SESSION_ID";

	void addMsg(String sessionId, Message msg);

	void removeMsg(String sessionId);

	List<Message> getMsg(String sessionId);

	default String getSCASessionId() {

		return SCA_SESSION_ID;
	}

}
