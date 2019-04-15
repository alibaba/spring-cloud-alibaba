package org.springframework.cloud.alibaba.cloud.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.alibaba.cloud.examples.RocketMQConsumerApplication.MySink;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
@EnableBinding({ MySink.class })
public class RocketMQConsumerApplication {

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

	public static void main(String[] args) {
		SpringApplication.run(RocketMQConsumerApplication.class, args);
	}

}
