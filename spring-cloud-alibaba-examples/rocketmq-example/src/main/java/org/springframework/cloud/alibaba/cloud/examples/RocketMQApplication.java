package org.springframework.cloud.alibaba.cloud.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.alibaba.cloud.examples.RocketMQApplication.MySink;
import org.springframework.cloud.alibaba.cloud.examples.RocketMQApplication.MySource;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
@EnableBinding({ MySource.class, MySink.class })
public class RocketMQApplication {

	public interface MySink {

		@Input("input1")
		SubscribableChannel input1();

		@Input("input2")
		SubscribableChannel input2();

		@Input("input3")
		SubscribableChannel input3();

		@Input("input4")
		SubscribableChannel input4();
	}

	public interface MySource {
		@Output("output1")
		MessageChannel output1();

		@Output("output2")
		MessageChannel output2();
	}

	public static void main(String[] args) {
		SpringApplication.run(RocketMQApplication.class, args);
	}

	@Bean
	public CustomRunner customRunner() {
		return new CustomRunner();
	}

	@Bean
	public CustomRunnerWithTransactional customRunnerWithTransactional() {
		return new CustomRunnerWithTransactional();
	}

	public static class CustomRunner implements CommandLineRunner {
		@Autowired
		private SenderService senderService;

		@Override
		public void run(String... args) throws Exception {
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
	}

	public static class CustomRunnerWithTransactional implements CommandLineRunner {
		@Autowired
		private SenderService senderService;

		@Override
		public void run(String... args) throws Exception {
			// COMMIT_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg1", false);
			// ROLLBACK_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg2", true);
            // ROLLBACK_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg3", true);
            // COMMIT_MESSAGE message
			senderService.sendTransactionalMsg("transactional-msg4", false);
		}
	}

}
