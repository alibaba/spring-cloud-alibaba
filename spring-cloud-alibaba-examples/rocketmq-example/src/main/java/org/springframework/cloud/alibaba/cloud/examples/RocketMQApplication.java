package org.springframework.cloud.alibaba.cloud.examples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.alibaba.cloud.examples.RocketMQApplication.MySink;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
@EnableBinding({ Source.class, MySink.class })
public class RocketMQApplication {

	public interface MySink {

		@Input("input1")
		SubscribableChannel input1();

		@Input("input2")
		SubscribableChannel input2();

		@Input("input3")
		SubscribableChannel input3();

	}

	public static void main(String[] args) {
		SpringApplication.run(RocketMQApplication.class, args);
	}

	@Bean
	public CustomRunner customRunner() {
		return new CustomRunner();
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

}
