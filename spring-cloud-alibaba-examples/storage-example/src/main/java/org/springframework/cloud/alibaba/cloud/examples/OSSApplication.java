package org.springframework.cloud.alibaba.cloud.examples;

import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.aliyun.oss.OSS;

/**
 * OSS Application
 *
 * @author <a href="mailto:fangjian0423@gmail.com">Jim</a>
 */
@SpringBootApplication
public class OSSApplication {

	public static final String BUCKET_NAME = "spring-cloud-alibaba";

	public static void main(String[] args) throws URISyntaxException {
		SpringApplication.run(OSSApplication.class, args);
	}

	@Bean
	public AppRunner appRunner() {
		return new AppRunner();
	}

	class AppRunner implements ApplicationRunner {
		@Autowired
		private OSS ossClient;

		@Override
		public void run(ApplicationArguments args) throws Exception {
			try {
				if (!ossClient.doesBucketExist(BUCKET_NAME)) {
					ossClient.createBucket(BUCKET_NAME);
				}
			}
			catch (Exception e) {
				System.err.println("oss handle bucket error: " + e.getMessage());
				System.exit(-1);
			}
		}
	}

}
