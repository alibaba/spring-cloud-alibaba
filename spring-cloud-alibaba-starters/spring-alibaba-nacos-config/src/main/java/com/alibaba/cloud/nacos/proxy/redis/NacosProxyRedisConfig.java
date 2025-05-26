/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.proxy.redis;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.DefaultRedisCredentials;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.RedisCredentials;
import redis.clients.jedis.RedisCredentialsProvider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class NacosProxyRedisConfig {

	private static final String GROUP = "nacos-redis";

	@ConditionalOnProperty(value = "spring.nacos.config.proxy.redis.enabled", havingValue = "true")
	@Bean
	public JedisPool nacosProxyRedis(@Value("${spring.nacos.config.config.proxy.redis.data-id}") String config) throws Exception {

		ConfigService configService = NacosConfigManager.getInstance().getConfigService();
		String redisCredentialsProviderConfig = configService.getConfig(config, GROUP, 3000L);

		NacosProxyRedisCredentialsProvider redisCredentialsProvider = new NacosProxyRedisCredentialsProvider();

		Properties properties = new Properties();
		properties.load(new StringReader(redisCredentialsProviderConfig));
		String username = (String) properties.get("spring.data.redis.username");
		String password = (String) properties.get("spring.data.redis.password");
		String redisHost = (String) properties.get("spring.data.redis.host");
		String redisPort = (String) properties.get("spring.data.redis.port");
		String database = (String) properties.get("spring.data.redis.database");

		RedisCredentials redisCredentials = new DefaultRedisCredentials(username, password);
		redisCredentialsProvider.setRedisCredentials(redisCredentials);

		DefaultJedisClientConfig defaultJedisClientConfig = DefaultJedisClientConfig.builder()
				.database(Integer.parseInt(database))
				.credentialsProvider(redisCredentialsProvider).build();
		GenericObjectPoolConfig<Jedis> poolConfig = new GenericObjectPoolConfig<>();
		poolConfig.setJmxEnabled(false); // 启用 JMX

		final JedisPool jedisPool = new JedisPool(poolConfig, new HostAndPort(redisHost, Integer.parseInt(redisPort)),
				defaultJedisClientConfig);
		configService.addListener(config, GROUP, new AbstractListener() {
			@Override
			public void receiveConfigInfo(String configInfo) {
				Properties properties = new Properties();
				try {
					properties.load(new StringReader(configInfo));
					String username = (String) properties.get("spring.data.redis.username");
					String password = (String) properties.get("spring.data.redis.password");
					RedisCredentials redisCredentials = new DefaultRedisCredentials(username, password);
					redisCredentialsProvider.setRedisCredentials(redisCredentials);
					jedisPool.clear();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return jedisPool;
	}

}

class NacosProxyRedisCredentialsProvider implements RedisCredentialsProvider {

	protected RedisCredentials redisCredentials;

	@Override
	public RedisCredentials get() {
		return redisCredentials;
	}

	public void setRedisCredentials(RedisCredentials redisCredentialC) {
		this.redisCredentials = redisCredentialC;
	}
}
