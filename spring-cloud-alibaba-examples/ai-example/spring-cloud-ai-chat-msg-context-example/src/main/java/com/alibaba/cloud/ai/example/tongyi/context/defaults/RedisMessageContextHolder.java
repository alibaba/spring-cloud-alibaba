package com.alibaba.cloud.ai.example.tongyi.context.defaults;

import java.util.List;

import com.alibaba.cloud.ai.example.tongyi.context.MessageContextHolder;

import org.springframework.ai.chat.messages.Message;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

//@Component
public class RedisMessageContextHolder implements MessageContextHolder{
	@Override
	public void addMsg(String sessionId, Message msg) {

		System.out.println("RedisMessageContextHolder addMsg");
	}

	@Override
	public void removeMsg(String sessionId) {

		System.out.println("RedisMessageContextHolder removeMsg");
	}

	@Override
	public List<Message> getMsg(String sessionId) {

		System.out.println("RedisMessageContextHolder getMsg");
		return null;
	}

}
