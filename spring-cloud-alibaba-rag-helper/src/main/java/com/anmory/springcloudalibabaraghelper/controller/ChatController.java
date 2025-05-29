package com.anmory.springcloudalibabaraghelper.controller;

import com.anmory.springcloudalibabaraghelper.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-29 下午10:19
 */

@RestController
@RequestMapping("/ai")
public class ChatController {
    @Autowired
    AiService aiService;

    @RequestMapping("/chat")
    public String chat(String question) throws IOException {
        return aiService.getReply(question);
    }
}
