package com.anmory.springcloudalibabaraghelper.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author Anmory
 * @description TODO
 * @date 2025-05-29 下午10:11
 */

@Slf4j
@Service
public class AiService {
    @Autowired
    RagService ragService;
    public String getReply(String question) throws IOException {
        log.info("调用的是千问大模型，不要钱");
        // 创建 OpenAI 客户端，连接 DashScope 的兼容接口
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))  // 或替换为 .apiKey("sk-xxx")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();

        String result = ragService.query(question);
        log.info("获取rag增强成功");
        System.out.println(result);
        String prompt = "你需要根据用户发送的question和从url向量数据库获取的result中提取有关信息," +
                "并根据这些信息组织语言进行回复，你能参考的信息有从result中的信息，只能参考里面的信息，" +
                "需要根据这些信息进行条理化地输出，并且符合用户提的问题：" + question +
                ",没有特殊情况不允许自己编造回复，你能参考的资料是：" + result;
        // 创建 ChatCompletion 参数
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("qwen-plus")  // 指定模型
                .addSystemMessage(prompt)
                .addUserMessage(question)
                .build();

        // 发送请求并获取响应
        ChatCompletion chatCompletion = client.chat().completions().create(params);


        // 提取并打印 content 字段内容
        List<ChatCompletion.Choice> choices = chatCompletion.choices();
        String content = choices.get(0).message().content().orElse("无响应内容");
        return content;
    }
}
