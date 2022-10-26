package com.example.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.userservice.dto.ScenarioDto;

@Configuration
public class RedisConfig {

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory();
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
		redisTemplate.setConnectionFactory(redisConnectionFactory());
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(ScenarioDto.class));
		return redisTemplate;
	}

	// // 리스너어댑터 설정
	// @Bean
	// MessageListenerAdapter messageListenerAdapter() {
	// 	return new MessageListenerAdapter(new RedisSubService());
	// }

	// 컨테이너 설정
	// @Bean
	// RedisMessageListenerContainer redisContainer() {
	// 	RedisMessageListenerContainer container = new RedisMessageListenerContainer();
	// 	container.setConnectionFactory(redisConnectionFactory());
	// 	// container.addMessageListener(messageListenerAdapter(), topic());
	// 	return container;
	// }

	// pub/sub 토픽 설정
	// @Bean
	// ChannelTopic topic() {
	// 	return new ChannelTopic("scenario-knowledge");
	// }
}
