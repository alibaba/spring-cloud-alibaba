package com.alibaba.cloud.ai.example.tongyi.controller;

import com.alibaba.cloud.ai.example.tongyi.service.ChatMsgService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 */

@RestController
@RequestMapping("/chat")
public class ChatMsgController {

	@Autowired
	private ChatMsgService msgService;

	@GetMapping("/msg")
	public String completion(@RequestParam String message) {

		return msgService.completion(message);
	}

}
