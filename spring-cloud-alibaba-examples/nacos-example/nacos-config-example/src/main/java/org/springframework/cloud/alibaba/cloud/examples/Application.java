package org.springframework.cloud.alibaba.cloud.examples;

import com.alibaba.nacos.api.config.listener.Listener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.alibaba.nacos.NacosConfigProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * @author xiaojing, Jianwei Mao
 */
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@Component
class SampleRunner implements ApplicationRunner {

	@Value("${user.name}")
	String userName;

	@Value("${user.age:25}")
	int userAge;


    @Autowired
    private NacosConfigProperties nacosConfigProperties;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		System.out.println(String.format("Initial username=%s, userAge=%d", userName, userAge));

		nacosConfigProperties.configServiceInstance()
                .addListener("nacos-config-example.properties", "DEFAULT_GROUP", new Listener() {

                    /**
                     * Callback with latest config data.
                     *
                     * For example, config data in Nacos is:
                     *
                     *     user.name=Nacos
                     *     user.age=25
                     *
                     * @param configInfo latest config data for specific dataId in Nacos server
                     */
                    @Override
                    public void receiveConfigInfo(String configInfo) {
                        String [] configLines = configInfo.split("\r\n");
                        Map<String, String> configs = new HashMap<>();
                        for (String c : configLines) {
                            String [] configPair = c.split("=");
                            configs.put(configPair[0], configPair[1]);
                        }

                        System.out.println(String.format("Latest username=%s, userAge=%s",
                                configs.get("user.name"), configs.get("user.age")));
                    }

                    @Override
                    public Executor getExecutor() {
                        return null;
                    }
                });
	}
}

@RestController
@RefreshScope
class SampleController {

	@Value("${user.name}")
	String userName;

	@Value("${user.age:25}")
	int age;

	@RequestMapping("/user")
	public String simple() {
		return "Hello Nacos Config!" + "Hello " + userName + " " + age + "!";
	}
}