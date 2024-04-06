package com.alibaba.cloud.ai.example.tongyi.service.impl.output;

import java.util.Map;

import com.alibaba.cloud.ai.example.tongyi.models.ActorsFilms;
import com.alibaba.cloud.ai.example.tongyi.service.AbstractTongYiServiceImpl;
import com.alibaba.cloud.ai.example.tongyi.service.TongYiService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.parser.BeanOutputParser;
import org.springframework.stereotype.Service;

/**
 * The BeanOutputParser generates an OpenAI JSON compliant schema for a JavaBean and provides instructions to use that schema when replying to a request.
 *
 * @author yuluo
 * @author <a href="mailto:yuluo08290126@gmail.com">yuluo</a>
 * @since 2023.0.0.0-RC1
 */

@Slf4j
@Service
public class TongYiOutputParseServiceImpl extends AbstractTongYiServiceImpl {

	private static final Logger logger = LoggerFactory.getLogger(TongYiService.class);

	private final ChatClient chatClient;

	public TongYiOutputParseServiceImpl(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	@Override
	public ActorsFilms genOutputParse(String actor) {

		var outputParser = new BeanOutputParser<>(ActorsFilms.class);

		String format = outputParser.getFormat();
		logger.info("format: " + format);
		String userMessage = """
				Generate the filmography for the actor {actor}.
				{format}
				""";
		PromptTemplate promptTemplate = new PromptTemplate(userMessage, Map.of("actor", actor, "format", format));
		Prompt prompt = promptTemplate.create();
		Generation generation = chatClient.call(prompt).getResult();

		// {@link BeanOutputParser#getFormat}
		// simple solve.
		String content = generation.getOutput().getContent()
				.replace("```json", "")
				.replace("```", "");

		return outputParser.parse(content);
	}
}
