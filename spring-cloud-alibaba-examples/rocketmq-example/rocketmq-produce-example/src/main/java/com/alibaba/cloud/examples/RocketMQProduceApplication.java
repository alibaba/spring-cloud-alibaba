package com.alibaba.cloud.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;

import com.alibaba.cloud.examples.RocketMQProduceApplication.MySource;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
@EnableBinding({ MySource.class })
public class RocketMQProduceApplication {

	public interface MySource {
		@Output("output1")
		MessageChannel output1();

		@Output("output2")
		MessageChannel output2();

		@Output("output3")
		MessageChannel output3();
	}

	public static void main(String[] args) {
		SpringApplication.run(RocketMQProduceApplication.class, args);
	}

	@Bean
	public CustomRunner customRunner() {
		return new CustomRunner("output1");
	}

	@Bean
	public CustomRunner customRunner2() {
		return new CustomRunner("output3");
	}

	@Bean
	public CustomRunnerWithTransactional customRunnerWithTransactional() {
		return new CustomRunnerWithTransactional();
	}

	public static class CustomRunner implements CommandLineRunner {

		private final String bindingName;

		public CustomRunner(String bindingName) {
			this.bindingName = bindingName;
		}

		@Autowired
		private SenderService senderService;

		@Autowired
		private MySource mySource;

		@Override
		public void run(String... args) throws Exception {
			if (this.bindingName.equals("output1")) {
				int count = 5;
				for (int index = 1; index <= count; index++) {
					String msgContent = "msg-" + index;
					if (index % 3 == 0) {
						senderService.send(msgContent);
					}
					else if (index % 3 == 1) {
						senderService.sendWithTags(msgContent, "tagStr");
					}
					else {
						senderService.sendObject(new Foo(index, "foo"), "tagObj");
					}
				}
			}
			else if (this.bindingName.equals("output3")) {
				int count = 20;
				for (int index = 1; index <= count; index++) {
					String msgContent = "pullMsg-" + index;
					mySource.output3()
							.send(MessageBuilder.withPayload(msgContent).build());
				}
			}

		}
	}

	public static class CustomRunnerWithTransactional implements CommandLineRunner {
		@Autowired
		private SenderService senderService;

		@Override
		public void run(String... args) throws Exception {
			// COMMIT_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg1", 1);
			// ROLLBACK_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg2", 2);
			// ROLLBACK_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg3", 3);
			// COMMIT_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg4", 4);
		}
	}

}
