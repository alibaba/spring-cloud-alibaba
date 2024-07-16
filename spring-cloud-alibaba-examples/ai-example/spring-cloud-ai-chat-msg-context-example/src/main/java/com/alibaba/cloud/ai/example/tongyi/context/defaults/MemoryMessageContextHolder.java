package com.alibaba.cloud.ai.example.tongyi.context.defaults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.alibaba.cloud.ai.example.tongyi.context.MessageContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@Component
public class MemoryMessageContextHolder implements MessageContextHolder {

	private static final Logger log = LoggerFactory.getLogger(MemoryMessageContextHolder.class);

	private final Map<String, List<Message>> msgContextHolderMap = new HashMap<>();

	@Override
	public void addMsg(String sessionId, Message msg) {

		msgContextHolderMap.computeIfAbsent(sessionId, k -> new ArrayList<>());
		log.info("addMsg: sessionId={}, msg={}", sessionId, msg);
	}

	@Override
	public void removeMsg(String sessionId) {

		msgContextHolderMap.remove(sessionId);
	}

	@Override
	public List<Message> getMsg(String sessionId) {

		return new ArrayList<>(msgContextHolderMap.getOrDefault(sessionId, new ArrayList<>()));
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("MessageContextHolderImpl{");
		StringJoiner joiner = new StringJoiner(", ", "{", "}");
		for (Map.Entry<String, List<Message>> entry : msgContextHolderMap.entrySet()) {
			joiner.add(entry.getKey() + "=" + entry.getValue().toString());
		}
		sb.append("msgContextHolderMap=").append(joiner);
		sb.append('}');
		return sb.toString();
	}
}
