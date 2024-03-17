package com.alibaba.cloud.ai.example.tongyi.controller;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yuluo
 * @author 1481556636@qq.com
 */

@RestController
@RequestMapping("/ai")
public class TongYiController {

	@Autowired
	private TongYiService tongyiService;

	@GetMapping("/example")
	public Map<String, String> completion(
			@RequestParam(value = "message", defaultValue = "Tell me a joke")
			String message
	) {

		return tongyiService.completion(message);
	}

}
