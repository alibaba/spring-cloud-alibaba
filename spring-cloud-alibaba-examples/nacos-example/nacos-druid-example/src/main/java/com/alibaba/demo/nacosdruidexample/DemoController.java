package com.alibaba.demo.nacosdruidexample;

import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

import com.alibaba.demo.nacosdruidexample.dao.JdbcEntityRepository;
import com.alibaba.demo.nacosdruidexample.dao.JpaDemoEntityRepository;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.util.concurrent.TimeUnit.MILLISECONDS;


@RestController("demo")
public class DemoController {

	JpaDemoEntityRepository jpaDemoEntityRepository;

	JdbcEntityRepository jdbcEntityRepository;

	@Value(value = "${testKey:123}")
	private String testKey;

	@GetMapping("/getTestKey")
	public String getTestKey(HttpServletResponse httpServletResponse) {
		return testKey + new Date();
	}

	DemoController(JpaDemoEntityRepository jpaDemoEntityRepository, JdbcEntityRepository jdbcEntityRepository) {
		this.jpaDemoEntityRepository = jpaDemoEntityRepository;
		this.jdbcEntityRepository = jdbcEntityRepository;

		for (int i = 0; i < 10; i++) {
			scheduleTask();
		}
	}

	private void scheduleTask() {
		Executors.newScheduledThreadPool(2).scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				selectTask();
			}
		}, 200, 200, MILLISECONDS);
	}

	private void selectTask() {
		int count = 200;
		int successCount = 0;
		int failCount = 0;
		long start = System.currentTimeMillis();
		while (count > 0) {
			try {
				String nameById = jdbcEntityRepository.findNameById(123l);
				if (nameById != null) {
					successCount++;
				}
			}
			catch (Throwable throwable) {
				failCount++;
			}
			finally {
				count--;
			}
		}

		System.out.println(new Date() + ",SuccessCount:" + successCount + ",failCount:" + failCount + ", cost:" + (System.currentTimeMillis() - start) + " ms");

	}

	private void searchTask() {
		int count = 1000;

		while (count > 0) {
			long start = System.currentTimeMillis();

			try {

				List<String> nameById = jdbcEntityRepository.getByContent(generateRandomString(1));
				if (nameById != null) {
					System.out.println("Success, cost:" + (System.currentTimeMillis() - start) + " ms");

				}
			}
			catch (Throwable throwable) {
				System.out.println("Fail, cost:" + (System.currentTimeMillis() - start) + " ms");

			}
			finally {
				count--;
			}
		}
	}

	private void insertTask() {
		int count = 1000;
		int successCount = 0;
		int failCount = 0;
		long start = System.currentTimeMillis();
		while (count > 0) {
			try {
				jdbcEntityRepository.insertNameById(generateRandomString(5), generateRandomString(100_100));

			}
			catch (Throwable throwable) {
				failCount++;
			}
			finally {
				count--;
			}
		}

		System.out.println(new Date() + ",SuccessCount:" + successCount + ",failCount:" + failCount + ", cost:" + (System.currentTimeMillis() - start) + " ms");

	}

	private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

	public static String generateRandomString(int length) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder(length);

		// 从字符集中随机选择字符并追加到 StringBuilder 中
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(CHARACTERS.length());
			sb.append(CHARACTERS.charAt(index));
		}

		return sb.toString();
	}
}
